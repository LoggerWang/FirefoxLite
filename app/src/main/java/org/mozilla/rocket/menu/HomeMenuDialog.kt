package org.mozilla.rocket.menu

import android.content.Context
import android.graphics.Outline
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.*
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import dagger.Lazy
import org.mozilla.fileutils.FileUtils
import org.mozilla.focus.R
import org.mozilla.focus.telemetry.TelemetryWrapper
import org.mozilla.focus.utils.FormatUtils
import org.mozilla.rocket.chrome.ChromeViewModel
import org.mozilla.rocket.chrome.MenuViewModel
import org.mozilla.rocket.content.appComponent
import org.mozilla.rocket.content.getActivityViewModel
import org.mozilla.rocket.extension.toFragmentActivity
import org.mozilla.rocket.nightmode.AdjustBrightnessDialog
import org.mozilla.rocket.shopping.search.ui.ShoppingSearchActivity
import org.mozilla.rocket.widget.LifecycleBottomSheetDialog
import javax.inject.Inject

class HomeMenuDialog : LifecycleBottomSheetDialog {

    @Inject
    lateinit var chromeViewModelCreator: Lazy<ChromeViewModel>
    @Inject
    lateinit var menuViewModelCreator: Lazy<MenuViewModel>

    private lateinit var chromeViewModel: ChromeViewModel
    private lateinit var menuViewModel: MenuViewModel

    private lateinit var rootView: View
    private lateinit var scroll_view: NestedScrollView
    private lateinit var img_screenshots: ImageView
    private lateinit var menu_screenshots: LinearLayout
    private lateinit var menu_bookmark: LinearLayout
    private lateinit var menu_history: LinearLayout
    private lateinit var menu_download: LinearLayout
    private lateinit var night_mode_switch: Switch
    private lateinit var content_services_switch: Switch
    private lateinit var btn_private_browsing: LinearLayout
    private lateinit var menu_smart_shopping_search: LinearLayout
    private lateinit var menu_night_mode: LinearLayout
    private lateinit var menu_content_services: LinearLayout
    private lateinit var menu_add_top_sites: LinearLayout
    private lateinit var menu_themes: LinearLayout
    private lateinit var img_private_mode: ImageView
    private lateinit var menu_preferences: LinearLayout
    private lateinit var menu_delete: LinearLayout
    private lateinit var content_services_red_dot: View
    private lateinit var add_top_sites_red_dot: View
    private lateinit var themes_red_dot: View
    private lateinit var menu_exit: LinearLayout
    private lateinit var menu_setting: LinearLayout
    private lateinit var menu_clean: LinearLayout

    private val uiHandler = Handler(Looper.getMainLooper())

    constructor(context: Context) : super(context)
    constructor(context: Context, @StyleRes theme: Int) : super(context, theme)

    override fun onCreate(savedInstanceState: Bundle?) {
        appComponent().inject(this)
        super.onCreate(savedInstanceState)
        context.toFragmentActivity().lifecycle.addObserver(this)
        chromeViewModel = getActivityViewModel(chromeViewModelCreator)
        menuViewModel = getActivityViewModel(menuViewModelCreator)

        initLayout()
        observeChromeAction()
        setCancelable(false)
        setCanceledOnTouchOutside(true)
    }

    override fun dismiss() {
        if (::rootView.isInitialized) {
            resetStates()
        }
        super.dismiss()
    }

    override fun onDetachedFromWindow() {
        uiHandler.removeCallbacksAndMessages(null)
        super.onDetachedFromWindow()
    }

    private fun resetStates() {
        scroll_view.fullScroll(ScrollView.FOCUS_UP)
        hideNewItemHint()
    }

    private fun initLayout() {
        rootView = layoutInflater.inflate(R.layout.bottom_sheet_home_menu, null)
        scroll_view = rootView.findViewById(R.id.scroll_view)
        img_screenshots = rootView.findViewById(R.id.img_screenshots)
        menu_screenshots = rootView.findViewById(R.id.menu_screenshots)
        menu_bookmark = rootView.findViewById(R.id.menu_bookmark)
        menu_download = rootView.findViewById(R.id.menu_download)
        night_mode_switch = rootView.findViewById(R.id.night_mode_switch)
        content_services_switch = rootView.findViewById(R.id.content_services_switch)
        btn_private_browsing = rootView.findViewById(R.id.btn_private_browsing)
        menu_smart_shopping_search = rootView.findViewById(R.id.menu_smart_shopping_search)
        menu_night_mode = rootView.findViewById(R.id.menu_night_mode)
        menu_content_services = rootView.findViewById(R.id.menu_content_services)
        menu_add_top_sites = rootView.findViewById(R.id.menu_add_top_sites)
        menu_themes = rootView.findViewById(R.id.menu_themes)
        img_private_mode = rootView.findViewById(R.id.img_private_mode)
        menu_preferences = rootView.findViewById(R.id.menu_preferences)
        menu_delete = rootView.findViewById(R.id.menu_delete)
        menu_exit = rootView.findViewById(R.id.menu_exit)
        content_services_red_dot = rootView.findViewById(R.id.content_services_red_dot)
        add_top_sites_red_dot = rootView.findViewById(R.id.add_top_sites_red_dot)
        themes_red_dot = rootView.findViewById(R.id.themes_red_dot)
        menu_history = rootView.findViewById(R.id.menu_history)
        menu_setting = rootView.findViewById(R.id.menu_setting)
        menu_clean = rootView.findViewById(R.id.menu_clean)
        scroll_view.apply {
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, resources.getDimension(R.dimen.menu_corner_radius))
                }
            }
            clipToOutline = true
        }
        initMenuTabs(rootView)
        initMenuItems(rootView)
        setContentView(rootView)
    }

    private fun initMenuTabs(contentLayout: View) {
        contentLayout.apply {
            chromeViewModel.hasUnreadScreenshot.observe(this@HomeMenuDialog, Observer {
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
                }
            }
            menu_history.setOnClickListener {
                postDelayClickEvent {
                    cancel()
                    chromeViewModel.showHistory.call()
                    TelemetryWrapper.clickMenuHistory()
                }
            }
            menu_download.setOnClickListener {
                postDelayClickEvent {
                    cancel()
                    chromeViewModel.showDownloadPanel.call()
                    TelemetryWrapper.clickMenuDownload()
                }
            }
            menu_setting.setOnClickListener {
                postDelayClickEvent {
                    cancel()
                    chromeViewModel.checkToDriveDefaultBrowser()
                    chromeViewModel.openPreference.call()
                    TelemetryWrapper.clickMenuSettings()
                }
            }
            menu_clean.setOnClickListener {
                postDelayClickEvent {
                    cancel()
                    onDeleteClicked()
                    TelemetryWrapper.clickMenuClearCache()
                }
            }
        }
    }

    private fun initMenuItems(contentLayout: View) {
        contentLayout.apply {
            chromeViewModel.isNightMode.observe(this@HomeMenuDialog, Observer { nightModeSettings ->
                night_mode_switch.isChecked = nightModeSettings.isEnabled
            })
            menuViewModel.isHomeScreenShoppingSearchEnabled.observe(this@HomeMenuDialog, Observer {
//                btn_private_browsing.isVisible = it
//                menu_smart_shopping_search.isVisible = !it
            })
            chromeViewModel.isPrivateBrowsingActive.observe(this@HomeMenuDialog, Observer {
                img_private_mode.isActivated = it
            })
            menuViewModel.shouldShowNewMenuItemHint.observe(this@HomeMenuDialog, Observer {
                if (it) {
                    showNewItemHint()
                    menuViewModel.onNewMenuItemDisplayed()
                }
            })
            menuViewModel.isContentHubEnabled.observe(this@HomeMenuDialog, Observer {
                if (content_services_switch.isChecked != it) {
                    content_services_switch.isChecked = it
                }
            })

            btn_private_browsing.setOnClickListener {
                postDelayClickEvent {
                    cancel()
                    chromeViewModel.togglePrivateMode.call()
                    TelemetryWrapper.togglePrivateMode(true)
                }
            }
            menu_smart_shopping_search.setOnClickListener {
                postDelayClickEvent {
                    cancel()
                    showShoppingSearch()
                }
            }
            menu_night_mode.setOnClickListener {
                chromeViewModel.adjustNightMode()
            }
            night_mode_switch.setOnCheckedChangeListener { _, isChecked ->
                val needToUpdate = isChecked != (chromeViewModel.isNightMode.value?.isEnabled == true)
                if (needToUpdate) {
                    chromeViewModel.onNightModeToggled()
                }
            }
            menu_content_services.setOnClickListener {
                content_services_switch.toggle()
            }
            content_services_switch.setOnCheckedChangeListener { _, isChecked ->
                menuViewModel.onContentHubSwitchToggled(isChecked)
                TelemetryWrapper.changeMenuVerticalToggle(isChecked)
            }
            menu_add_top_sites.setOnClickListener {
                postDelayClickEvent {
                    cancel()
                    chromeViewModel.onAddNewTopSiteMenuClicked()
                    TelemetryWrapper.clickMenuAddTopsite()
                }
            }
            menu_themes.setOnClickListener {
                postDelayClickEvent {
                    cancel()
                    chromeViewModel.onThemeSettingMenuClicked()
                    TelemetryWrapper.clickMenuTheme()
                }
            }
            menu_preferences.setOnClickListener {
                postDelayClickEvent {
                    cancel()
                    chromeViewModel.checkToDriveDefaultBrowser()
                    chromeViewModel.openPreference.call()
                    TelemetryWrapper.clickMenuSettings()
                }
            }
            menu_delete.setOnClickListener {
                postDelayClickEvent {
                    cancel()
                    onDeleteClicked()
                    TelemetryWrapper.clickMenuClearCache()
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

    private fun showNewItemHint() {
        content_services_red_dot.visibility = View.VISIBLE
       add_top_sites_red_dot.visibility = View.VISIBLE
        themes_red_dot.visibility = View.VISIBLE
    }

    private fun hideNewItemHint() {
        content_services_red_dot.visibility = View.INVISIBLE
        add_top_sites_red_dot.visibility = View.INVISIBLE
        themes_red_dot.visibility = View.INVISIBLE
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

    private fun showShoppingSearch() {
        val context: Context = this.context ?: return
        context.startActivity(ShoppingSearchActivity.getStartIntent(context))
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