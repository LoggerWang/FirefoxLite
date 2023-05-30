package de.blinkt.openvpn

import android.os.RemoteException
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.mucc.flownet.baseUrl
import de.blinkt.openvpn.core.ConfigParser
import de.blinkt.openvpn.core.ProfileManager
import de.blinkt.openvpn.core.VPNLaunchHelper
import de.blinkt.openvpn.flowapi.*
import de.blinkt.openvpn.utils.ConnectState
import de.blinkt.openvpn.utils.ProxyModeEnum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.StringReader

object OpenVpnApi {
    var appId = ""
    var userId = ""
    var mCurrentProxyMode = ProxyModeEnum.PROXY_ALL
    val mSmartPkgNameList = ArrayList<String>() // 智能代理的列表
    val mCustomPkgNameList = ArrayList<String>() // 自定义代理app列表的集合

    var serverListLiveData = MutableLiveData<ServerBean>()
    var serverLiveData = MutableLiveData<VpnProfileBean>()
    var serverStateLiveData = MutableLiveData<ConnectState>()

    lateinit var mActivity: AppCompatActivity

    fun setActivity(activity: AppCompatActivity) {
        mActivity = activity
        mActivity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                if (serverStateLiveData.value == ConnectState.STATE_START) VpnHelper.instance.stopVpn()
                VpnHelper.instance.unRegisterStateReceiver()
            }
        })
        VpnHelper.instance.initStateReceiver()
    }

    fun setBaseUrl(url: String) {
        baseUrl = url
    }

    fun setAppIdUserId(appId: String, userId: String) {
        this.appId = appId
        this.userId = userId
    }

    // 设置模式为智能或者自定义的时候, 需要传入包名列表
    fun setProxyMode(mode: ProxyModeEnum) {
        mCurrentProxyMode = mode
    }


    // 获取节点列表
    fun getServerNode() {
        CoroutineScope(Dispatchers.IO).launch {
            mActivity.flowRequest {
                getServerNode(appId)
            }.catchError {
                Log.e("muccc_e", this.message ?: "--")
            }.collect {
                serverListLiveData.postValue(it.data)
            }
        }
    }

    // 获取节点
    fun getNodeFileMsgNew(map: HashMap<String, String>) {
        CoroutineScope(Dispatchers.IO).launch {
            mActivity.flowRequest {
                getNodeFileMsgNew(map)
            }.catchError {
                Log.e("muccc_e", this.message ?: "--")
            }.collect {
                serverLiveData.postValue(it.data)
            }
        }
    }

    // 设置连接的节点
    fun setSelectNode(node: ServerNodeBean, tryConnect: Boolean = true) {
        VpnHelper.instance.mSelectServerNode = node
        if (tryConnect) checkFileConnect()
    }

    // 获取节点信息并连接
    fun checkFileConnect() {
        VpnHelper.instance.checkFileConnect()
    }

    fun checkDownloadUrl(country: String, salt: String,  downloadUrl: String) {
        VpnHelper.instance.mSelectServerNode =
            ServerNodeBean("", "", "", "", country, "", "", "", "", "", "", downloadUrl,salt)
        VpnHelper.instance.checkDownUrl()
    }

    // 获取当前连接状态
    fun getCurrentState(): ConnectState {
        return serverStateLiveData.value ?: ConnectState.STATE_DISCONNECTED
    }

    // 启动VPN, 全部代理
    @Throws(RemoteException::class)
    fun startVpn(config: String?, sCountry: String?, userName: String?) {
        val cp = ConfigParser()
        try {
            cp.parseConfig(StringReader(config))
            val vp = cp.convertProfile() // Analysis.ovpn
            Log.d("muccc", "startVpnInternal: ==============$cp\n$vp")
            vp.mName = sCountry
            if (vp.checkProfile(OpenVpnApi.mActivity) != R.string.no_error_found) {
                throw RemoteException(mActivity.getString(vp.checkProfile(mActivity)))
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
}