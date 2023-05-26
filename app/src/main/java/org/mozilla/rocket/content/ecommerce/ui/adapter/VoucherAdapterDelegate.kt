package org.mozilla.rocket.content.ecommerce.ui.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import org.mozilla.focus.R
import org.mozilla.focus.utils.DrawableUtils
import org.mozilla.rocket.adapter.AdapterDelegate
import org.mozilla.rocket.adapter.DelegateAdapter
import org.mozilla.rocket.content.ecommerce.ui.VoucherViewModel

class VoucherAdapterDelegate(private val voucherViewModel: VoucherViewModel) : AdapterDelegate {

    override fun onCreateViewHolder(view: View): DelegateAdapter.ViewHolder =
        VoucherViewHolder(view, voucherViewModel)
}

class VoucherViewHolder(
    override val containerView: View,
    private val voucherViewModel: VoucherViewModel
) : DelegateAdapter.ViewHolder(containerView) {
    var voucher_item:CardView = containerView.findViewById(R.id.voucher_item)
    var voucher_category_text:TextView = containerView.findViewById(R.id.voucher_category_text)
    var voucher_category_image:ImageView = containerView.findViewById(R.id.voucher_category_image)
    override fun bind(uiModel: DelegateAdapter.UiModel) {
        val voucherItem = uiModel as Voucher

        voucher_item.setOnClickListener {
            voucherViewModel.onVoucherItemClicked(voucherItem)
        }

        voucher_category_text.text = voucherItem.name

        DrawableUtils.getAndroidDrawable(itemView.context, voucherItem.image)?.let {
            voucher_category_image.setImageDrawable(it)
        }
    }
}

object VoucherKey {
    const val KEY_NAME = "name"
    const val KEY_URL = "url"
    const val KEY_IMAGE = "img"
    const val KEY_SOURCE = "source"
}

data class Voucher(
    val url: String,
    val name: String,
    val image: String,
    val source: String,
    val subCategoryId: String = DEFAULT_SUB_CATEGORY_ID
) : DelegateAdapter.UiModel() {
    companion object {
        private const val DEFAULT_SUB_CATEGORY_ID = "23"
    }
}