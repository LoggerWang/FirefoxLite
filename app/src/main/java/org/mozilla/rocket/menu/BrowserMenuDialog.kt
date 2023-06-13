package org.mozilla.rocket.menu

import android.content.Context
import android.content.Intent
import android.graphics.Outline
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.*
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import dagger.Lazy
import org.mozilla.fileutils.FileUtils
import org.mozilla.focus.R
import org.mozilla.focus.activity.SetVpnActivity
import org.mozilla.focus.telemetry.TelemetryWrapper
import org.mozilla.focus.utils.FormatUtils
import org.mozilla.rocket.buriedpoint.BuriedPointUtil
import org.mozilla.rocket.chrome.BottomBarItemAdapter
import org.mozilla.rocket.chrome.ChromeViewModel
import org.mozilla.rocket.chrome.MenuViewModel
import org.mozilla.rocket.content.appComponent
import org.mozilla.rocket.content.getActivityViewModel
import org.mozilla.rocket.content.view.BottomBar
import org.mozilla.rocket.extension.nonNullObserve
import org.mozilla.rocket.extension.switchFrom
import org.mozilla.rocket.nightmode.AdjustBrightnessDialog
import org.mozilla.rocket.widget.LifecycleBottomSheetDialog
import javax.inject.Inject

class BrowserMenuDialog : LifecycleBottomSheetDialog {

    @Inject
    lateinit var chromeViewModelCreator: Lazy<ChromeViewModel>
    @Inject
    lateinit var menuViewModelCreator: Lazy<MenuViewModel>

    private lateinit var menuViewModel: MenuViewModel
    private lateinit var chromeViewModel: ChromeViewModel
    private lateinit var bottomBarItemAdapter: BottomBarItemAdapter

    private lateinit var rootView: View
    private lateinit var img_screenshots: ImageView
    private lateinit var scroll_view: NestedScrollView
    private lateinit var menu_screenshots: LinearLayout
    private lateinit var menu_bookmark: LinearLayout
    private lateinit var menu_history: LinearLayout
    private lateinit var menu_download: LinearLayout
    private lateinit var turbomode_switch: Switch
    private lateinit var block_images_switch: Switch
    private lateinit var night_mode_switch: Switch
    private lateinit var menu_find_in_page: LinearLayout
    private lateinit var menu_pin_shortcut: LinearLayout
    private lateinit var menu_night_mode: LinearLayout
    private lateinit var menu_turbomode: LinearLayout
    private lateinit var menu_blockimg: LinearLayout
    private lateinit var menu_preferences: LinearLayout
    private lateinit var menu_delete: LinearLayout
    private lateinit var menu_exit: LinearLayout
    private lateinit var menu_vpn: LinearLayout
    private lateinit var menu_add_mark: LinearLayout
    private lateinit var iv_add_to_mark: ImageView

    private val uiHandler = Handler(Looper.getMainLooper())

    constructor(context: Context) : super(context)
    constructor(context: Context, @StyleRes theme: Int) : super(context, theme)

    override fun onCreate(savedInstanceState: Bundle?) {
        appComponent().inject(this)
        super.onCreate(savedInstanceState)
        chromeViewModel = getActivityViewModel(chromeViewModelCreator)
        menuViewModel = getActivityViewModel(menuViewModelCreator)

        initLayout()
        observeChromeAction()
        setCancelable(false)
        setCanceledOnTouchOutside(true)
    }

    override fun dismiss() {
        if (::rootView.isInitialized) {
            scroll_view.fullScroll(ScrollView.FOCUS_UP)
        }
        super.dismiss()
    }

    override fun onDetachedFromWindow() {
        uiHandler.removeCallbacksAndMessages(null)
        super.onDetachedFromWindow()
    }

    private fun initLayout() {
        rootView = layoutInflater.inflate(R.layout.bottom_sheet_browser_menu, null)
        rootView.findViewById<LinearLayout>(R.id.content_layout).apply {
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, resources.getDimension(R.dimen.menu_corner_radius))
                }
            }
            clipToOutline = true
        }
        img_screenshots = rootView.findViewById(R.id.img_screenshots)
        scroll_view = rootView.findViewById(R.id.scroll_view)
        menu_screenshots = rootView.findViewById(R.id.menu_screenshots)
        menu_bookmark = rootView.findViewById(R.id.menu_bookmark)
        menu_download = rootView.findViewById(R.id.menu_download)
        turbomode_switch = rootView.findViewById(R.id.turbomode_switch)
        block_images_switch = rootView.findViewById(R.id.block_images_switch)
        night_mode_switch = rootView.findViewById(R.id.night_mode_switch)
        menu_find_in_page = rootView.findViewById(R.id.menu_find_in_page)
        menu_pin_shortcut = rootView.findViewById(R.id.menu_pin_shortcut)
        menu_night_mode = rootView.findViewById(R.id.menu_night_mode)
        menu_turbomode = rootView.findViewById(R.id.menu_turbomode)
        menu_blockimg = rootView.findViewById(R.id.menu_blockimg)
        menu_preferences = rootView.findViewById(R.id.menu_preferences)
        menu_delete = rootView.findViewById(R.id.menu_delete)
        menu_exit = rootView.findViewById(R.id.menu_exit)
        menu_history = rootView.findViewById(R.id.menu_history)
        menu_vpn = rootView.findViewById(R.id.menu_vpn)
        menu_add_mark = rootView.findViewById(R.id.menu_add_mark)
        iv_add_to_mark = rootView.findViewById(R.id.iv_add_to_mark)
        initMenuTabs(rootView)
        initMenuItems(rootView)
        initBottomBar()
        setContentView(rootView)
    }

    private fun initMenuTabs(contentLayout: View) {
        contentLayout.apply {
            chromeViewModel.hasUnreadScreenshot.observe(this@BrowserMenuDialog, Observer {
                img_screenshots.isActivated = it
            })

            menu_screenshots.setOnClickListener {
                postDelayClickEvent {
                    cancel()
                    chromeViewModel.showScreenshots()
                }
            }
            menu_bookmark.setOnClickListener {
                postDelayClickEvent {
                    cancel()
                    chromeViewModel.showBookmarks.call()
                    TelemetryWrapper.clickMenuBookmark()
                    BuriedPointUtil.addClick("/toolbar/more/bookmarks")
                }
            }
            menu_history.setOnClickListener {
                postDelayClickEvent {
                    cancel()
                    chromeViewModel.showHistory.call()
                    TelemetryWrapper.clickMenuHistory()
                    BuriedPointUtil.addClick("/toolbar/more/historys")
                }
            }
            menu_download.setOnClickListener {
                postDelayClickEvent {
                    cancel()
                    chromeViewModel.showDownloadPanel.call()
                    TelemetryWrapper.clickMenuDownload()
                    BuriedPointUtil.addClick("/toolbar/more/downloads")
                }
            }
            menu_vpn.setOnClickListener {
                postDelayClickEvent {
                    cancel()
                    val intent = Intent(context, SetVpnActivity::class.java)
                    context.startActivity(intent)
                    BuriedPointUtil.addClick("/toolbar/more/vpn")
                }
            }
            menu_add_mark.setOnClickListener {
                postDelayClickEvent {
                    cancel()
                    val isActivated = bottomBarItemAdapter.getItem(BottomBarItemAdapter.TYPE_BOOKMARK)?.view?.isActivated == true
                    TelemetryWrapper.clickToolbarBookmark(!isActivated, TelemetryWrapper.Extra_Value.MENU, 0)
                    chromeViewModel.toggleBookmark()
                    BuriedPointUtil.addClick("/toolbar/more/addto_bookmarks")
                }

            }
        }
    }

    private fun initMenuItems(contentLayout: View) {
        contentLayout.apply {
            chromeViewModel.isTurboModeEnabled.observe(this@BrowserMenuDialog, Observer {
                turbomode_switch.isChecked = it
            })
            chromeViewModel.isBlockImageEnabled.observe(this@BrowserMenuDialog, Observer {
                block_images_switch.isChecked = it
            })
            chromeViewModel.isNightMode.observe(this@BrowserMenuDialog, Observer { nightModeSettings ->
                night_mode_switch.isChecked = nightModeSettings.isEnabled
            })

            menu_find_in_page.setOnClickListener {
                postDelayClickEvent {
                    cancel()
                    chromeViewModel.showFindInPage.call()
                    BuriedPointUtil.addClick("/toolbar/more/findin_page")
                }
            }
            menu_pin_shortcut.setOnClickListener {
                postDelayClickEvent {
                    cancel()
                    chromeViewModel.pinShortcut.call()
                    TelemetryWrapper.clickMenuPinShortcut()
                }
            }
            menu_night_mode.setOnClickListener {
                chromeViewModel.adjustNightMode()
            }
            menu_turbomode.setOnClickListener { turbomode_switch.toggle() }
            turbomode_switch.setOnCheckedChangeListener { _, isChecked ->
                val needToUpdate = isChecked != (chromeViewModel.isTurboModeEnabled.value == true)
                if (needToUpdate) {
                    chromeViewModel.onTurboModeToggled()
                }
            }
            menu_blockimg.setOnClickListener { block_images_switch.toggle() }
            block_images_switch.setOnCheckedChangeListener { _, isChecked ->
                val needToUpdate = isChecked != (chromeViewModel.isBlockImageEnabled.value == true)
                if (needToUpdate) {
                    chromeViewModel.onBlockImageToggled()
                }
            }
            night_mode_switch.setOnCheckedChangeListener { _, isChecked ->
                val needToUpdate = isChecked != (chromeViewModel.isNightMode.value?.isEnabled == true)
                if (needToUpdate) {
                    chromeViewModel.onNightModeToggled()
                }
            }
            menu_preferences.setOnClickListener {
                postDelayClickEvent {
                    cancel()
                    chromeViewModel.checkToDriveDefaultBrowser()
                    chromeViewModel.openPreference.call()
                    TelemetryWrapper.clickMenuSettings()
                    BuriedPointUtil.addClick("/toolbar/more/settings")
                }
            }
            menu_delete.setOnClickListener {
                postDelayClickEvent {
                    cancel()
                    onDeleteClicked()
                    TelemetryWrapper.clickMenuClearCache()
                    BuriedPointUtil.addClick("/toolbar/more/clear_cache")
                }
            }
            menu_exit.setOnClickListener {
                postDelayClickEvent {
                    cancel()
                    chromeViewModel.exitApp.call()
                    TelemetryWrapper.clickMenuExit()
                }
            }
        }
    }

    private fun onDeleteClicked() {
        val diff = FileUtils.clearCache(context)
        val stringId = if (diff < 0) R.string.message_clear_cache_fail else R.string.message_cleared_cached
        val msg = context.getString(stringId, FormatUtils.getReadableStringFromFileSize(diff))
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    private fun observeChromeAction() {
        chromeViewModel.showAdjustBrightness.observe(this, Observer { showAdjustBrightness() })
    }

    private fun showAdjustBrightness() {
        ContextCompat.startActivity(context, AdjustBrightnessDialog.Intents.getStartIntentFromMenu(context), null)
    }

    private fun initBottomBar() {
        //底部功能按钮 已隐藏
        val bottomBar = rootView.findViewById<BottomBar>(R.id.menu_bottom_bar)
        bottomBar.setOnItemClickListener(object : BottomBar.OnItemClickListener {
            override fun onItemClick(type: Int, position: Int) {
                cancel()
                when (type) {
                    BottomBarItemAdapter.TYPE_TAB_COUNTER -> {
                        chromeViewModel.showTabTray.call()
                        TelemetryWrapper.showTabTrayToolbar(TelemetryWrapper.Extra_Value.MENU, position)
                    }
                    BottomBarItemAdapter.TYPE_MENU -> {
                        chromeViewModel.showBrowserMenu.call()
                        TelemetryWrapper.showMenuToolbar(TelemetryWrapper.Extra_Value.MENU, position)
                    }
                    BottomBarItemAdapter.TYPE_HOME -> {
                        chromeViewModel.showNewTab.call()
                        TelemetryWrapper.clickAddTabToolbar(TelemetryWrapper.Extra_Value.MENU, position)
                    }
                    BottomBarItemAdapter.TYPE_SEARCH -> {
                        chromeViewModel.showUrlInput.call()
                        TelemetryWrapper.clickToolbarSearch(TelemetryWrapper.Extra_Value.MENU, position)
                    }
                    BottomBarItemAdapter.TYPE_CAPTURE -> chromeViewModel.onDoScreenshot(ChromeViewModel.ScreenCaptureTelemetryData(TelemetryWrapper.Extra_Value.MENU, position))
                    BottomBarItemAdapter.TYPE_PIN_SHORTCUT -> {
                        chromeViewModel.pinShortcut.call()
                        TelemetryWrapper.clickAddToHome(TelemetryWrapper.Extra_Value.MENU, position)
                    }
                    BottomBarItemAdapter.TYPE_BOOKMARK -> {
                        val isActivated = bottomBarItemAdapter.getItem(BottomBarItemAdapter.TYPE_BOOKMARK)?.view?.isActivated == true
                        TelemetryWrapper.clickToolbarBookmark(!isActivated, TelemetryWrapper.Extra_Value.MENU, position)
                        chromeViewModel.toggleBookmark()
                    }
                    BottomBarItemAdapter.TYPE_REFRESH -> {
                        chromeViewModel.refreshOrStop.call()
                        TelemetryWrapper.clickToolbarReload(TelemetryWrapper.Extra_Value.MENU, position)
                    }
                    BottomBarItemAdapter.TYPE_SHARE -> {
                        chromeViewModel.share.call()
                        TelemetryWrapper.clickToolbarShare(TelemetryWrapper.Extra_Value.MENU, position)
                    }
                    BottomBarItemAdapter.TYPE_NEXT -> {
                        chromeViewModel.goNext.call()
                        TelemetryWrapper.clickToolbarForward(TelemetryWrapper.Extra_Value.MENU, position)
                    }
                    BottomBarItemAdapter.TYPE_BACK -> {
                        chromeViewModel.goBack.call()
                        TelemetryWrapper.clickToolbarBack(position)
                    }
                    else -> throw IllegalArgumentException("Unhandled bottom bar item, type: $type")
                } // move Telemetry to ScreenCaptureTask doInBackground() cause we need to init category first.
            }
        })
        bottomBarItemAdapter = BottomBarItemAdapter(bottomBar, BottomBarItemAdapter.Theme.Light)
        menuViewModel.bottomItems.nonNullObserve(this) { bottomItems ->
            bottomBarItemAdapter.setItems(bottomItems)
            hidePinShortcutButtonIfNotSupported()
        }

        chromeViewModel.tabCount.switchFrom(menuViewModel.bottomItems)
                .observe(this, Observer { bottomBarItemAdapter.setTabCount(it ?: 0) })
        chromeViewModel.isRefreshing.switchFrom(menuViewModel.bottomItems)
                .observe(this, Observer { bottomBarItemAdapter.setRefreshing(it == true) })
        chromeViewModel.canGoForward.switchFrom(menuViewModel.bottomItems)
                .observe(this, Observer { bottomBarItemAdapter.setCanGoForward(it == true) })
        chromeViewModel.canGoBack.switchFrom(menuViewModel.bottomItems)
                .observe(this, Observer { bottomBarItemAdapter.setCanGoBack(it == true) })
        chromeViewModel.isCurrentUrlBookmarked.switchFrom(menuViewModel.bottomItems)
                .observe(this, Observer {
                    bottomBarItemAdapter.setBookmark(it == true)
                    //添加到书签
                    DrawableCompat.setTint(iv_add_to_mark.drawable, context.resources.getColor(if (it)R.color.paletteDarkBlueC100 else R.color.paletteBlack100))
                })
    }

    private fun hidePinShortcutButtonIfNotSupported() {
        val requestPinShortcutSupported = ShortcutManagerCompat.isRequestPinShortcutSupported(context)
        if (!requestPinShortcutSupported) {
            val pinShortcutItem = bottomBarItemAdapter.getItem(BottomBarItemAdapter.TYPE_PIN_SHORTCUT)
            pinShortcutItem?.view?.apply {
                visibility = View.GONE
            }
        }
    }

    /**
     * Post delay click event to wait the clicking feedback shows
     */
    private fun postDelayClickEvent(action: () -> Unit) {
        uiHandler.postDelayed({
            action()
        }, 150)
    }
}