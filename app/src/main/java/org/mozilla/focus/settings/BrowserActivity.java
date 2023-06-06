package org.mozilla.focus.settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.MimeTypeMap;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.anysitebrowser.base.core.log.Logger;
import com.anysitebrowser.base.core.utils.lang.StringUtils;
import com.anysitebrowser.tools.core.lang.ContentType;

import org.mozilla.focus.R;

import java.util.UUID;


public class BrowserActivity extends AppCompatActivity {
    private static final String TAG = "BrowserActivity";
    public static final String KEY_EXTRAS_PRE= "pve_pre";
    public static final String KEY_EXTRAS_WEB_TITLE = "web_title";
    public static final String KEY_EXTRAS_URL = "url";
    public static final String KEY_SHOW_OPTION = "opt";
    public static final String KEY_EXTRAS_THUMBNAIL = "thumbnail";
    public static final String KEY_EXTRAS_TYPE = "type";
    public static final String KEY_EXTRAS_MSG_RES = "msg_res";
    public static final String KEY_EXTRAS_MSG = "msg";
    public static final String KEY_EXTRAS_DES_RES = "des_res";
    public static final String KEY_EXTRAS_DES = "des";
    public static final String KEY_EXTRA_FB_CONTENT_URL_RES = "fb_content_url_res";
    public static final String KEY_EXTRA_FB_CONTENT_URL = "fb_content_url";

    public static final String CONTENT_DISPOSITION_PREFIX = "attachment;filename=";

    private ViewGroup mRootView;
    private View mCustomView;
    private FrameLayout mCustomViewContainer;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;

    protected MyWebView mWebView;
    private MyWebChromeClient mWebChromeClient;
    private MyWebViewClient mWebViewClient;

    private TextView mTitleText;
    private ImageView mLeftBtn;

    protected View mTitleArea;
    protected View mOptionArea;
    private View mErrorView;
    private View mBtnBack;
    private View mBtnForward;
    private View mBtnShare;
    private View mBtnRefresh;
    private View mBtnOpen;

    private ProgressBar mProgressBar;
    protected boolean mDownloadBySystem;
    protected boolean mShowOption;
    protected String mTypeStr;
    protected String mWebTitle;

    private long mTotalDuration = 0;
    private long mStartTime = 0;

    public static void launch(Activity activity, String jumpUrl) {
        if(activity == null) return;
        Intent intent = new Intent(activity, BrowserActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(BrowserActivity.KEY_EXTRAS_URL, jumpUrl);
        bundle.putString(BrowserActivity.KEY_EXTRAS_WEB_TITLE, "");
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }


    private void loadView() {
        String url = getIntent().getStringExtra(KEY_EXTRAS_URL);
        if (url == null)
            mWebView.loadUrl("");
        else
            mWebView.loadUrl(url);
    }

    public boolean inCustomView() {
        return (mCustomView != null);
    }

    public void hideCustomView() {
        mWebChromeClient.onHideCustomView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mWebView != null){
            mWebView.onPause();
        }
        pauseViewing();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browser_activity);
        initView();
    }


    protected void initView() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        setResult(RESULT_OK);

        if (Build.VERSION.SDK_INT >= 11)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        mRootView = findViewById(R.id.browser_root);
        mCustomViewContainer = (FrameLayout) findViewById(R.id.customViewContainer);
        mProgressBar = (ProgressBar) findViewById(R.id.pb);
        mProgressBar.setMax(100);

        mTitleArea = findViewById(R.id.common_titlebar);

//        if (NotificationBarUtil.isSupport())
//            mTitleArea.setPadding(0, Utils.getStatusBarHeight(this), 0, 0);

        mTitleText = (TextView) findViewById(R.id.title_text);
        mLeftBtn = (ImageView) findViewById(R.id.return_view);
        mLeftBtn.setOnClickListener(mOnClickListener);

        mOptionArea = findViewById(R.id.option_area);
        mBtnBack = findViewById(R.id.btn_back);
        mBtnBack.setOnClickListener(mOnClickListener);
        mBtnForward = findViewById(R.id.btn_forward);
        mBtnForward.setOnClickListener(mOnClickListener);
        mBtnRefresh = findViewById(R.id.btn_refresh);
        mBtnRefresh.setOnClickListener(mOnClickListener);
        mBtnShare = findViewById(R.id.btn_share);
        mBtnShare.setOnClickListener(mOnClickListener);
        mBtnOpen = findViewById(R.id.btn_open);
        mBtnOpen.setOnClickListener(mOnClickListener);

        mErrorView = findViewById(R.id.error_view);
        mErrorView.setOnClickListener(mOnClickListener);

        mShowOption = getIntent().getBooleanExtra(KEY_SHOW_OPTION, false);
        if (!mShowOption)
            mOptionArea.setVisibility(View.GONE);
        if (getIntent().hasExtra(KEY_EXTRAS_TYPE))
            mTypeStr = getIntent().getStringExtra(KEY_EXTRAS_TYPE);
        if (getIntent().hasExtra(KEY_EXTRAS_WEB_TITLE))
            mWebTitle = getIntent().getStringExtra(KEY_EXTRAS_WEB_TITLE);
        startViewing();
        mWebView = (MyWebView) findViewById(R.id.webView);
        mWebViewClient = new MyWebViewClient();
        mWebView.setWebViewClient(mWebViewClient);
        mWebChromeClient = new MyWebChromeClient();
        mWebView.setWebChromeClient(mWebChromeClient);
        mWebView.setDownloadListener(mDownloadListener);

        try {
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.getSettings().setPluginState(PluginState.ON);
            mWebView.getSettings().setAppCacheEnabled(true);
            //解决Cache/Code Cache/js: No such file or directory
            mWebView.getSettings().setAppCachePath(this.getCacheDir().getAbsolutePath());
            mWebView.getSettings().setBuiltInZoomControls(false);
            mWebView.getSettings().setSaveFormData(true);
            mWebView.getSettings().setBlockNetworkImage(true);
            //(在webview加载页面之前，设置加载模式为MIXED_CONTENT_ALWAYS_ALLOW)
            // 允许从任何来源加载内容，即使起源是不安全的
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            }

            mWebView.removeJavascriptInterface("searchBoxJavaBridge_");
            mWebView.removeJavascriptInterface("accessibility");
            mWebView.removeJavascriptInterface("accessibilityTraversal");
        } catch (Exception e) {
            Logger.e(TAG, "WebSettings error " + e.toString());
        }

        if (!TextUtils.isEmpty(mWebTitle))
            mTitleText.setText(mWebTitle);
        loadView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mWebView != null){
            mWebView.onResume();
        }
        startViewing();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (inCustomView()) {
            hideCustomView();
        }
    }

    @Override
    protected void onDestroy() {
        if (mWebView == null)
            return;
        ViewParent parent = mWebView.getParent();
        if (parent != null) {
            ((ViewGroup) parent).removeView(mWebView);
        }
        mWebView.stopLoading();
        // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
        mWebView.getSettings().setJavaScriptEnabled(false);
        mWebView.clearHistory();

        mWebView.removeAllViews();
        mWebView.setVisibility(View.GONE);
        mWebView.destroy();
        pauseViewing();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (inCustomView()) {
                hideCustomView();
                return true;
            }

            if (goBack())
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (mTypeStr != null && mTypeStr.equals(ContentType.VIDEO.toString())) {
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mTitleArea.setVisibility(View.GONE);
                mOptionArea.setVisibility(View.GONE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            } else {
                mTitleArea.setVisibility(View.VISIBLE);
                if (mShowOption)
                    mOptionArea.setVisibility(View.VISIBLE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            }
        }

        super.onConfigurationChanged(newConfig);
    }

    private void setFullScreen(boolean fullScreen) {
        if (fullScreen) {
            mTitleArea.setVisibility(View.GONE);
            mOptionArea.setVisibility(View.GONE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            mTitleArea.setVisibility(View.VISIBLE);
            if (mShowOption)
                mOptionArea.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            if(R.id.btn_back == id) {
                goBack();
            } else if(R.id.btn_forward == id) {
                goForward();
            } else if(R.id.btn_share == id) {
                openShareDialog();
            } else if(R.id.btn_refresh == id) {
                mWebView.reload();
            } else if(R.id.btn_open == id) {
                openByBrowser();
            } else if(R.id.return_view == id) {
                finish();
            } else if(R.id.error_view == id) {
                mErrorView.setVisibility(View.GONE);
                loadView();
            }
        }
    };

    @Override
    public void finish() {
        Logger.v(TAG, this.getClass().getSimpleName() + ".finish()");
        Intent intent = new Intent();
        intent.putExtra("duration", mTotalDuration);
        this.setResult(RESULT_OK, intent);
        super.finish();
    }

    ;

    private boolean goBack() {
        if ((mCustomView == null) && mWebView.canGoBack() && mErrorView.getVisibility()==View.GONE) {
            mWebView.goBack();
            return true;
        }

        return false;
    }

    private boolean goForward() {
        if ((mCustomView == null) && mWebView.canGoForward()) {
            mWebView.goForward();
            return true;
        }

        return false;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private class MyWebChromeClient extends WebChromeClient {
        private View mVideoProgressView;

        @Override
        public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
            Logger.d(TAG, "onShowCustomView, requestedOrientation : " + requestedOrientation);
            onShowCustomView(view, callback);
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            // if a view already exists then immediately terminate the new one
            if (mCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }
            mCustomView = view;
            mWebView.setVisibility(View.GONE);
            mCustomViewContainer.setVisibility(View.VISIBLE);
            mCustomViewContainer.addView(view);
            mCustomViewCallback = callback;

            // setFullScreen(true);
        }

        @Override
        public View getVideoLoadingProgressView() {
            if (mVideoProgressView == null) {
                LayoutInflater inflater = LayoutInflater.from(BrowserActivity.this);
                mVideoProgressView = inflater.inflate(R.layout.browser_video_progress, null);
            }
            return mVideoProgressView;
        }

        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
            if (mCustomView == null)
                return;

            mWebView.setVisibility(View.VISIBLE);
            mCustomViewContainer.setVisibility(View.GONE);

            // Hide the custom view.
            mCustomView.setVisibility(View.GONE);

            // Remove the custom view from its container.
            mCustomViewContainer.removeView(mCustomView);
            mCustomViewCallback.onCustomViewHidden();

            mCustomView = null;

            // setFullScreen(false);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            if (TextUtils.isEmpty(mWebTitle))
                mTitleText.setText(title);
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            mProgressBar.setProgress(newProgress);
            if (newProgress == 100) {
                mProgressBar.setVisibility(View.GONE);
            }
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public Bitmap getDefaultVideoPoster() {
            return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Logger.d("BrowserActivity","==onPageFinished==url="+url);
            mProgressBar.setVisibility(View.GONE);
            //以下  用来解决加载https的url和里面含有http资源不兼容的情况
            mWebView.getSettings().setBlockNetworkImage(false);//解除阻塞
            //判断webview是否加载了，图片资源
            if (!mWebView.getSettings().getLoadsImagesAutomatically()) {
                mWebView.getSettings().setLoadsImagesAutomatically(true); //设置wenView加载图片资源
            }
            webviewLoadSuccessshowAd();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Logger.d("BrowserActivity","==shouldOverrideUrlLoading==url="+url);
            // fix youku intent error bug
            if (url.startsWith("intent://download") || url.startsWith("intent://play"))
                return true;
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Logger.d("BrowserActivity","==onReceivedError==errorCode="+errorCode+"==description=="+description+"==failingUrl=="+failingUrl);
            mErrorView.setVisibility(View.VISIBLE);
            view.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            super.onReceivedError(view, errorCode, description, failingUrl);
        }
    }

    protected void openShareDialog() {
        /*Bundle args = new Bundle();
        String url = mWebView.getUrl();
        Bitmap thumbnail = getIntent().getParcelableExtra(KEY_EXTRAS_THUMBNAIL);
        args.putString(SocialShareData.EXTRA_TITLE, mWebView.getTitle());
        args.putString(SocialShareData.EXTRA_DESCRIPTION, getExtraDescription());
        args.putString(SocialShareData.EXTRA_MSG, getExtraMsg());
        args.putString(SocialShareData.EXTRA_WEBPAGE, url);
        args.putParcelable(SocialShareData.EXTRA_THUMBNAIL, thumbnail);

        ShareDialogFragment mShareDialog = new ShareDialogFragment();
        mShareDialog.setArguments(args);
        mShareDialog.show(getSupportFragmentManager(), "share");*/
    }

    @SuppressLint("StringFormatInvalid")
    private String getExtraDescription() {
        try {
            Intent intent = getIntent();
            if (intent.hasExtra(KEY_EXTRAS_DES_RES)) {
                int desRes = intent.getIntExtra(KEY_EXTRAS_DES_RES, 0);
                return getString(desRes, mWebView.getTitle(), mWebView.getUrl());
            } else if (intent.hasExtra(KEY_EXTRAS_DES))
                return intent.getStringExtra(KEY_EXTRAS_DES);
        } catch (Exception e) {
        }

        return getString(R.string.browser_app, mWebView.getTitle(), mWebView.getUrl());
    }

    @SuppressLint("StringFormatInvalid")
    private String getExtraMsg() {
        try {
            Intent intent = getIntent();
            if (intent.hasExtra(KEY_EXTRAS_MSG_RES)) {
                int msgRes = intent.getIntExtra(KEY_EXTRAS_MSG_RES, 0);
                return getString(msgRes, mWebView.getTitle(), mWebView.getUrl());
            } else if (intent.hasExtra(KEY_EXTRAS_MSG))
                return intent.getStringExtra(KEY_EXTRAS_MSG);
        } catch (Exception e) {
        }

        return getString(R.string.browser_app, mWebView.getTitle(), mWebView.getUrl());
    }

    private void openByBrowser() {
        String url = getIntent().getStringExtra(KEY_EXTRAS_URL);
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(uri);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void startDownloadByThirdApp(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private DownloadListener mDownloadListener = new DownloadListener() {

        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.FROYO
                    || ((url.contains("https://") && Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB))) {
                startDownloadByThirdApp(url);
                return;
            }

            // only support after Build.VERSION_CODES.GINGERBREAD
            DownloadManager manager = (DownloadManager) BrowserActivity.this.getSystemService(Context.DOWNLOAD_SERVICE);
            Request request = new Request(Uri.parse(url));
            String fileName = getFileName(contentDisposition);
            if (StringUtils.isBlank(fileName))
                fileName = UUID.randomUUID().toString() + "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(mimetype);
            try {
                // It seems that switch main storage couldn't apply immediately which lead to Exception
                if (fileName != null)
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                request.setVisibleInDownloadsUi(true);
                manager.enqueue(request);
                mDownloadBySystem = true;

                Toast.makeText(getApplicationContext(), "Downloading", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Logger.d(TAG, "onDownloadStart exception, try to download use browser:" + e.toString());
                startDownloadByThirdApp(url);
            }
        }
    };

    private String getFileName(String contentDisposition) {
        if (contentDisposition == null)
            return "";
        int index = contentDisposition.lastIndexOf(CONTENT_DISPOSITION_PREFIX);
        return index < 0 ? contentDisposition : contentDisposition.substring(index + CONTENT_DISPOSITION_PREFIX.length());
    }

    private void startViewing() {
        if (mStartTime != 0)
            return;
        mStartTime = System.currentTimeMillis();
    }

    private void pauseViewing() {
        if (mStartTime == 0)
            return;
        mTotalDuration += (System.currentTimeMillis() - mStartTime);
        mStartTime = 0;
    }


    public void webviewLoadSuccessshowAd() {
    }
}
