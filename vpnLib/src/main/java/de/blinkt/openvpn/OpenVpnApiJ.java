package de.blinkt.openvpn;

import android.content.Context;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.ConfigParser;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VPNLaunchHelper;

public class OpenVpnApiJ {

    private static final String TAG = "OpenVpnApi";
    OnVpnProfileListener onVpnProfileListener;
    public static void startVpn(Context context, String inlineConfig, String sCountry, String userName) throws RemoteException {
        if (TextUtils.isEmpty(inlineConfig)) throw new RemoteException("config is empty");
        startVpnInternal(context, inlineConfig, sCountry, userName);
    }

    static void startVpnInternal(Context context, String inlineConfig, String sCountry, String userName) throws RemoteException {
        ConfigParser cp = new ConfigParser();
        try {
            cp.parseConfig(new StringReader(inlineConfig));
            VpnProfile vp = cp.convertProfile();// Analysis.ovpn
            Log.d(TAG, "startVpnInternal: ==============" + cp + "\n" + vp);
            vp.mName = sCountry;
            if (vp.checkProfile(context) != de.blinkt.openvpn.R.string.no_error_found) {
                throw new RemoteException(context.getString(vp.checkProfile(context)));
            }
            vp.mProfileCreator = context.getPackageName();
            vp.mUsername = userName;
            Log.d("legend","===OpenVpnApi==startVpnInternal==onSuccess==vp.mName ==="+vp.mName+"==userName=="+userName );
           // vp.mPassword = pw;
            ProfileManager.setTemporaryProfile(context, vp);
            VPNLaunchHelper.startOpenVpn(vp, context);
        } catch (IOException | ConfigParser.ConfigParseError e) {
            throw new RemoteException(e.getMessage());
        }
    }

    public static void startVpnInternal(Context context, String inlineConfig, String sCountry, String userName, List<String> allowPkgs) throws RemoteException {
        ConfigParser cp = new ConfigParser();
        try {
            cp.parseConfig(new StringReader(inlineConfig));
            VpnProfile vp = cp.convertProfile();// Analysis.ovpn
            Log.d(TAG, "startVpnInternal: ==============" + cp + "\n" + vp);
            vp.mName = sCountry;
            if (vp.checkProfile(context) != de.blinkt.openvpn.R.string.no_error_found) {
                throw new RemoteException(context.getString(vp.checkProfile(context)));
            }
            vp.mProfileCreator = context.getPackageName();
            vp.mUsername = userName;
//            vp.mPassword = pw;
            if (allowPkgs.size() > 0) {
                vp.mAllowedAppsVpn.clear();
                vp.mAllowedAppsVpn.addAll(allowPkgs);
            }
            ProfileManager.setTemporaryProfile(context, vp);
            VPNLaunchHelper.startOpenVpn(vp, context);
        } catch (IOException | ConfigParser.ConfigParseError e) {
            throw new RemoteException(e.getMessage());
        }
    }
    public interface OnVpnProfileListener{
        void onCallBack(VpnProfile vp);
    }


}