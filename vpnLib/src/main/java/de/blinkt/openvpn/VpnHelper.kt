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
import de.blinkt.openvpn.utils.ServerState
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
        Log.e("legend", "===VpnHelper===status===$connectionState")
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
            Log.d("legend", ("===Error==checkFileConnect==" ) )
            OpenVpnApi.serverStateLiveData.value = ConnectState.STATE_DISCONNECTED
            return
        }
        checkVpnFileMsg()
    }

    private fun checkVpnFileMsg() {
//        OpenVpnApi.serverStateLiveData.value = ConnectState.STATE_PREPARE
        getVpnFileMsg()
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

    private fun getVpnFileMsg() {
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
                Log.d("legend", ("===Error==getVpnFileMsg==" ) )
                OpenVpnApi.serverStateLiveData.value = ConnectState.STATE_DISCONNECTED
                showToast(OpenVpnApi.mActivity.getString(R.string.file_empty))
            }.collect {
                if (it.data.isNullOrEmpty()) {
                    Log.d("legend", ("===Error==getVpnFileMsg==data is null===" ) )
                    OpenVpnApi.serverStateLiveData.value = ConnectState.STATE_DISCONNECTED
                    showToast(OpenVpnApi.mActivity.getString(R.string.file_empty))
                } else {
                    downloadVpnFile(it.data)
                }
            }
        }
    }

    fun checkDownUrl() {
//        OpenVpnApi.serverStateLiveData.value = ConnectState.STATE_PREPARE
        downloadVpnFile(mSelectServerNode!!.downloadUrl)
    }

    private fun downloadVpnFile(fileUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            OpenVpnApi.mActivity.flowRequestDown {
                downloadVpnFile(fileUrl)
            }.catchError {
                Log.d("legend", ("===Error==downloadVpnFile=====" ) )
                onSaveFailed(this.message ?: "---")
//                OpenVpnApi.mActivity.runOnUiThread {
//                    Log.d("legend", ("===Error==getZoneProfile==" + this.message) )
//                    OpenVpnApi.serverStateLiveData.value = ConnectState.STATE_DISCONNECTED
//                    OpenVpnApi.netLiveData.value = ServerState.SERVER_STATE_ERROR
//                }
            }.collect {
                mSelectServerNode!!.vpnFileStr = it.string()
                withContext(Dispatchers.Main) { prepareVpn() }
            }
        }
    }

    private fun onSaveFailed(failed: String) {
        Log.d("legend", ("===Error==onSaveFailed====="+failed ) )
        OpenVpnApi.mActivity.runOnUiThread {
        OpenVpnApi.serverStateLiveData.value = ConnectState.STATE_DISCONNECTED
        OpenVpnApi.netLiveData.value = ServerState.SERVER_STATE_ERROR
        showToast(OpenVpnApi.mActivity.getString(R.string.file_empty))}
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
                Log.d("legend", ("===Error==prepareVpn=====当前==STATE_PREPARE" ) )
                OpenVpnApi.serverStateLiveData.value = ConnectState.STATE_DISCONNECTED
                // No internet connection available
                showToast(OpenVpnApi.mActivity.getString(R.string.net_unavailable_content))
            }
        } else if (OpenVpnApi.serverStateLiveData.value == ConnectState.STATE_START) {
            // VPN is stopped, show a Toast message.
            stopVpn()
            Log.d("legend", ("===Error==prepareVpn=====当前==STATE_START" ) )
            OpenVpnApi.serverStateLiveData.value = ConnectState.STATE_DISCONNECTED
        }
    }

     fun toStartVpn() {
        var fileStr = mSelectServerNode?.vpnFileStr
        val regionName = mSelectServerNode?.regionName
        fileStr = VpnEncryptUtil.Decrypt(
            fileStr, mSelectServerNode!!.salt, mSelectServerNode!!.downloadUrl
        )
         Log.d("legend", ("===VpnHelper==toStartVpn=====fileStr==$fileStr ==regionName==$regionName" ) )
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
            OpenVpnApi.mActivity.runOnUiThread {
                Log.d("legend", ("===Error==toStartVpn=IOException===$e") )
                OpenVpnApi.serverStateLiveData.value = ConnectState.STATE_DISCONNECTED
                OpenVpnApi.netLiveData.value = ServerState.SERVER_STATE_ERROR
            }
            e.printStackTrace()
        } catch (e: RemoteException) {
            OpenVpnApi.mActivity.runOnUiThread {
                Log.d("legend", ("===Error==toStartVpn=RemoteException===$e") )
                OpenVpnApi.serverStateLiveData.value = ConnectState.STATE_DISCONNECTED
                OpenVpnApi.netLiveData.value = ServerState.SERVER_STATE_ERROR
            }
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
            OpenVpnApi.mActivity.runOnUiThread {
                Log.d("legend", ("===Error==startVpn=IOException===$e") )
                OpenVpnApi.serverStateLiveData.value = ConnectState.STATE_DISCONNECTED
                OpenVpnApi.netLiveData.value = ServerState.SERVER_STATE_ERROR
            }
            throw RemoteException(e.message)
        } catch (e: ConfigParser.ConfigParseError) {
            OpenVpnApi.mActivity.runOnUiThread {
                Log.d("legend", ("===Error==startVpn=ConfigParseError===$e") )
                OpenVpnApi.serverStateLiveData.value = ConnectState.STATE_DISCONNECTED
                OpenVpnApi.netLiveData.value = ServerState.SERVER_STATE_ERROR
            }
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
            OpenVpnApi.mActivity.runOnUiThread {
                Log.d("legend", ("===Error==startVpnInternalSmart=IOException===$e") )
                OpenVpnApi.serverStateLiveData.value = ConnectState.STATE_DISCONNECTED
                OpenVpnApi.netLiveData.value = ServerState.SERVER_STATE_ERROR
            }
            throw RemoteException(e.message)
        } catch (e: ConfigParser.ConfigParseError) {
            OpenVpnApi.mActivity.runOnUiThread {
                Log.d("legend", ("===Error==startVpnInternalSmart=ConfigParseError===$e") )
                OpenVpnApi.serverStateLiveData.value = ConnectState.STATE_DISCONNECTED
                OpenVpnApi.netLiveData.value = ServerState.SERVER_STATE_ERROR
            }
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
            OpenVpnApi.mActivity.runOnUiThread {
                Log.d("legend", ("===Error==startVpnInternal=IOException===$e") )
                OpenVpnApi.serverStateLiveData.value = ConnectState.STATE_DISCONNECTED
                OpenVpnApi.netLiveData.value = ServerState.SERVER_STATE_ERROR
            }
            throw RemoteException(e.message)
        } catch (e: ConfigParser.ConfigParseError) {
            OpenVpnApi.mActivity.runOnUiThread {
                Log.d("legend", ("===Error==startVpnInternal=ConfigParseError===$e") )
                OpenVpnApi.serverStateLiveData.value = ConnectState.STATE_DISCONNECTED
                OpenVpnApi.netLiveData.value = ServerState.SERVER_STATE_ERROR
            }
            throw RemoteException(e.message)
        }
    }


}