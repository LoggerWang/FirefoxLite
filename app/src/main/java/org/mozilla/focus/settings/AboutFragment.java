package org.mozilla.focus.settings;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.anysitebrowser.tools.core.utils.AppStarter;
import com.anysitebrowser.tools.core.utils.ui.SafeToast;

import org.jetbrains.annotations.NotNull;
import org.mozilla.focus.R;
import org.mozilla.focus.utils.NoDoubleClickListener;
import org.mozilla.rocket.buriedpoint.BuriedPointUtil;
import org.mozilla.rocket.config.RemoteConfigHelper;
import org.mozilla.rocket.util.APKVersionInfoUtils;


/**
 * desc:
 */
public class AboutFragment extends Fragment {
    private LinearLayout itemContainer;
    private ImageView about_icon;
    private final int[] titleArrays = {R.string.me_update, R.string.me_privacy, R.string.me_terms_of_service};
    private TextView aboutVersion;

    public AboutFragment() {
    }

    private int countPP = 0;

    public static AboutFragment newInstance() {
        AboutFragment fragment = new AboutFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);
        itemContainer = rootView.findViewById(R.id.ll_item_container);
        about_icon = rootView.findViewById(R.id.about_icon);
        aboutVersion = rootView.findViewById(R.id.about_version);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        about_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoBackDoor();
            }
        });
        try {
            aboutVersion.setText("V"+getActivity().getApplication().getPackageManager().getPackageInfo(getActivity().getApplication().getPackageName(),0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < titleArrays.length; i++) {
            AboutItemView aboutItemView = new AboutItemView(requireContext());
            aboutItemView.setContent(0, titleArrays[i]);
            if (i == 0) {
                aboutItemView.setVersion();
            }
            itemContainer.addView(aboutItemView);
            int finalI = i;
            aboutItemView.setOnClickListener(new NoDoubleClickListener() {
                @Override
                public void onNoDoubleClick(View v) {
                    onItemClick(finalI);
                }
            });
        }
    }


    private void onItemClick(int pos) {
        switch (pos) {
            case 0:
                updateApk();
                break;
            case 1:
                BrowserActivity.launch(getActivity(), "http://truth-storage.anonymoussocial.network/legals/privacy.html");
                break;
            case 2:
                BrowserActivity.launch(getActivity(), "http://truth-storage.anonymoussocial.network/legals/terms.html");
                break;
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }


    private void updateApk() {
        long remoteVersionCode = RemoteConfigHelper.getUpdateVersion();
        int localVersionCode = APKVersionInfoUtils.getVersionCode(requireContext());
        if (remoteVersionCode > 0 && remoteVersionCode > localVersionCode) {
            //has new version
            BuriedPointUtil.addClick("/about/update/x","has_new_version", "true");
            AppStarter.startAppMarket(requireContext(), requireContext().getApplicationInfo().packageName, "", "update_user_check", false);
        } else {
            BuriedPointUtil.addClick("/about/update/x","has_new_version", "false");
            SafeToast.showToast(getResources().getString(R.string.setting_no_new_version), Toast.LENGTH_SHORT);
        }
    }

    private int mCurClickTimes = 0;
    private void  gotoBackDoor(){
        /*mCurClickTimes++;
        if (mCurClickTimes >= 5) {
            BackDoorActivity.launch(getActivity());
        } else {
            TaskHelper.exec(new TaskHelper.UITask() {
                @Override
                public void callback(Exception e) {
                    mCurClickTimes = 0;
                }
            }, 0, 2000);
        }*/
    }
}