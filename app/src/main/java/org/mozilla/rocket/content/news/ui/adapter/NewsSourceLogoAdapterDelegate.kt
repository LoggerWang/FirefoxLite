package org.mozilla.rocket.content.news.ui.adapter

import android.view.View
import android.widget.ImageView
import org.mozilla.focus.R
import org.mozilla.rocket.adapter.AdapterDelegate
import org.mozilla.rocket.adapter.DelegateAdapter

class NewsSourceLogoAdapterDelegate : AdapterDelegate {
    override fun onCreateViewHolder(view: View): DelegateAdapter.ViewHolder =
        NewsSourceLogoViewHolder(view)
}

class NewsSourceLogoViewHolder(
    override val containerView: View
) : DelegateAdapter.ViewHolder(containerView) {

    var news_item_source_logo_image: ImageView = containerView.findViewById(R.id.news_item_source_logo_image)
    override fun bind(uiModel: DelegateAdapter.UiModel) {
        val newsUiModel = uiModel as NewsSourceLogoUiModel
        news_item_source_logo_image.setImageResource(newsUiModel.resourceId)
    }
}

data class NewsSourceLogoUiModel(
    val resourceId: Int
) : DelegateAdapter.UiModel()