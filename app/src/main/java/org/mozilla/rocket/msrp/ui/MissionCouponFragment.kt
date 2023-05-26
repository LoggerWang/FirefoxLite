package org.mozilla.rocket.msrp.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import dagger.Lazy
import org.mozilla.focus.R
import org.mozilla.rocket.content.appComponent
import org.mozilla.rocket.content.appContext
import org.mozilla.rocket.content.common.ui.ContentTabActivity
import org.mozilla.rocket.content.getViewModel
import org.mozilla.rocket.extension.showToast
import javax.inject.Inject

class MissionCouponFragment : Fragment() {

    private val safeArgs: MissionCouponFragmentArgs by navArgs()
    private val mission by lazy { safeArgs.mission }
    private lateinit var viewModel: MissionCouponViewModel
    private lateinit var coupon_code:TextView
    private lateinit var coupon_copy_btn:TextView
    private lateinit var coupon_expiration:TextView
    private lateinit var coupon_go_shopping_btn:TextView
    private lateinit var faq_text: TextView
    private lateinit var image:ImageView
    private lateinit var loading_view:ProgressBar
    private lateinit var title:TextView


    @Inject
    lateinit var missionCouponViewModelCreator: Lazy<MissionCouponViewModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        appComponent().inject(this)
        super.onCreate(savedInstanceState)
        viewModel = getViewModel(missionCouponViewModelCreator)
        viewModel.init(mission)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_mission_coupon, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        coupon_code = view.findViewById(R.id.coupon_code)
        coupon_copy_btn = view.findViewById(R.id.coupon_copy_btn)
        coupon_expiration = view.findViewById(R.id.coupon_expiration)
        title = view.findViewById(R.id.title)
        coupon_go_shopping_btn = view.findViewById(R.id.coupon_go_shopping_btn)
        faq_text = view.findViewById(R.id.faq_text)
        image = view.findViewById(R.id.image)
        loading_view = view.findViewById(R.id.loading_view)
        initViews()
        bindData()
        observeAction()
    }

    private fun initViews() {
        initFaqText()
        coupon_copy_btn.setOnClickListener {
            viewModel.onCopyCouponButtonClicked()
        }
        coupon_go_shopping_btn.setOnClickListener {
            viewModel.onGoShoppingButtonClicked()
        }
    }

    private fun initFaqText() {
        val contextUsStr = getString(R.string.msrp_contact_us)
        val faqStr = getString(R.string.msrp_faq, contextUsStr)
        val contextUsIndex = faqStr.indexOf(contextUsStr)
        val str = SpannableString(faqStr).apply {
            setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    viewModel.onFaqButtonClick()
                }
            }, contextUsIndex, contextUsIndex + contextUsStr.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        faq_text.apply {
            movementMethod = LinkMovementMethod.getInstance()
            text = str
        }
    }

    private fun bindData() {
        viewModel.couponName.observe(viewLifecycleOwner, Observer {
            title.text = getString(R.string.msrp_voucher_title, it)
        })
        viewModel.expirationTime.observe(viewLifecycleOwner, Observer { timeText ->
            coupon_expiration.text = getString(R.string.msrp_reward_challenge_expire, timeText)
        })
        viewModel.missionImage.observe(viewLifecycleOwner, Observer { url ->
            Glide.with(requireContext())
                    .load(url)
                    .apply(RequestOptions().apply { transforms(CircleCrop()) })
                    .into(image)
        })
        viewModel.couponCode.observe(viewLifecycleOwner, Observer { couponCode ->
            coupon_code.text = couponCode
        })
        viewModel.isLoading.observe(viewLifecycleOwner, Observer {
            loading_view.isVisible = it
        })
    }

    private fun observeAction() {
        viewModel.showToast.observe(viewLifecycleOwner, Observer {
            appContext().showToast(it)
        })
        viewModel.copyToClipboard.observe(viewLifecycleOwner, Observer { text ->
            copyToClipboard(COUPON_COPY_LABEL, text)
        })
        viewModel.openShoppingPage.observe(viewLifecycleOwner, Observer { url ->
            if (url != null) {
                openContentTab(url)
            }
        })
        viewModel.openFaqPage.observe(viewLifecycleOwner, Observer {
            openFaqPage()
        })
    }

    private fun copyToClipboard(label: String, text: String) {
        getSystemService(appContext(), ClipboardManager::class.java)?.run {
//            primaryClip = ClipData.newPlainText(label, text)
        }
    }

    private fun openContentTab(url: String) {
        val intent = ContentTabActivity.getStartIntent(requireContext(), url, enableTurboMode = true)
        startActivity(intent)
    }

    private fun openFaqPage() {
        val intent = ContentTabActivity.getStartIntent(requireContext(), FAQ_PAGE_URL, enableTurboMode = false)
        startActivity(intent)
    }

    companion object {
        private const val COUPON_COPY_LABEL = "coupon"
        private const val FAQ_PAGE_URL = "https://qsurvey.mozilla.com/s3/Firefox-Lite-Reward-Help"
    }
}