package org.mozilla.rocket.content.travel.ui.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import org.mozilla.focus.R
import org.mozilla.rocket.adapter.AdapterDelegate
import org.mozilla.rocket.adapter.DelegateAdapter

class CitySearchResultCategoryAdapterDelegate() : AdapterDelegate {
    override fun onCreateViewHolder(view: View): DelegateAdapter.ViewHolder = CitySearchResultCategoryViewHolder(view)
}

class CitySearchResultCategoryViewHolder(override val containerView: View) : DelegateAdapter.ViewHolder(containerView) {
    var title: TextView = containerView.findViewById(R.id.title)
    var icon: ImageView = containerView.findViewById(R.id.icon)
    override fun bind(uiModel: DelegateAdapter.UiModel) {
        uiModel as CitySearchResultCategoryUiModel
        icon.setImageResource(uiModel.imgResId)
        title.text = uiModel.title
    }
}

data class CitySearchResultCategoryUiModel(
    val imgResId: Int,
    val title: String
) : DelegateAdapter.UiModel()
