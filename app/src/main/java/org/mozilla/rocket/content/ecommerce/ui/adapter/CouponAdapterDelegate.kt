package org.mozilla.rocket.content.ecommerce.ui.adapter

import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import org.mozilla.focus.R
import org.mozilla.focus.glide.GlideApp
import org.mozilla.rocket.adapter.AdapterDelegate
import org.mozilla.rocket.adapter.DelegateAdapter
import org.mozilla.rocket.content.ecommerce.ui.CouponViewModel
import org.mozilla.rocket.extension.obtainBackgroundColor

class CouponAdapterDelegate(private val couponViewModel: CouponViewModel) : AdapterDelegate {

    override fun onCreateViewHolder(view: View): DelegateAdapter.ViewHolder =
        CouponViewHolder(view, couponViewModel)
}

class CouponViewHolder(
    override val containerView: View,
    private val couponViewModel: CouponViewModel
) : DelegateAdapter.ViewHolder(containerView) {
    var coupon_brand:TextView?=containerView.findViewById(R.id.coupon_brand)
    var coupon_title:TextView?=containerView.findViewById(R.id.coupon_title)
    var coupon_remain:TextView?=containerView.findViewById(R.id.coupon_remain)
    var coupon_image: ImageView?=containerView.findViewById(R.id.coupon_image)
    override fun bind(uiModel: DelegateAdapter.UiModel) {
        val couponItem = uiModel as Coupon
//        coupon_brand = containerView.findViewById(R.id.coupon_brand)
//        coupon_title = containerView.findViewById(R.id.coupon_title)
//        coupon_remain = containerView.findViewById(R.id.coupon_remain)
//        coupon_image = containerView.findViewById(R.id.coupon_image)
        itemView.setOnClickListener { couponViewModel.onCouponItemClicked(couponItem) }

        coupon_brand?.text = couponItem.source
        coupon_title?.text = couponItem.title
        coupon_remain?.text = when (couponItem.remainingDays) {
            Long.MIN_VALUE -> ""
            0L -> itemView.context.getString(R.string.shopping_coupon_expire_zero)
            1L -> itemView.context.getString(R.string.shopping_coupon_expire_one)
            else -> itemView.context.getString(R.string.shopping_coupon_expire_other, couponItem.remainingDays)
        }

        GlideApp.with(itemView.context)
            .asBitmap()
            .load(couponItem.imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.placeholder)
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(e: GlideException?, model: Any, target: com.bumptech.glide.request.target.Target<Bitmap>, isFirstResource: Boolean): Boolean {
                    return false
                }

                override fun onResourceReady(resource: Bitmap?, model: Any, target: com.bumptech.glide.request.target.Target<Bitmap>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                    if (resource != null) {
                        coupon_image?.setBackgroundColor(resource.obtainBackgroundColor())
                    }
                    return false
                }
            })
            .into(coupon_image)
    }
}

data class Coupon(
    val source: String,
    val category: String,
    val subCategoryId: String,
    val imageUrl: String,
    val linkUrl: String,
    val title: String,
    val componentId: String,
    val remainingDays: Long
) : DelegateAdapter.UiModel()
