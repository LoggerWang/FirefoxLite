package de.blinkt.openvpn

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.VpnService
import android.os.RemoteException
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import de.blinkt.openvpn.core.*
import de.blinkt.openvpn.model.ServerNodeBean
import de.blinkt.openvpn.flowapi.catchError
import de.blinkt.openvpn.flowapi.flowRequest
import de.blinkt.openvpn.flowapi.flowRequestDown
import de.blinkt.openvpn.utils.ConnectState
import de.blinkt.openvpn.utils.ProxyModeEnum
import de.blinkt.openvpn.utils.Settings
import de.blinkt.openvpn.utils.VpnEncryptUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*

class VpnHelper {
    companion object {
        val instance = VpnHelper()
    }

    private val TAG = "OpenVpnApi"

    private val settings by lazy { Settings(OpenVpnApi.mActivity, "CLICK_SP_KEY") }
    var mSelectServerNode: ServerNodeBean? = null // 当前节点

    private var inData = 0L
    private var outData = 0L
    private var diffIn = 0L
    private var diffOut = 0L

    fun initStateReceiver() {
        LocalBroadcastManager.getInstance(OpenVpnApi.mActivity)
            .registerReceiver(broadcastReceiver, IntentFilter("connectionState"))
    }

    fun unRegisterStateReceiver() {
        LocalBroadcastManager.getInstance(OpenVpnApi.mActivity)
            .unregisterReceiver(broadcastReceiver)
    }

    private var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                val state = intent.getStringExtra("state")
                if (!state.isNullOrEmpty()) {
                    setStatus(state)
                } else {
                    inData = intent.getLongExtra("in", inData)
                    outData = intent.getLongExtra("out", outData)
                    diffIn = intent.getLongExtra("diffIn", diffIn)
                    diffOut = intent.getLongExtra("diffOut", diffOut)
                    //  updateConnectionData()
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setStatus(connectionState: String) {
        Log.e("muccc_status", connectionState)
        when (connectionState) {
            "DISCONNECTED" -> {
                OpenVPNService.setDefaultStatus()
                OpenVpnApi.serverStateLiveData.value = ConnectState.STATE_DISCONNECTED
            }
            "CONNECTED" -> {
                OpenVpnApi.serverStateLiveData.value = ConnectState.STATE_START
            }// it will use after restart this activity
            "WAIT" -> {}// "waiting for server connection!!"
            "AUTH" -> {} //"server authenticating!!"
            "RECONNECTING" -> {}
            "NONETWORK" -> {}  //"No network connection"
            "RESOLVE" -> {}
            "USERPAUSE" -> {}
        }
    }

    fun checkFileConnect() {
        if (OpenVpnApi.serverStateLiveData.value != ConnectState.STATE_DISCONNECTED) {
            showToast(OpenVpnApi.mActivity.getString(R.string.preparing));return
        }

        if (!netCheck(OpenVpnApi.mActivity)) {
            showToast(OpenVpnApi.mActivity.getString(R.string.net_unavailable_content))
            OpenVpnApi.serverStateLiveData.value = ConnectState.STATE_DISCONNECTED
            return
        }
        // 判断用户是否还有流量
        checkVpnFileMsg()
    }

    private fun checkVpnFileMsg() {
        OpenVpnApi.serverStateLiveData.value = ConnectState.STATE_PREPARE
        val vpnFileName = settings.get("id_${mSelectServerNode!!.id}")
        val filePathName = OpenVpnApi.mActivity.cacheDir.path + File.separator + vpnFileName
        if (vpnFileName.isNullOrEmpty() || !File(filePathName).exists()) {
            // 没有存过信息, 则直接去下载  // 存过信息, 但是缓存文件不再了, 也要重新下载
            Log.e("muccc_status", "no_file")
            getVpnFileMsg(OpenVpnApi.mActivity.cacheDir.path)
            return
        }
        parseVpnFile(vpnFileName)
    }

    fun stopVpn(): Boolean {
        try {
            OpenVPNThread.stop()
            showToast(OpenVpnApi.mActivity.getString(R.string.connection_disconnected))
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun showToast(txt: String) {
        Toast.makeText(OpenVpnApi.mActivity, txt, Toast.LENGTH_LONG).show()
    }

    @SuppressLint("MissingPermission")
    private fun netCheck(context: Context): Boolean {
        val cm =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nInfo = cm.activeNetworkInfo
        return nInfo != null && nInfo.isConnectedOrConnecting
    }

    private fun getVpnFileMsg(filePath: String) {
        val map = HashMap<String, String>()
        map.put("userId", OpenVpnApi.userId)
        map.put("nodeIp", mSelectServerNode!!.nodeIp)
        map.put("serverId", mSelectServerNode!!.serverId)
        map.put("serverName", mSelectServerNode!!.serverName)
        map.put("serverOrgId", mSelectServerNode!!.serverOrgId)
        map.put("appId", OpenVpnApi.appId)
        CoroutineScope(Dispatchers.IO).launch {
            OpenVpnApi.mActivity.flowRequest {
                getNodeFileMsg(map)
            }.catchError {
                Log.e("muccc_e", this.message ?: "--")
                OpenVpnApi.serverStateLiveData.value = ConnectState.STATE_DISCONNECTED
                showToast(OpenVpnApi.mActivity.getString(R.string.file_empty))
            }.collect {
                if (it.data.isNullOrEmpty()) {
                    OpenVpnApi.serverStateLiveData.value = ConnectState.STATE_DISCONNECTED
                    showToast(OpenVpnApi.mActivity.getString(R.string.file_empty))
                } else {
                    downloadVpnFile(it.data, filePath)
                }
            }
        }
    }

    fun checkDownUrl() {
        OpenVpnApi.serverStateLiveData.value = ConnectState.STATE_PREPARE
        val vpnFileName = settings.get("id_${mSelectServerNode!!.id}")
        val filePathName = OpenVpnApi.mActivity.cacheDir.path + File.separator + vpnFileName
        if (vpnFileName.isNullOrEmpty() || !File(filePathName).exists()) {
            // 没有存过信息, 则直接去下载  // 存过信息, 但是缓存文件不再了, 也要重新下载
            Log.e("muccc_status", "no_file")
            downloadVpnFile(mSelectServerNode!!.downloadUrl, OpenVpnApi.mActivity.cacheDir.path)
            return
        }
        parseVpnFile(vpnFileName)
    }

    private fun downloadVpnFile(fileUrl: String, filePath: String) {
        CoroutineScope(Dispatchers.IO).launch {
            OpenVpnApi.mActivity.flowRequestDown {
                downloadVpnFile(fileUrl)
            }.catchError {
                onSaveFailed(this.message ?: "---")
            }.collect {
                saveFile(fileUrl, filePath, it.string())
            }
        }
    }

    private fun saveFile(fileUrl: String, filePath: String, rawData: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1)
                val file = File(filePath + File.separator + fileName)
                if (!file.exists()) {
                    val c = file.createNewFile()
                }
                val fw = FileWriter(file)
                fw.write(rawData)
                fw.close()
                Log.e("muccc_status", "save_fail_success")
                onSaveSuccess(fileName)
            } catch (e: FileNotFoundException) {
                onSaveFailed("saveFile: FileNotFoundException ")
                Log.e("muccc_status", "saveFile: FileNotFoundException")
            } catch (e: java.lang.Exception) {
                onSaveFailed("saveFile: IOException ")
                Log.e("muccc_status", "saveFile: IOException")
            }
        }
    }

    private fun onSaveSuccess(fileName: String) {
        settings.set("id_${mSelectServerNode!!.id}", fileName)
        parseVpnFile(fileName)
    }

    private fun onSaveFailed(failed: String) {
        OpenVpnApi.serverStateLiveData.value = ConnectState.STATE_DISCONNECTED
        showToast(OpenVpnApi.mActivity.getString(R.string.file_empty))
    }

    // 解析文件
    private fun parseVpnFile(fileName: String) {
        Log.e("muccc_status", "parse_file")
        CoroutineScope(Dispatchers.IO).launch(Dispatchers.IO) {
            // .ovpn file
            val file = File(OpenVpnApi.mActivity.cacheDir.path + File.separator + fileName)
            if (file.exists()) {
                var s: String?
                val sb = StringBuffer()
                val reader = BufferedReader(FileReader(file))
                try {
                    while (reader.readLine().also { s = it } != null) sb.append(s + "\n\r")
                    reader.close()
                } catch (e: Exception) {
                    println("file error")
                }

                mSelectServerNode!!.vpnFileStr = sb.toString()
                withContext(Dispatchers.Main) { prepareVpn() }
            } else {
                withContext(Dispatchers.Main) {
                    onSaveFailed("")
                }
            }
        }
    }

    private fun decryptStream(inputStream: InputStream): InputStream {
        var outputStream: ByteArrayOutputStream? = null
        try {
            outputStream = ByteArrayOutputStream()
            val bytes = ByteArray(8 * 1024)
            var j: Int
            while (inputStream.read(bytes).also { j = it } != -1) {
                for (i in 0 until j) {
                    outputStream.write(bytes[i].toInt() xor 9527)
                }
            }
            outputStream.flush()
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ByteArrayInputStream(
            outputStream!!.toByteArray()
        )
    }

    private fun prepareVpn() {
        if (OpenVpnApi.serverStateLiveData.value == ConnectState.STATE_PREPARE) {
            if (netCheck(OpenVpnApi.mActivity)) {
                // Checking permission for network monitor
                Log.e("muccc_status", "start_vpn")
                val intent = VpnService.prepare(OpenVpnApi.mActivity)
                if (intent != null) OpenVpnApi.mActivity.startActivityForResult(intent, 998)
                else toStartVpn()    //have already permission
            } else {
                // No internet connection available
                showToast(OpenVpnApi.mActivity.getString(R.string.net_unavailable_content))
            }
        } else if (OpenVpnApi.serverStateLiveData.value == ConnectState.STATE_START) {
            // VPN is stopped, show a Toast message.
            stopVpn()
            OpenVpnApi.serverStateLiveData.value = ConnectState.STATE_DISCONNECTED
        }
    }

    private fun toStartVpn() {
        var fileStr = mSelectServerNode?.vpnFileStr
        val regionName = mSelectServerNode?.regionName
        fileStr = VpnEncryptUtil.Decrypt(
            fileStr, mSelectServerNode!!.salt, mSelectServerNode!!.downloadUrl
        )
        if (fileStr.isNullOrEmpty())
            fileStr = "setenv UV_ID a78ae2125fae4296ab6f080b3647c37e\n" +
                    "setenv UV_NAME summer-plains-6114\n" +
                    "client\n" +
                    "dev tun\n" +
                    "dev-type tun\n" +
                    "remote 2600:3c03::f03c:93ff:fe66:ceba 16100 udp6\n" +
                    "remote 143.42.112.178 16100 udp\n" +
                    "remote-random\n" +
                    "nobind\n" +
                    "persist-tun\n" +
                    "cipher AES-128-CBC\n" +
                    "auth SHA1\n" +
                    "verb 2\n" +
                    "mute 3\n" +
                    "push-peer-info\n" +
                    "ping 10\n" +
                    "ping-restart 60\n" +
                    "hand-window 70\n" +
                    "server-poll-timeout 4\n" +
                    "reneg-sec 2592000\n" +
                    "sndbuf 393216\n" +
                    "rcvbuf 393216\n" +
                    "remote-cert-tls server\n" +
                    "comp-lzo no\n" +
                    "key-direction 1\n" +
                    "<ca>\n" +
                    "-----BEGIN CERTIFICATE-----\n" +
                    "MIIFcjCCA1qgAwIBAgIJAP8kRBylOcERMA0GCSqGSIb3DQEBCwUAMEYxITAfBgNV\n" +
                    "BAoMGDY0MWQ2MDM4OTM3NTZmMjNhNjgyZDU5MTEhMB8GA1UEAwwYNjQxZDYwMzg5\n" +
                    "Mzc1NmYyM2E2ODJkNTkyMB4XDTIzMDMyNDA4MzI1N1oXDTQzMDMxOTA4MzI1N1ow\n" +
                    "RjEhMB8GA1UECgwYNjQxZDYwMzg5Mzc1NmYyM2E2ODJkNTkxMSEwHwYDVQQDDBg2\n" +
                    "NDFkNjAzODkzNzU2ZjIzYTY4MmQ1OTIwggIiMA0GCSqGSIb3DQEBAQUAA4ICDwAw\n" +
                    "ggIKAoICAQCzN5hyUXcIN2bIEOGWsJhKEmlAuiwg7b/OJjfnPxrn/f8xOyFnnKYf\n" +
                    "I/iatDAvwylv6MyOZpxhFuTaAzkE/4SpNt6ZAGtIcu29/cN7AdReDRHCHqTbU0MX\n" +
                    "ol+bQ4FFIBycGBg+ikmd32ofVZ0G2dkHvUrnKU0tCNFdxGJ4vNp8L0ZpP2NhUNO+\n" +
                    "/6nCoTv+IczApmBpNsy8eBSpJ0KkQh8avBYmQVPV8ljWVTXq65b1fDayoeGC6vqi\n" +
                    "bEuNGPH+iJb0N7ds2xLPl8Nc690kbnpd2uv3NR5ll3eJLVD82cxTDMFtCEyKMca5\n" +
                    "JuqR93hlSRUmGIL9D1Hl1wib551PONhmWwdG4uah/5k5o9zk2mmRd1w6d54W1HWx\n" +
                    "ZILxR1oIM9pG7ar4f99Kbj4GQ0gMslDTnTRBSuaGGTZIMXl8GLMdUqmfi0ZLRxre\n" +
                    "Mua7A1zBfq3D5iER9923Yh8H8P1J8SdLyoLwsApT8YxjckZ5xfFKYAJI21f5lh5c\n" +
                    "A20rOJDLpy59HOGPn50eBw0OelraxGOuNQJMlVwdyVGI7yI3ObvQU0T3BIiuE7Bc\n" +
                    "PevHqdWi0dP5eSK/QSArEQ5gflDdo5QcxVa7/HinZ8S83+wkavv9eBLyyV1B0FZw\n" +
                    "IBzhWWwQPeGCIrx9jWkMAI/TSIsD2cjHZUbtx0CNDloVN6+FbDZDYwIDAQABo2Mw\n" +
                    "YTAOBgNVHQ8BAf8EBAMCAQYwDwYDVR0TAQH/BAUwAwEB/zAdBgNVHQ4EFgQUNyKI\n" +
                    "a/Jknf6aEQ8ZJTdnq2/sJN4wHwYDVR0jBBgwFoAUNyKIa/Jknf6aEQ8ZJTdnq2/s\n" +
                    "JN4wDQYJKoZIhvcNAQELBQADggIBAJ7aGD0CvZDmdf721Xp+yA7VjWnVVZCqG6e0\n" +
                    "Uv1Cshk/wtV6tTMHshts2P2h6r8z9OuO4/fZ40cLWhDnMfWywwwW3I2jOyzIMeH6\n" +
                    "Ya2qz03C0Y8VlgRNsLLMg3vnsvGyohKvWwwUmPg1MvFsZXwbTQ0pgWEFZA1wOfWA\n" +
                    "XDh7r4GlIYTj5dPkIfrY8aRxblUz4a7vyYYVHK9ZdFz1Ij9b+gf/Okv+WbNwcpsy\n" +
                    "re1Oi1lbO3VtCBCroRQIGogWfmb+DLHjp1dnCLQuthQ4D8zUgaUFAvyTcu+cQr9N\n" +
                    "9U410Us7wNk3HNgKzq170Rg2XLlxjpyArClB/3U3SinHjOmuULU+9tGG/UUgsSGP\n" +
                    "PdmPEPP5P0g07kJmhvqri5bFF0xR96ZwnIJC51ZKqO8ciJdHAHJg58fpAEN3ccl8\n" +
                    "iE9kPABEhlM6w6PP+wIBaZUu4g+0I+YUxJmIhcQNxRxpvFb+za0PFAeB8KDVrd4m\n" +
                    "guGIRgO1HrOE0+ZGgM9GF81zG2soLov+pmDXJXoZXVYul18VWyCQWqtX6OyjzJTq\n" +
                    "FizlxC769IDWM4160rBGshLOtrhBZn87lLTq+wsI63n3yox3MjqFH5/OTe615Yei\n" +
                    "hjMLn82buICFVEfJD1oBYeKKLjUsIHD+BGgvDiHh0WYgEb0kTtnTPE4uPL7BSn5b\n" +
                    "O7Jo1ib6\n" +
                    "-----END CERTIFICATE-----\n" +
                    "</ca>\n" +
                    "<tls-auth>\n" +
                    "-----BEGIN OpenVPN Static key V1-----\n" +
                    "656646b9b9bc70477444d5e0a7e47e82\n" +
                    "cda8ca3b232603628b92406c13eb9842\n" +
                    "1830cdfc4b9f84889f4189368ee1228b\n" +
                    "8d93b69f73c2516f96b0a193e32ec23d\n" +
                    "5a1a03fab565d6458857070b316d01a8\n" +
                    "bd7053db040bb0495cb4a8a0d878ae88\n" +
                    "c6acf9fd44e07089708d5d15062398d0\n" +
                    "f87497c358c1df532fa17cf43fc4cbcf\n" +
                    "143502b1c009c9f9c374e557389e12cb\n" +
                    "1df2e595aa863671faae2f923d851dc7\n" +
                    "2af2f7404a45933d37b457c98e9df66a\n" +
                    "263bc3354ad77414e5f1d77855dd09b6\n" +
                    "4b5b8ccfc514feca2caa9343fed119c7\n" +
                    "32fa5a3b5d67a6c7fbcbb9df10ba591f\n" +
                    "7605571b14313106206b1ca1f98e9469\n" +
                    "cfd28a45273b09e5bf6db57669f5861a\n" +
                    "-----END OpenVPN Static key V1-----\n" +
                    "</tls-auth>\n" +
                    "<cert>\n" +
                    "-----BEGIN CERTIFICATE-----\n" +
                    "MIIFgTCCA2mgAwIBAgIJAJ6rOaIUguoFMA0GCSqGSIb3DQEBCwUAMEYxITAfBgNV\n" +
                    "BAoMGDY0MWQ2MDM4OTM3NTZmMjNhNjgyZDU5MTEhMB8GA1UEAwwYNjQxZDYwMzg5\n" +
                    "Mzc1NmYyM2E2ODJkNTkyMB4XDTIzMDMyNDA4MzYwMVoXDTQzMDMxOTA4MzYwMVow\n" +
                    "RjEhMB8GA1UECgwYNjQxZDYwMzg5Mzc1NmYyM2E2ODJkNTkxMSEwHwYDVQQDDBg2\n" +
                    "NDFkNjBmMDkzNzU2ZjIzYTY4MmQ2ODUwggIiMA0GCSqGSIb3DQEBAQUAA4ICDwAw\n" +
                    "ggIKAoICAQDQ28BXqsO5n2phLB8TFwIt6xZBK6nn3I1iLx69HzFRxSTBCQKChLuE\n" +
                    "JV8uI/4R+GKnS3vRX/u6ATAvPgJzulJHSWNsc5RrX2jBgBltrDS83Q7ahmpr73I0\n" +
                    "yCP6ZQA9gmLm6+edCE1IflDzgaBN2sxniOxmTMY8wMuoJw8/JJP3QJ6VRmeB1176\n" +
                    "5HiSp762CKQSOvizJw94bByCu/Rsv1Re3C3vWo7qpWAsv60ijU6yWqJcM4IFaFUr\n" +
                    "IXZxqqCxpAd1oqeq8oiUn7BYY2KGpIDasD3Wsd4VXXx+Me73EjgyYkKJrMOXie26\n" +
                    "PeA/Zly7Sfdk0nvTS9EAc37x144eOSzhgBfz6+ybHygG6dfCUpb9eOAx91FvZygi\n" +
                    "Wvksh6oIxubOIngcfZiSxT0W2AM4+n5cIg72x3HT19tft4TkZmnmWBIaKxKoofy7\n" +
                    "ENI6YHoTWQWQwXGVRNuqY31z/Wh/tU7I/i1kJl3SpxN7tAT8fg21rTzUOaAGjhbQ\n" +
                    "y+dyxBVuJYrC2UvyfM7Wcfi9jRX9Zvd/onSKe508p1f1+MMWOuxhI0OXVG+yiTN5\n" +
                    "cmJBprhpG/DDtsrzEtUPo6chX8fiZFleYo5WtTz3aRdp5ayzmANobw5pHQl4WFHk\n" +
                    "IGL3mmlrtqB6F66tcMpHms7h6BNDfH0vR9FY4URvsSNd+uHlfnLqrwIDAQABo3Iw\n" +
                    "cDAOBgNVHQ8BAf8EBAMCBaAwCQYDVR0TBAIwADATBgNVHSUEDDAKBggrBgEFBQcD\n" +
                    "AjAdBgNVHQ4EFgQU+Jx7PM7ym0qZ+eYQdl/uQcwahIkwHwYDVR0jBBgwFoAUNyKI\n" +
                    "a/Jknf6aEQ8ZJTdnq2/sJN4wDQYJKoZIhvcNAQELBQADggIBADGB/jQpyS7k6732\n" +
                    "vwmxbaTP8M5DlcHKDeUSvL7pmW4uqOWvp20KttPz93+1UxQ1bvYn9FhasTDiXVQZ\n" +
                    "6pn1gZ6QfRr3hGjHqrYtev1BgQHiqpUFCReikG42F8OIxJeNQiMN/K/jtfXBKAJr\n" +
                    "DROaSoswoizd8SgAACwtJtqyApaJyZqc6ronbnqV7ujISGWRHc+0liao35Kn5svh\n" +
                    "nfrhUuaNvFqDfpMCtr6PPo5tOYG9I+SWEl9Sy2Oq/uHSRS2vR41VYabQgKazpC1x\n" +
                    "5+YuaBigJQ6EHLQ6X3p0VOCklmEX2QlVHry7AFGEI4tGN6JSDSuLr+Fc4opuYTqw\n" +
                    "Fa8Cbmu2TWZRuJ9+Pq2xy6Vnnkdohzp5c+x/JioUenuviUx2vZ0vVkVWzcFkBmUw\n" +
                    "OODSo7tRndEdIoZ2L6Vhsomt12dxFdhisx6N7MuMVOCKmbz36Gmy7wYWZG6AZ2+p\n" +
                    "4oMicvKB9mt11nuy4Y27pEreawxQI7TSVtikFN7HWzFPusjzyVeAHG7w16BWEgAd\n" +
                    "lPQV54MXFXDOk07vqT4c7bX+mC/CJLjsV+saiboUixqENw6GDMMrRiMA11hpHHlG\n" +
                    "eQ3P0uetvHeXjOElN9wxnDFOUzrSi6tRKiZp32Yz1D4hqnFUFDDkLgNBbXMpR+mz\n" +
                    "E6Q/iAyJtbwlk56qCIfNEmIMtx6I\n" +
                    "-----END CERTIFICATE-----\n" +
                    "</cert>\n" +
                    "<key>\n" +
                    "-----BEGIN PRIVATE KEY-----\n" +
                    "MIIJQwIBADANBgkqhkiG9w0BAQEFAASCCS0wggkpAgEAAoICAQDQ28BXqsO5n2ph\n" +
                    "LB8TFwIt6xZBK6nn3I1iLx69HzFRxSTBCQKChLuEJV8uI/4R+GKnS3vRX/u6ATAv\n" +
                    "PgJzulJHSWNsc5RrX2jBgBltrDS83Q7ahmpr73I0yCP6ZQA9gmLm6+edCE1IflDz\n" +
                    "gaBN2sxniOxmTMY8wMuoJw8/JJP3QJ6VRmeB11765HiSp762CKQSOvizJw94bByC\n" +
                    "u/Rsv1Re3C3vWo7qpWAsv60ijU6yWqJcM4IFaFUrIXZxqqCxpAd1oqeq8oiUn7BY\n" +
                    "Y2KGpIDasD3Wsd4VXXx+Me73EjgyYkKJrMOXie26PeA/Zly7Sfdk0nvTS9EAc37x\n" +
                    "144eOSzhgBfz6+ybHygG6dfCUpb9eOAx91FvZygiWvksh6oIxubOIngcfZiSxT0W\n" +
                    "2AM4+n5cIg72x3HT19tft4TkZmnmWBIaKxKoofy7ENI6YHoTWQWQwXGVRNuqY31z\n" +
                    "/Wh/tU7I/i1kJl3SpxN7tAT8fg21rTzUOaAGjhbQy+dyxBVuJYrC2UvyfM7Wcfi9\n" +
                    "jRX9Zvd/onSKe508p1f1+MMWOuxhI0OXVG+yiTN5cmJBprhpG/DDtsrzEtUPo6ch\n" +
                    "X8fiZFleYo5WtTz3aRdp5ayzmANobw5pHQl4WFHkIGL3mmlrtqB6F66tcMpHms7h\n" +
                    "6BNDfH0vR9FY4URvsSNd+uHlfnLqrwIDAQABAoICAC/87dq2v7XcrQKasqEePj/y\n" +
                    "rfBXafh+MnYGwZdOnXReSa2YtlzqxnL8azwcxPm4CmLJ8y8OULZSI5IXO1T/0HvU\n" +
                    "nuWCQO3SxC0Bk9YhJRTn7cvD6pvWLnV2Lt4Pati3JqZMObFZAPjRHSR1+jEpPqHj\n" +
                    "Uf3HUh0PZzvBjb1CWEUNMeK2hH3O0GHxMMrxg94rahdOOWpgZSuUG5DmQqjh5S7O\n" +
                    "hDdkpUXArbJacBtqQFS2gmuQ4iT/7eG1FJPVUoq+V4Bt2PxsMXmEn24Pl8m77mdQ\n" +
                    "60RzJcXe7wXHz1Ixe3wIG6Wqbl5ZMKgEI4wtpszZl+AwhIbt+p2R6+8BSgQjuu3C\n" +
                    "0I2od5WwcXJnW+h3rQhQXldpE0s+TkEpQaEOSPmAJffPuB3pVfxptz13YPHelzYZ\n" +
                    "FmC/oxD9vmEwxEA3QNPCVdv3DQMcGu902lD2i8ZPX6tu9ODWYsnl10ckbFT3Y6Ua\n" +
                    "S2jbf04sifREEKXVD/arS4iDo27ra91tcmz2fU2GWe7yaeSgZQ3yAhjTbj9XEwnV\n" +
                    "a8j2gBuIMkX6eXTuJttd8c0gjDj4jzkhqbcZSmP+ByRR1LzxzMH42EDYKro+xllq\n" +
                    "RrUV8oSAqWK9+J+pz3Zp6Si7YTQMO6E5KdX0TerM4fQz0JoHo5w0D545q7xWv2B9\n" +
                    "x25jc61DxUsqD92QqtuhAoIBAQDnm3big1Twwn3mwLyDoGVyorXzyGZ/mCvArERJ\n" +
                    "0s118wfIg0sIlyRd58lwoFHqemRCrNoGz8R/faAwDAdJOpXoAhy39sEijnnpL6hy\n" +
                    "7eUNPWdWEx+sficJ0FNJRtVmwPdQgSbypJwVLU8Nr/Nx85OHXk5jtMGlH0fh8yQY\n" +
                    "WjdLRR/ESGuh20czbCBINNQI3XPqsGMBZFZA7SWAFxpWsLwEB94osSu8xcsmfqe4\n" +
                    "JRZPyN0FPlubtl0fDvLZbZSkCiUhoXzbD/awu9LDi0KwsAyC6mTa2zOAS1iVWovi\n" +
                    "QuHRdSPBQvhGGvEe9LqU8VbCVze0gKRMpiangk7LxHODDaunAoIBAQDm2vADgf2n\n" +
                    "oMCF/eX0rgi3exDIJ/Q8+8rREGDqZlCvLjDRCDSdo7n1+hbatrIp317b8OOPo7ch\n" +
                    "bg2cqi8IbQtcNjyteTyV3wMa42F16p+azXPOCX4BksoHirN+p9AfJR50pJpivsuI\n" +
                    "4u441KVki104xVSmJbV5xHvNBW/xbjvbVdV3iXFCW0Jqg1FRIs5bK9A07v1yP2TQ\n" +
                    "VhqvSdSo+KtmEsRRSxkyf6xPLEAU+OYwPPFAaDJM9nFpQM1f1f7XYvm6PeNlSL2d\n" +
                    "S1BIzTDNf8L+IosrMlMrEEdjz4z4hKvIDQXtV2CQ5o89DcN5Lmc6Rat1qr1N00gy\n" +
                    "bAAh1je6Twm5AoIBAQDFjqjD4j6r91qpYDHtHP4r4HrP7Vrw734ypT7CsdusBpfX\n" +
                    "A5cXRsUwjKwZM8dr+OZYsVRwhv0XurLPCROV9TE1a8fPa92mel2m+I35Ks88ken5\n" +
                    "U+qqmhj2mArS8GbLdUIAG4p7Ysn4jmNmS3We96QZzemTxvV+wcXxCBL76shLnjTQ\n" +
                    "mu2kbIS5ajvHG7jZrvxXbgmlgE7Kpv5b2lkPJUXbMlLf5vBl31nABj1OJ+R2PeG5\n" +
                    "vU0q701TVs7zhDNMEZNGspbrzRwh+LR5M+q5CBJEAWfx/xRZFOwEye2UHHS+0+qh\n" +
                    "yVw5gHW4cAxPS+UzdG4Bu9FpyDgknlRUH8hgO2L1AoIBAQDic7tixX2rlD2mcoPD\n" +
                    "O4Bjc33nyYGE3L17zOQ0qn7VCKtsMO9j/zoodtU6YGJuPqXPfAHNYhv+1/TUTABs\n" +
                    "9IJjRvTIehebMDLcilWNFMTRVl4VqywGoRY6xa0DPdwi4Qd6BglHeb8wHOLvrnA7\n" +
                    "K5YbXT6G6JCWzM1gNMg1fRuMMSt/4bvPCrQgxYXb4BeqSVUox5KoIlVjJENy1Sqd\n" +
                    "QajoLNqUn+Usqx68XzzRFcK/aNiChHNrGYfwZlfPfZxJbdZoAdNxBCS0Ci/L6jPI\n" +
                    "MV52ai44Blqc8JAG8Y5oLcRQLa41vjOEkBXMHxczZPhvqe3iM8UUdX/prhWHG5p4\n" +
                    "0JVJAoIBAGNGehd5m2SDDxzFdeq7mdi1aNAVtE9thrKFbAHZ2cZuLe/A4MA7wGw1\n" +
                    "A7TRyHe7QF11sdl6Jmo94Gu5U3xLC+hLNdg7X0cOdxWEHufBZAmBI9qfeFrbYpPt\n" +
                    "qTgKONud49nlM96IpRYL+58WzeAwLhJz/o2oMWS+X5YMUufK383tixZG0+4Oue9C\n" +
                    "f7NtgQpAzpCxDaJFM/JXrgWmOuMsFbXlhFxouV1TCOCyNB4Z5y6FxWioevego3FM\n" +
                    "Rk36gdAmjK7plzuwyAcc9yoTcHKch6OcGOoE6dSnyw8Q8w/QeQR2rSOcNn4ioSh0\n" +
                    "DA7iVTatKClCfQ9vrUCRyxK3S3Pjdo8=\n" +
                    "-----END PRIVATE KEY-----\n" +
                    "</key>"
        try {
            if (OpenVpnApi.mCurrentProxyMode == ProxyModeEnum.PROXY_SMART) {
                startVpnInternalSmart(
                    fileStr, regionName, OpenVpnApi.userId, OpenVpnApi.mSmartPkgNameList
                )
            } else if (OpenVpnApi.mCurrentProxyMode == ProxyModeEnum.PROXY_CUSTOM) {
                startVpnInternal(
                    fileStr, regionName, OpenVpnApi.userId, OpenVpnApi.mCustomPkgNameList
                )
            } else {
                startVpn(fileStr, regionName, OpenVpnApi.userId)
            }
            OpenVpnApi.serverStateLiveData.value = ConnectState.STATE_CONNECTING
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    // 启动VPN, 全部代理
    @Throws(RemoteException::class)
    fun startVpn(config: String?, sCountry: String?, userName: String?) {
        val cp = ConfigParser()
        try {
            cp.parseConfig(StringReader(config))
            val vp = cp.convertProfile() // Analysis.ovpn
            Log.d(TAG, "startVpnInternal: ==============$cp\n$vp")
            vp.mName = sCountry
            if (vp.checkProfile(OpenVpnApi.mActivity) != R.string.no_error_found) {
                throw RemoteException(OpenVpnApi.mActivity.getString(vp.checkProfile(OpenVpnApi.mActivity)))
            }
            vp.mProfileCreator = OpenVpnApi.mActivity.packageName
            vp.mUsername = userName
            vp.mAllowedAppsVpn.clear()
            vp.mAllowedAppsVpnAreDisallowed = true
            // vp.mPassword = pw;
            ProfileManager.setTemporaryProfile(OpenVpnApi.mActivity, vp)
            VPNLaunchHelper.startOpenVpn(vp, OpenVpnApi.mActivity)
        } catch (e: IOException) {
            throw RemoteException(e.message)
        } catch (e: ConfigParser.ConfigParseError) {
            throw RemoteException(e.message)
        }
    }

    // 启动VPN, 智能代理, 传入不需要代理的包名的列表
    @Throws(RemoteException::class)
    fun startVpnInternalSmart(
        config: String?, sCountry: String?, userName: String?, disAllowPkgs: List<String>
    ) {
        val cp = ConfigParser()
        try {
            cp.parseConfig(StringReader(config))
            val vp = cp.convertProfile() // Analysis.ovpn
            Log.d(TAG, "startVpnInternal: ==============$cp\n$vp")
            vp.mName = sCountry
            if (vp.checkProfile(OpenVpnApi.mActivity) != R.string.no_error_found) {
                throw RemoteException(OpenVpnApi.mActivity.getString(vp.checkProfile(OpenVpnApi.mActivity)))
            }
            vp.mProfileCreator = OpenVpnApi.mActivity.packageName
            vp.mUsername = userName
            //            vp.mPassword = pw;
            if (disAllowPkgs.size > 0) {
                vp.mAllowedAppsVpnAreDisallowed = true
                vp.mAllowedAppsVpn.clear()
                vp.mAllowedAppsVpn.addAll(disAllowPkgs)
            }
            ProfileManager.setTemporaryProfile(OpenVpnApi.mActivity, vp)
            VPNLaunchHelper.startOpenVpn(vp, OpenVpnApi.mActivity)
        } catch (e: IOException) {
            throw RemoteException(e.message)
        } catch (e: ConfigParser.ConfigParseError) {
            throw RemoteException(e.message)
        }
    }

    // 启动VPN, 自定义代理, 传入需要使用代理的包名的列表
    @Throws(RemoteException::class)
    fun startVpnInternal(
        config: String?, sCountry: String?, userName: String?, allowPkgs: List<String>
    ) {
        val cp = ConfigParser()
        try {
            cp.parseConfig(StringReader(config))
            val vp = cp.convertProfile() // Analysis.ovpn
            Log.d(TAG, "startVpnInternal: ==============$cp\n$vp")
            vp.mName = sCountry
            if (vp.checkProfile(OpenVpnApi.mActivity) != R.string.no_error_found) {
                throw RemoteException(OpenVpnApi.mActivity.getString(vp.checkProfile(OpenVpnApi.mActivity)))
            }
            vp.mProfileCreator = OpenVpnApi.mActivity.packageName
            vp.mUsername = userName
            //            vp.mPassword = pw;
            if (allowPkgs.size > 0) {
                vp.mAllowedAppsVpnAreDisallowed = false
                vp.mAllowedAppsVpn.clear()
                vp.mAllowedAppsVpn.addAll(allowPkgs)
            }
            ProfileManager.setTemporaryProfile(OpenVpnApi.mActivity, vp)
            VPNLaunchHelper.startOpenVpn(vp, OpenVpnApi.mActivity)
        } catch (e: IOException) {
            throw RemoteException(e.message)
        } catch (e: ConfigParser.ConfigParseError) {
            throw RemoteException(e.message)
        }
    }


}