package org.mozilla.rocket.content.travel.ui.adapter

import android.view.View
import android.widget.LinearLayout
import org.mozilla.focus.R
import org.mozilla.rocket.adapter.AdapterDelegate
import org.mozilla.rocket.adapter.DelegateAdapter
import org.mozilla.rocket.content.travel.ui.TravelExploreViewModel

class CitySearchAdapterDelegate(private val travelExploreViewModel: TravelExploreViewModel) : AdapterDelegate {
    override fun onCreateViewHolder(view: View): DelegateAdapter.ViewHolder =
        CitySearchViewHolder(view, travelExploreViewModel)
}

class CitySearchViewHolder(
    override val containerView: View,
    private val travelExploreViewModel: TravelExploreViewModel
) : DelegateAdapter.ViewHolder(containerView) {

    var city_search_edit_area: LinearLayout = containerView.findViewById(R.id.city_search_edit_area)
    override fun bind(uiModel: DelegateAdapter.UiModel) {

        city_search_edit_area.setOnClickListener {
            travelExploreViewModel.onSearchInputClicked()
        }
    }
}

class CitySearchUiModel : DelegateAdapter.UiModel()
