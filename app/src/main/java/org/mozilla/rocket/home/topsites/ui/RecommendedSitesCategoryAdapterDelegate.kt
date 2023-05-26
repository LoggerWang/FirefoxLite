package org.mozilla.rocket.home.topsites.ui

import android.view.View
import android.widget.TextView
import org.mozilla.focus.R
import org.mozilla.rocket.adapter.AdapterDelegate
import org.mozilla.rocket.adapter.DelegateAdapter

class RecommendedSitesCategoryAdapterDelegate() : AdapterDelegate {

    override fun onCreateViewHolder(view: View): DelegateAdapter.ViewHolder =
        RecommendedSitesCategoryViewHolder(view)
}

class RecommendedSitesCategoryViewHolder(override val containerView: View) : DelegateAdapter.ViewHolder(containerView) {
    var recommended_category: TextView = containerView.findViewById(R.id.recommended_category)
    override fun bind(uiModel: DelegateAdapter.UiModel) {
        val recommendedSitesUiCategory = uiModel as RecommendedSitesUiCategory
        recommended_category.text = recommendedSitesUiCategory.categoryName
    }
}

data class RecommendedSitesUiCategory(
    val categoryId: String,
    val categoryName: String
) : DelegateAdapter.UiModel()