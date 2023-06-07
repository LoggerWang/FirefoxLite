package org.mozilla.rocket.home

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.anysitebrowser.base.core.log.Logger
import com.anysitebrowser.base.core.utils.app.AppDist
import com.anysitebrowser.base.core.utils.device.DeviceHelper
import com.anysitebrowser.base.core.utils.lang.ObjectStore
import com.anysitebrowser.tools.core.utils.Utils
import com.google.android.material.snackbar.Snackbar
import com.suke.widget.SwitchButton
import dagger.Lazy
import de.blinkt.openvpn.OpenVpnApi
import de.blinkt.openvpn.VpnHelper
import de.blinkt.openvpn.model.ZoneBean
import de.blinkt.openvpn.utils.ConnectState
import de.blinkt.openvpn.utils.ProxyModeEnum
import de.blinkt.openvpn.utils.Settings
import org.mozilla.focus.R
import org.mozilla.focus.activity.MainActivity
import org.mozilla.focus.activity.SetVpnActivity
import org.mozilla.focus.locale.LocaleAwareFragment
import org.mozilla.focus.navigation.ScreenNavigator
import org.mozilla.focus.tabs.TabCounter
import org.mozilla.focus.telemetry.TelemetryWrapper
import org.mozilla.focus.utils.DialogUtils
import org.mozilla.focus.utils.FirebaseHelper
import org.mozilla.focus.utils.FirebaseHelper.stopAndClose
import org.mozilla.focus.utils.ViewUtils
import org.mozilla.rocket.adapter.AdapterDelegatesManager
import org.mozilla.rocket.adapter.DelegateAdapter
import org.mozilla.rocket.buriedpoint.BuriedPointUtil
import org.mozilla.rocket.chrome.ChromeViewModel
import org.mozilla.rocket.component.RocketLauncherActivity
import org.mozilla.rocket.config.RemoteConfigHelper
import org.mozilla.rocket.content.appComponent
import org.mozilla.rocket.content.ecommerce.ui.ShoppingActivity
import org.mozilla.rocket.content.game.ui.GameActivity
import org.mozilla.rocket.content.getActivityViewModel
import org.mozilla.rocket.content.news.ui.NewsActivity
import org.mozilla.rocket.content.travel.ui.TravelActivity
import org.mozilla.rocket.download.DownloadIndicatorViewModel
import org.mozilla.rocket.extension.showFxToast
import org.mozilla.rocket.extension.switchMap
import org.mozilla.rocket.fxa.ProfileActivity
import org.mozilla.rocket.home.contenthub.ui.ContentHub
import org.mozilla.rocket.home.logoman.ui.LogoManNotification
import org.mozilla.rocket.home.topsites.domain.PinTopSiteUseCase
import org.mozilla.rocket.home.topsites.ui.*
import org.mozilla.rocket.home.topsites.ui.SiteViewHolder.Companion.TOP_SITE_LONG_CLICK_TARGET
import org.mozilla.rocket.home.ui.HomeScreenBackground
import org.mozilla.rocket.home.ui.MenuButton
import org.mozilla.rocket.home.ui.MenuButton.Companion.DOWNLOAD_STATE_DEFAULT
import org.mozilla.rocket.home.ui.MenuButton.Companion.DOWNLOAD_STATE_DOWNLOADING
import org.mozilla.rocket.home.ui.MenuButton.Companion.DOWNLOAD_STATE_UNREAD
import org.mozilla.rocket.home.ui.MenuButton.Companion.DOWNLOAD_STATE_WARNING
import org.mozilla.rocket.msrp.data.Mission
import org.mozilla.rocket.msrp.ui.RewardActivity
import org.mozilla.rocket.nightmode.themed.ThemedImageView
import org.mozilla.rocket.nightmode.themed.ThemedLinearLayout
import org.mozilla.rocket.nightmode.themed.ThemedTextView
import org.mozilla.rocket.nightmode.themed.ThemedView
import org.mozilla.rocket.settings.defaultbrowser.ui.DefaultBrowserHelper
import org.mozilla.rocket.settings.defaultbrowser.ui.DefaultBrowserPreferenceViewModel
import org.mozilla.rocket.shopping.search.ui.ShoppingSearchActivity
import org.mozilla.rocket.theme.ThemeManager
import org.mozilla.rocket.update.UpdateDialog.OnClickedListener
import org.mozilla.rocket.util.ResourceUtils.getVisibility
import org.mozilla.rocket.util.ToastMessage
import org.mozilla.rocket.util.setCurrentItem
import javax.inject.Inject

class HomeFragment : LocaleAwareFragment(), ScreenNavigator.HomeScreen {

    @Inject
    lateinit var homeViewModelCreator: Lazy<HomeViewModel>

    @Inject
    lateinit var chromeViewModelCreator: Lazy<ChromeViewModel>

    @Inject
    lateinit var downloadIndicatorViewModelCreator: Lazy<DownloadIndicatorViewModel>

    @Inject
    lateinit var defaultBrowserPreferenceViewModelCreator: Lazy<DefaultBrowserPreferenceViewModel>

    @Inject
    lateinit var appContext: Context

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var chromeViewModel: ChromeViewModel
    private lateinit var downloadIndicatorViewModel: DownloadIndicatorViewModel
    private lateinit var defaultBrowserPreferenceViewModel: DefaultBrowserPreferenceViewModel
    private lateinit var themeManager: ThemeManager
    private lateinit var topSitesAdapter: DelegateAdapter
    private lateinit var defaultBrowserHelper: DefaultBrowserHelper
    private var currentShoppingBtnVisibleState = false


    private lateinit var menu_red_dot: ImageView
    private lateinit var account_layout: ThemedLinearLayout
    private lateinit var arc_panel: ThemedLinearLayout
    private lateinit var arc_view: ThemedImageView
    private lateinit var content_hub: ContentHub
    private lateinit var content_hub_layout: LinearLayout
    private lateinit var content_hub_title: ThemedTextView
    private lateinit var home_background: HomeScreenBackground
    private lateinit var home_fragment_fake_input: ThemedView
    private lateinit var home_fragment_fake_input_icon: ThemedImageView
    private lateinit var home_fragment_fake_input_text: ThemedTextView
    private lateinit var home_fragment_menu_button: MenuButton
    private lateinit var home_fragment_tab_counter: TabCounter
    private lateinit var iv_home_history: ImageView
    private lateinit var iv_home_marks: ImageView
    private lateinit var vpnSwitchButton: SwitchButton
    private lateinit var ivVpnProtect: ImageView
    private lateinit var view_vpn: ThemedLinearLayout
    private lateinit var home_fragment_title: ImageView
    private lateinit var logo_man_notification: LogoManNotification
    private lateinit var main_list: ViewPager2
    private lateinit var page_indicator: PagerIndicator
    private lateinit var private_mode_button: ThemedImageView
    private lateinit var profile_button: ImageView
    private lateinit var reward_button: ImageView
    private lateinit var search_panel: ThemedLinearLayout
    private lateinit var shopping_button: ThemedImageView

    /** Home背景是否支持切换*/
    private var homeBackgroundChangeAble = false

    private val topSitesPageChangeCallback = object : OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            homeViewModel.onTopSitesPagePositionChanged(position)
        }
    }
    private val toastObserver = Observer<ToastMessage> {
        appContext.showFxToast(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        appComponent().inject(this)
        super.onCreate(savedInstanceState)
        homeViewModel = getActivityViewModel(homeViewModelCreator)
        chromeViewModel = getActivityViewModel(chromeViewModelCreator)
        downloadIndicatorViewModel = getActivityViewModel(downloadIndicatorViewModelCreator)
        defaultBrowserPreferenceViewModel =
            getActivityViewModel(defaultBrowserPreferenceViewModelCreator)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_home, container, false)
        shopping_button = view.findViewById(R.id.shopping_button)
        menu_red_dot = view.findViewById(R.id.menu_red_dot)
        account_layout = view.findViewById(R.id.account_layout)
        arc_panel = view.findViewById(R.id.arc_panel)
        arc_view = view.findViewById(R.id.arc_view)
        content_hub = view.findViewById(R.id.content_hub)
        content_hub_layout = view.findViewById(R.id.content_hub_layout)
        content_hub_title = view.findViewById(R.id.content_hub_title)
        home_background = view.findViewById(R.id.home_background)
        home_fragment_fake_input = view.findViewById(R.id.home_fragment_fake_input)
        home_fragment_fake_input_icon = view.findViewById(R.id.home_fragment_fake_input_icon)
        home_fragment_fake_input_text = view.findViewById(R.id.home_fragment_fake_input_text)
        home_fragment_menu_button = view.findViewById(R.id.home_fragment_menu_button)
        home_fragment_tab_counter = view.findViewById(R.id.home_fragment_tab_counter)
        home_fragment_title = view.findViewById(R.id.home_fragment_title)
        logo_man_notification = view.findViewById(R.id.logo_man_notification)
        main_list = view.findViewById(R.id.main_list)
        page_indicator = view.findViewById(R.id.page_indicator)
        private_mode_button = view.findViewById(R.id.private_mode_button)
        profile_button = view.findViewById(R.id.profile_button)
        reward_button = view.findViewById(R.id.reward_button)
        search_panel = view.findViewById(R.id.search_panel)
        iv_home_history = view.findViewById(R.id.iv_home_history)
        iv_home_marks = view.findViewById(R.id.iv_home_marks)
        vpnSwitchButton = view.findViewById(R.id.vpn_switch_button)
        ivVpnProtect = view.findViewById(R.id.ivVpnProtect)
        view_vpn = view.findViewById(R.id.view_vpn)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        themeManager = (context as ThemeManager.ThemeHost).themeManager
        initSearchToolBar()
        initBackgroundView()
        initTopSites()
        initContentHub()
        initFxaView()
        initLogoManNotification()
        observeDarkTheme()
        initOnboardingSpotlight()
        observeAddNewTopSites()
        observeSetDefaultBrowser()
        observeActions()

        Looper.myQueue().addIdleHandler {
            FirebaseHelper.retrieveTrace("coldStart")?.stopAndClose()
            false
        }
        initVpn()
    }


    private lateinit var zoneList: ArrayList<ZoneBean>
    private val settings by lazy { Settings(activity as MainActivity, "vpn_settings") }
    private var mIsStateChange = false

    private fun initVpn() {
        Logger.d("legend", "===initVpn===")
        val autoConnectVpn = settings.getBoolean("autoConnectVpn", false)
        // connectZoneId  // 上次连接的zone_id, 如果是自动的, 则为空""

        OpenVpnApi.setActivity(activity as MainActivity)
        OpenVpnApi.setBaseUrl("https://test-api.cybervpn.pro/")
        OpenVpnApi.setAppIdUserId("com.tiktok.forbannedcountries", "a.5242925349028eb5")
//        OpenVpnApi.setAppIdUserId(AppDist.getAppId(ObjectStore.getContext()), DeviceHelper.getOrCreateDeviceId(activity))
        // 设置模式为智能或者自定义的时候, 需要传入包名列表mSmartPkgNameList(反选)或mCustomPkgNameList(正选)
        OpenVpnApi.setProxyMode(ProxyModeEnum.PROXY_CUSTOM)
        //白名单
        OpenVpnApi.mCustomPkgNameList.add((activity as MainActivity).packageName)
        OpenVpnApi.zoneLiveData.observe(activity as MainActivity) {
            zoneList = it
//            if (autoConnectVpn) {
//                vpnSwitchButton.isChecked = true
//            }
        }
        OpenVpnApi.serverStateLiveData.observe(activity as MainActivity) {
            Logger.d("legend", "===serverStateLiveData===$it")
            when (it) {
                ConnectState.STATE_PREPARE -> {
                    vpnSwitchButton.isEnabled = false
                }
                ConnectState.STATE_START -> {
                    mIsStateChange = true
                    vpnSwitchButton.isChecked = true
                    vpnSwitchButton.isEnabled = true
                }
                ConnectState.STATE_DISCONNECTED -> {
                    mIsStateChange = true
                    vpnSwitchButton.isChecked = false
                    vpnSwitchButton.isEnabled = true
                }

                else -> {}
            }
        }

        val map = HashMap<String, String>()
        map.put("trace_id", "muccc")
        map.put("app_id", "com.sailfishvpn.fastly.ios")
        map.put("app_version", "4010079")
        map.put("os_version", "29")
        map.put("user_id", "a.5242925349028eb5")
        map.put("beyla_id", "fa441a4acf544cf0b9179d7d898cd7b3")

//        map.put("trace_id", Utils.createUniqueId())
//        map.put("app_id", AppDist.getAppId(ObjectStore.getContext()))
//        map.put("app_version", Utils.getVersionCode(ObjectStore.getContext()).toString())
//        map.put("os_version", Build.VERSION.SDK_INT.toString())
//        map.put("user_id", DeviceHelper.getOrCreateDeviceId(activity))
////            map.put("country","")
////            map.put("gaid","")
//        map.put("beyla_id", DeviceHelper.getOrCreateDeviceId(activity))

        OpenVpnApi.getZoneList(map)
//        vpnSwitchButton.setOnClickListener(object :View.OnClickListener{
//            override fun onClick(p0: View?) {
//                Logger.d("legend","===HomeFragment== vpnSwitchButton.setOnClickListener====")
//            }
//
//        })


        vpnSwitchButton.setOnCheckedChangeListener { view, isChecked ->
            if (mIsStateChange) {
                mIsStateChange = false
            } else {

            }
            Logger.d("legend","===HomeFragment==setOnCheckedChangeListener==isChecked==$isChecked")
            if (isChecked) connectVpn() else OpenVpnApi.stopVpn()
//            settings.setBoolean("autoConnectVpn", isChecked)
            ivVpnProtect.setImageResource(if (isChecked) R.drawable.vpn_thunder_open else R.drawable.vpn_thunder_off)
        }

        view_vpn.setOnLongClickListener {
            if (this::zoneList.isInitialized) {
                val intent = Intent(appContext, SetVpnActivity::class.java)
                startActivity(intent)
            } else {
                OpenVpnApi.getZoneList(map)
            }
            true
        }
    }

    private fun connectVpn() {
        if (OpenVpnApi.serverStateLiveData.value==ConnectState.STATE_PREPARE
            || OpenVpnApi.serverStateLiveData.value==ConnectState.STATE_CONNECTING
            || OpenVpnApi.serverStateLiveData.value==ConnectState.STATE_START) {
            Logger.d("legend","===HomeFragment==connectVpn=时候，正在连接货已经连接成功===${OpenVpnApi.serverStateLiveData.value}")
            return
        }
        val map = HashMap<String, String>()
        map.put("trace_id", "muccc")
        map.put("app_id", "com.sailfishvpn.fastly.ios")
        map.put("app_version", "4010079")
        map.put("os_version", "29")
        map.put("user_id", "a.5242925349028eb5")
//            map.put("country","")
//            map.put("gaid","")
        map.put("beyla_id", "fa441a4acf544cf0b9179d7d898cd7b3")
//            map.put("ip","")
//            map.put("device_id","")
//            map.put("release_channel","")

        val bean = zoneList.firstOrNull { zoneBean ->
            val connectZoneId = settings.get("connectZoneId", "")
            if (connectZoneId!!.isEmpty()) zoneBean.auto == 1 else connectZoneId == zoneBean.zone_id
        }
        Logger.d("legend","===HomeFragment==getZoneProfile==")
        if (bean != null) OpenVpnApi.getZoneProfile(map, bean.zone_id)

    }

    private fun initSearchToolBar() {
        home_fragment_fake_input_text.setOnClickListener {
            chromeViewModel.showUrlInput.call()
            TelemetryWrapper.showSearchBarHome()
        }
        home_fragment_menu_button.apply {
            setOnClickListener {
                chromeViewModel.showHomeMenu.call()
                TelemetryWrapper.showMenuHome()
            }
            setOnLongClickListener {
                chromeViewModel.showDownloadPanel.call()
                TelemetryWrapper.longPressDownloadIndicator()
                true
            }
        }
        iv_home_history.setOnClickListener {
            chromeViewModel.showHistory.call()
            TelemetryWrapper.clickMenuHistory()
        }
        iv_home_marks.setOnClickListener {
            chromeViewModel.showBookmarks.call()
            TelemetryWrapper.clickMenuBookmark()
        }
        home_fragment_tab_counter.setOnClickListener {
            chromeViewModel.showTabTray.call()
            TelemetryWrapper.showTabTrayHome()
        }
        chromeViewModel.tabCount.observe(viewLifecycleOwner, Observer {
            setTabCount(it ?: 0)
        })
        homeViewModel.isShoppingSearchEnabled.observe(viewLifecycleOwner, Observer { isEnabled ->
            shopping_button.isVisible = isEnabled
            private_mode_button.isVisible = !isEnabled
        })
        shopping_button.setOnClickListener { homeViewModel.onShoppingButtonClicked() }
        homeViewModel.openShoppingSearch.observe(viewLifecycleOwner, Observer {
            showShoppingSearch()
        })
        chromeViewModel.isPrivateBrowsingActive.observe(viewLifecycleOwner, Observer {
            private_mode_button.isActivated = it
        })
        private_mode_button.setOnClickListener { homeViewModel.onPrivateModeButtonClicked() }
        homeViewModel.openPrivateMode.observe(viewLifecycleOwner, Observer {
            chromeViewModel.togglePrivateMode.call()
        })
        homeViewModel.shouldShowNewMenuItemHint.switchMap {
            if (it) {
                MutableLiveData<DownloadIndicatorViewModel.Status>().apply { DownloadIndicatorViewModel.Status.DEFAULT }
            } else {
                downloadIndicatorViewModel.downloadIndicatorObservable
            }
        }.observe(viewLifecycleOwner, Observer {
            home_fragment_menu_button.apply {
                when (it) {
                    DownloadIndicatorViewModel.Status.DOWNLOADING -> setDownloadState(
                        DOWNLOAD_STATE_DOWNLOADING
                    )
                    DownloadIndicatorViewModel.Status.UNREAD -> setDownloadState(
                        DOWNLOAD_STATE_UNREAD
                    )
                    DownloadIndicatorViewModel.Status.WARNING -> setDownloadState(
                        DOWNLOAD_STATE_WARNING
                    )
                    else -> setDownloadState(DOWNLOAD_STATE_DEFAULT)
                }
            }
        })
        homeViewModel.shouldShowNewMenuItemHint.observe(viewLifecycleOwner, Observer {
            menu_red_dot.isVisible = it
        })
    }

    private fun initBackgroundView() {
        themeManager.subscribeThemeChange(home_background)
        val backgroundGestureDetector =
            GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent?): Boolean {
                    return true
                }

                override fun onDoubleTap(e: MotionEvent?): Boolean {
                    return homeViewModel.onBackgroundViewDoubleTap()
                }

                override fun onLongPress(e: MotionEvent?) {
                    homeViewModel.onBackgroundViewLongPress()
                }
            })
        home_background.setOnTouchListener { _, event ->
            backgroundGestureDetector.onTouchEvent(event)
        }
        homeViewModel.toggleBackgroundColor.observe(viewLifecycleOwner, Observer {
            if (homeBackgroundChangeAble) {
                val themeSet = themeManager.toggleNextTheme()
                TelemetryWrapper.changeThemeTo(themeSet.name)
            }
        })
        homeViewModel.resetBackgroundColor.observe(viewLifecycleOwner, Observer {
            themeManager.resetDefaultTheme()
            TelemetryWrapper.resetThemeToDefault()
        })
        homeViewModel.homeBackgroundColorThemeClicked.observe(
            viewLifecycleOwner,
            Observer { themeSet ->
                themeManager.setCurrentTheme(themeSet)
            })
    }

    private fun initTopSites() {
        topSitesAdapter = DelegateAdapter(
            AdapterDelegatesManager().apply {
                add(
                    SitePage::class,
                    R.layout.item_top_site_page,
                    SitePageAdapterDelegate(homeViewModel, chromeViewModel)
                )
            }
        )
        main_list.apply {
            adapter = this@HomeFragment.topSitesAdapter
            registerOnPageChangeCallback(topSitesPageChangeCallback)
        }
        var savedTopSitesPagePosition = homeViewModel.topSitesPageIndex.value
        homeViewModel.run {
            sitePages.observe(viewLifecycleOwner, Observer {
                page_indicator.setSize(it.size)
                topSitesAdapter.setData(it)
                savedTopSitesPagePosition?.let { savedPosition ->
                    savedTopSitesPagePosition = null
                    main_list.setCurrentItem(savedPosition, false)
                }
            })
            topSitesPageIndex.observe(viewLifecycleOwner, Observer {
                page_indicator.setSelection(it)
            })
            openBrowser.observe(viewLifecycleOwner, Observer { url ->
                ScreenNavigator.get(context).showBrowserScreen(url, true, false)
            })
            showTopSiteMenu.observe(viewLifecycleOwner, Observer { (site, position) ->
                site as Site.UrlSite.RemovableSite
                val anchorView =
                    main_list.findViewWithTag<View>(TOP_SITE_LONG_CLICK_TARGET).apply { tag = null }
                val allowToPin = !site.isPinned
                showTopSiteMenu(anchorView, allowToPin, site, position)
            })
            showAddTopSiteMenu.observe(viewLifecycleOwner, Observer {
                val anchorView =
                    main_list.findViewWithTag<View>(TOP_SITE_LONG_CLICK_TARGET).apply { tag = null }
                showAddTopSiteMenu(anchorView)
            })
        }
        chromeViewModel.clearBrowsingHistory.observe(viewLifecycleOwner, Observer {
            homeViewModel.onClearBrowsingHistory()
        })
    }

    private fun initContentHub() {
        content_hub.setOnItemClickListener {
            homeViewModel.onContentHubItemClicked(it)
        }
        homeViewModel.run {
            shouldShowContentHubItemText.observe(viewLifecycleOwner, Observer {
                content_hub.setShowText(it)
            })
            isContentHubEnabled.observe(viewLifecycleOwner, Observer { isEnabled ->
                content_hub_layout.isVisible = isEnabled
                home_fragment_title.apply {
                    visibility = if (isEnabled) {
                        resources.getVisibility(R.integer.home_firefox_logo_visibility)
                    } else {
                        View.VISIBLE
                    }
                    layoutParams = (layoutParams as ConstraintLayout.LayoutParams).apply {
                        verticalBias = if (isEnabled) {
                            TITLE_VERTICAL_BIAS_WITH_CONTENT_HUB
                        } else {
                            TITLE_VERTICAL_BIAS
                        }
                    }
                }
            })
            contentHubItems.observe(viewLifecycleOwner, Observer { items ->
                content_hub.setItems(items)
            })
            openContentPage.observe(viewLifecycleOwner, Observer {
                val context = requireContext()
                when (it) {
                    is ContentHub.Item.Travel -> startActivity(TravelActivity.getStartIntent(context))
                    is ContentHub.Item.Shopping -> startActivity(
                        ShoppingActivity.getStartIntent(
                            context
                        )
                    )
                    is ContentHub.Item.News -> startActivity(NewsActivity.getStartIntent(context))
                    is ContentHub.Item.Games -> startActivity(GameActivity.getStartIntent(context))
                }
            })
        }
    }

    private fun initFxaView() {
        homeViewModel.isAccountLayerVisible.observe(viewLifecycleOwner, Observer {
            account_layout.isVisible = it
        })
        homeViewModel.hasUnreadMissions.observe(viewLifecycleOwner, Observer {
            reward_button.isActivated = it
        })
        homeViewModel.isFxAccount.observe(viewLifecycleOwner, Observer {
            profile_button.isActivated = it
        })
        reward_button.setOnClickListener { homeViewModel.onRewardButtonClicked() }
        profile_button.setOnClickListener { homeViewModel.onProfileButtonClicked() }
    }

    private fun observeDarkTheme() {
        chromeViewModel.isDarkTheme.observe(viewLifecycleOwner, Observer { darkThemeEnable ->
            ViewUtils.updateStatusBarStyle(!darkThemeEnable, requireActivity().window)
            topSitesAdapter.notifyDataSetChanged()
            home_background.setDarkTheme(darkThemeEnable)
            content_hub_title.setDarkTheme(darkThemeEnable)
            arc_view.setDarkTheme(darkThemeEnable)
            arc_panel.setDarkTheme(darkThemeEnable)
            search_panel.setDarkTheme(darkThemeEnable)
            home_fragment_fake_input.setDarkTheme(darkThemeEnable)
            home_fragment_fake_input_icon.setDarkTheme(darkThemeEnable)
            home_fragment_fake_input_text.setDarkTheme(darkThemeEnable)
            home_fragment_tab_counter.setDarkTheme(darkThemeEnable)
            home_fragment_menu_button.setDarkTheme(darkThemeEnable)
            account_layout.setDarkTheme(darkThemeEnable)
            shopping_button.setDarkTheme(darkThemeEnable)
            private_mode_button.setDarkTheme(darkThemeEnable)
        })
    }

    override fun onStart() {
        super.onStart()
        homeViewModel.onPageForeground()
    }

    override fun onResume() {
        super.onResume()
        defaultBrowserPreferenceViewModel.onResume()
    }

    override fun onPause() {
        super.onPause()
        defaultBrowserPreferenceViewModel.onPause()
    }

    override fun onStop() {
        super.onStop()
        homeViewModel.onPageBackground()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        themeManager.unsubscribeThemeChange(home_background)
        main_list.unregisterOnPageChangeCallback(topSitesPageChangeCallback)
        homeViewModel.showToast.removeObserver(toastObserver)
    }

    override fun getFragment(): Fragment = this

    override fun onUrlInputScreenVisible(visible: Boolean) {
        if (visible) {
            chromeViewModel.onShowHomePageUrlInput()
        } else {
            chromeViewModel.onDismissHomePageUrlInput()
        }
    }

    override fun applyLocale() {
        home_fragment_fake_input_text.text = getString(R.string.home_search_bar_text)
    }

    fun notifyAddNewTopSiteResult(pinTopSiteResult: PinTopSiteUseCase.PinTopSiteResult?) {
        if (pinTopSiteResult != null) {
            homeViewModel.onAddNewTopSiteResult(pinTopSiteResult)
        }
    }

    private fun showTopSiteMenu(anchorView: View, pinEnabled: Boolean, site: Site, position: Int) {
        PopupMenu(anchorView.context, anchorView, Gravity.CLIP_HORIZONTAL)
            .apply {
                menuInflater.inflate(R.menu.menu_top_site_item, menu)
                menu.findItem(R.id.pin)?.apply {
                    isVisible = pinEnabled
                }
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.pin -> homeViewModel.onPinTopSiteClicked(site, position)
                        R.id.remove -> homeViewModel.onRemoveTopSiteClicked(site, position)
                        else -> throw IllegalStateException("Unhandled menu item")
                    }

                    true
                }
            }
            .show()
    }

    private fun showAddTopSiteMenu(anchorView: View) {
        PopupMenu(anchorView.context, anchorView, Gravity.CLIP_HORIZONTAL)
            .apply {
                menuInflater.inflate(R.menu.menu_add_top_site_item, menu)
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.add_top_sites -> homeViewModel.onAddTopSiteContextMenuClicked()
                        else -> throw IllegalStateException("Unhandled menu item")
                    }
                    true
                }
            }
            .show()
    }

    private fun setTabCount(count: Int, animationEnabled: Boolean = false) {
        home_fragment_tab_counter.apply {
            if (animationEnabled) {
                setCountWithAnimation(count)
            } else {
                setCount(count)
            }
            if (count > 0) {
                isEnabled = true
                alpha = 1f
            } else {
                isEnabled = false
                alpha = 0.3f
            }
        }
    }

    private fun showShoppingSearch() {
        val context: Context = this.context ?: return
        startActivity(ShoppingSearchActivity.getStartIntent(context))
    }

    private fun showAddNewTopSitesPage() {
        activity?.let {
            it.startActivityForResult(
                AddNewTopSitesActivity.getStartIntent(it),
                AddNewTopSitesActivity.REQUEST_CODE_ADD_NEW_TOP_SITES
            )
        }
    }

    private fun initLogoManNotification() {
        homeViewModel.logoManNotification.observe(viewLifecycleOwner, Observer {
            it?.let { (notification, animate) ->
                showLogoManNotification(notification, animate)
            }
        })
        homeViewModel.hideLogoManNotification.observe(viewLifecycleOwner, Observer {
            hideLogoManNotification()
        })
        logo_man_notification.setNotificationActionListener(object :
            LogoManNotification.NotificationActionListener {
            override fun onNotificationClick() {
                homeViewModel.onLogoManNotificationClicked()
            }

            override fun onNotificationDismiss() {
                homeViewModel.onLogoManDismissed()
            }
        })
    }

    private fun showLogoManNotification(
        notification: LogoManNotification.Notification,
        animate: Boolean
    ) {
        logo_man_notification.showNotification(notification, animate)
        homeViewModel.onLogoManShown()
    }

    private fun hideLogoManNotification() {
        logo_man_notification.isVisible = false
    }

    private fun showShoppingSearchSpotlight() {
        val dismissListener = DialogInterface.OnDismissListener {
            restoreStatusBarColor()
            shopping_button?.isVisible = currentShoppingBtnVisibleState
            private_mode_button?.isVisible = !currentShoppingBtnVisibleState
            homeViewModel.onShoppingSearchOnboardingSpotlightDismiss()
        }
        shopping_button.post {
            if (isAdded) {
                setOnboardingStatusBarColor()
                DialogUtils.showShoppingSearchSpotlight(requireActivity(), shopping_button, dismissListener)
            }
        }
    }

    private fun restoreStatusBarColor() {
        activity?.window?.statusBarColor = Color.TRANSPARENT
    }

    private fun setOnboardingStatusBarColor() {
        activity?.let {
            it.window.statusBarColor = ContextCompat.getColor(it, R.color.paletteBlack50)
        }
    }

    private fun initOnboardingSpotlight() {
        homeViewModel.showShoppingSearchOnboardingSpotlight.observe(viewLifecycleOwner, Observer {
            currentShoppingBtnVisibleState = shopping_button.isVisible
            shopping_button.isVisible = true
            private_mode_button.isVisible = false
            showShoppingSearchSpotlight()
        })
    }

    private fun observeAddNewTopSites() {
        homeViewModel.openAddNewTopSitesPage.observe(viewLifecycleOwner, Observer {
            showAddNewTopSitesPage()
        })
        homeViewModel.addNewTopSiteFullyPinned.observe(viewLifecycleOwner, Observer {
            context?.let {
                Toast.makeText(it, R.string.add_top_site_toast, Toast.LENGTH_LONG).show()
            }
        })
        chromeViewModel.addNewTopSiteMenuClicked.observe(viewLifecycleOwner, Observer {
            homeViewModel.onAddTopSiteMenuClicked()
        })
        homeViewModel.addNewTopSiteSuccess.observe(viewLifecycleOwner, Observer { page ->
            page?.let {
                scrollToTopSitePage(it)
            }
            Snackbar.make(main_list, getText(R.string.add_top_site_snackbar_1), Snackbar.LENGTH_LONG)
                .setAction(R.string.add_top_site_button) { homeViewModel.onAddMoreTopSiteSnackBarClicked() }
                .show()
        })
        homeViewModel.addExistingTopSite.observe(viewLifecycleOwner, Observer { page ->
            page?.let {
                scrollToTopSitePage(it)
            }
            Snackbar.make(main_list, getText(R.string.add_top_site_snackbar_2), Snackbar.LENGTH_LONG)
                .setAction(R.string.add_top_site_button) { homeViewModel.onAddMoreTopSiteSnackBarClicked() }
                .show()
        })
    }

    private fun observeSetDefaultBrowser() {
        activity?.let { activity ->
            defaultBrowserHelper = DefaultBrowserHelper(activity, defaultBrowserPreferenceViewModel)
            homeViewModel.tryToSetDefaultBrowser.observe(viewLifecycleOwner, Observer {
                defaultBrowserPreferenceViewModel.performAction()
            })
            defaultBrowserPreferenceViewModel.openDefaultAppsSettings.observe(viewLifecycleOwner, Observer { defaultBrowserHelper.openDefaultAppsSettings() })
            defaultBrowserPreferenceViewModel.openAppDetailSettings.observe(viewLifecycleOwner, Observer { defaultBrowserHelper.openAppDetailSettings() })
            defaultBrowserPreferenceViewModel.openSumoPage.observe(viewLifecycleOwner, Observer { defaultBrowserHelper.openSumoPage() })
            defaultBrowserPreferenceViewModel.triggerWebOpen.observe(viewLifecycleOwner, Observer { defaultBrowserHelper.triggerWebOpen() })
            defaultBrowserPreferenceViewModel.openDefaultAppsSettingsTutorialDialog.observe(viewLifecycleOwner, Observer { DialogUtils.showGoToSystemAppsSettingsDialog(activity, defaultBrowserPreferenceViewModel) })
            defaultBrowserPreferenceViewModel.openUrlTutorialDialog.observe(viewLifecycleOwner, Observer { DialogUtils.showOpenUrlDialog(activity, defaultBrowserPreferenceViewModel) })
            defaultBrowserPreferenceViewModel.successToSetDefaultBrowser.observe(viewLifecycleOwner, Observer { defaultBrowserHelper.showSuccessMessage() })
            defaultBrowserPreferenceViewModel.failToSetDefaultBrowser.observe(viewLifecycleOwner, Observer { defaultBrowserHelper.showFailMessage() })
        }
    }

    private fun scrollToTopSitePage(page: Int) =
        main_list.postDelayed({ main_list.setCurrentItem(page, 300) }, 100)

    private fun observeActions() {
        homeViewModel.showToast.observeForever(toastObserver)
        homeViewModel.openRewardPage.observe(viewLifecycleOwner, Observer {
            openRewardPage()
        })
        homeViewModel.openProfilePage.observe(viewLifecycleOwner, Observer {
            openProfilePage()
        })
        homeViewModel.showMissionCompleteDialog.observe(viewLifecycleOwner, Observer { mission ->
            showMissionCompleteDialog(mission)
        })
        homeViewModel.executeUriAction.observe(viewLifecycleOwner, Observer { action ->
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(action), appContext, RocketLauncherActivity::class.java))
        })
        homeViewModel.openMissionDetailPage.observe(viewLifecycleOwner, Observer { mission ->
            openMissionDetailPage(mission)
        })
        homeViewModel.showContentHubClickOnboarding.observe(viewLifecycleOwner, Observer { couponName ->
            showRequestClickContentHubOnboarding(couponName)
        })
        homeViewModel.showKeyboard.observe(viewLifecycleOwner, Observer {
            Looper.myQueue().addIdleHandler {
                if (!isStateSaved) {
                    home_fragment_fake_input.performClick()
                }
                false
            }
        })
        chromeViewModel.themeSettingMenuClicked.observe(viewLifecycleOwner, Observer {
            homeViewModel.onThemeSettingMenuClicked()
        })
        //主题切换弹窗dialog
        homeViewModel.showThemeSetting.observe(viewLifecycleOwner, Observer {
            activity?.let {
//                DialogUtils.showThemeSettingDialog(it, homeViewModel)
            }
        })
        homeViewModel.showSetAsDefaultBrowserOnboarding.observe(viewLifecycleOwner, Observer {
            activity?.let {
                DialogUtils.showSetAsDefaultBrowserDialog(
                    it,
                    { homeViewModel.onSetAsDefaultBrowserClicked() },
                    { homeViewModel.onCancelSetAsDefaultBrowserClicked() }
                )
            }
        })
    }

    private fun showMissionCompleteDialog(mission: Mission) {
        DialogUtils.createMissionCompleteDialog(requireContext(), mission.imageUrl)
                .onPositive {
                    homeViewModel.onRedeemCompletedMissionButtonClicked(mission)
                }
                .onNegative {
                    homeViewModel.onRedeemCompletedLaterButtonClicked()
                }
                .onClose {
                    homeViewModel.onRedeemCompletedDialogClosed()
                }
                .show()
    }

    private fun showRequestClickContentHubOnboarding(couponName: String) {
        val dismissListener = DialogInterface.OnDismissListener {
            restoreStatusBarColor()
            homeViewModel.onContentHubRequestClickHintDismissed()
        }
        content_hub.post {
            if (isAdded) {
                homeViewModel.onShowClickContentHubOnboarding()
                setOnboardingStatusBarColor()
                DialogUtils.showContentServiceRequestClickSpotlight(requireActivity(), content_hub, couponName, dismissListener)
            }
        }
    }

    private fun openRewardPage() {
        startActivity(RewardActivity.getStartIntent(requireContext()))
    }

    private fun openProfilePage() {
        startActivity(ProfileActivity.getStartIntent(requireContext()))
    }

    private fun openMissionDetailPage(mission: Mission) {
        startActivity(RewardActivity.getStartIntent(requireContext(), RewardActivity.DeepLink.MissionDetailPage(mission)))
    }

    companion object {
        private const val TITLE_VERTICAL_BIAS = 0.45f
        private const val TITLE_VERTICAL_BIAS_WITH_CONTENT_HUB = 0.26f
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Logger.d("legend","===HomeFragment==onActivityResult===requestCode==$requestCode ===resultCode==$resultCode")
        if (requestCode==998) {
            if (resultCode == -1){
                VpnHelper.instance.toStartVpn()
            }else{
                //用户拒绝了vpn权限
            }
        }

    }
}
