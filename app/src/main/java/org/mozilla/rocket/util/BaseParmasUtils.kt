package org.mozilla.rocket.util

import android.accounts.AccountManager
import android.os.Build
import com.anysitebrowser.base.core.beylaid.BeylaIdHelper
import com.anysitebrowser.base.core.utils.app.AppDist
import com.anysitebrowser.base.core.utils.lang.ObjectStore
import com.anysitebrowser.tools.core.utils.Utils
import java.util.Locale

/**
 * @desc:
 * @author:  wanglezhi
 * @createTime:  2023/6/9 4:42 PM
 */
//fun getBaseParams():HashMap<String,String>{
//    val requestBuilder: Request.Builder =
//        original.newBuilder() //第一次获取token时，“user_id”和“identity_id”为空，需要服务端那边特殊处理一下
//            //先暂时写个假数据
//            .header(
//                "user_id",
//                if (TextUtils.isEmpty(AccountManager.getUserId())) "userId" else AccountManager.getUserId()
//            )
//            .header(
//                "identity_id",
//                if (TextUtils.isEmpty(AccountManager.getToken())) "userToken" else AccountManager.getToken()
//            )
//            .header("app_id", AppDist.getAppId(ObjectStore.getContext()))
//            .header(
//                "app_version_name", Utils.getVersionName(
//                    ObjectStore.getContext(), ObjectStore.getContext().getPackageName()
//                )
//            )
//            .header(
//                "app_version_code",
//                java.lang.String.valueOf(Utils.getVersionCode(ObjectStore.getContext()))
//            )
//            .header("api_version", 1.toString())
//            .header("beyla_id", BeylaIdHelper.getBeylaId())
//            .header("gaid", getEncodeGAid())
//            .header("os_type", "Android")
//            .header("os_version", java.lang.String.valueOf(Build.VERSION.SDK_INT))
//            .header("country", getEncodeCountryCode(ObjectStore.getContext()))
//            .header("lang", Locale.getDefault().getLanguage())
//            .header("sign", sign)
//            .header("time_stamp", java.lang.String.valueOf(timeMillis))
//            .method(original.method(), original.body())
//}