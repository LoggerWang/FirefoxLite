package org.mozilla.rocket.update;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.mozilla.focus.R;

/**
 *
 * desc:
 */
public class UpdateDialog extends Dialog {

    private TextView tvTitle, tvVersionName;
    private View btConfim, btCancel;
    private String title, versionName, msg;
    private OnClickedListener listener;
    private boolean unCancelAble;
    private boolean isForceUpdate;
    private View unForceUpdateBts;
    private LinearLayout llUpdateMsgs;


    public UpdateDialog(@NonNull Context context, String title, String versionName, String msg, OnClickedListener listener, boolean forceUpdate) {
        super(context);
        this.title = title;
        this.versionName = versionName;
        this.msg = msg;
        this.listener = listener;
        isForceUpdate = forceUpdate;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_update_dialog);
        Window window = getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);
        window.setBackgroundDrawableResource(R.color.transparent);
        initView();
    }

    private void initView(){
        tvTitle = findViewById(R.id.tv_title);
        tvVersionName = findViewById(R.id.tv_version_name);
        llUpdateMsgs = findViewById(R.id.update_message);
        unForceUpdateBts = findViewById(R.id.update_bts);
        if(isForceUpdate){
            unForceUpdateBts.setVisibility(View.GONE);
            btConfim = findViewById(R.id.tv_ok2);
            btConfim.setVisibility(View.VISIBLE);
        } else {
            unForceUpdateBts.setVisibility(View.VISIBLE);
            findViewById(R.id.tv_ok2).setVisibility(View.GONE);
            btConfim = findViewById(R.id.tv_ok);
            btCancel = findViewById(R.id.tv_cancel);
            btCancel.setOnClickListener(v -> {
                if (!unCancelAble) {
                    if (listener != null) {
                        listener.onCancel();
                    }
                    dismiss();
                }

            });
        }
        if(msg != null && msg.length()>0){
            String[] msgs = msg.split("@@");
            int size = msgs.length;
            if(size >0){
                for (int i = 0; i < size; i++){
                    UpdateItemView itemView = new UpdateItemView(getContext());
                    String indexStr = i+1+".";
                    itemView.setContent(indexStr,msgs[i]);
                    llUpdateMsgs.addView(itemView);
                }
            }
        }

        tvTitle.setText(title);
        if(TextUtils.isEmpty(versionName)){
            tvVersionName.setVisibility(View.GONE);
        } else {
            tvVersionName.setText(versionName);
        }
        //tvMsg.setText(msg);
        btConfim.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConfirm();
            }
            if (!unCancelAble) {
                dismiss();
            }
        });
        setUnCancelAble(isForceUpdate);
    }

    /**
     * 设置弹窗不可点击取消，只能点击右侧确定按钮
     *
     * @param unCancelAble
     */
    private void setUnCancelAble(boolean unCancelAble) {
        setCanceledOnTouchOutside(!unCancelAble);
        setCancelable(!unCancelAble);
        this.unCancelAble = unCancelAble;
    }

    public interface OnClickedListener {
        void onConfirm();

        void onCancel();
    }


}
