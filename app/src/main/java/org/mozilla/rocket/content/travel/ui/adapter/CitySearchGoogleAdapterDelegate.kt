package org.mozilla.rocket.content.travel.ui.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.mozilla.focus.R
import org.mozilla.rocket.adapter.AdapterDelegate
import org.mozilla.rocket.adapter.DelegateAdapter
import org.mozilla.rocket.content.travel.ui.TravelCitySearchViewModel

class CitySearchGoogleAdapterDelegate(private val searchViewModel: TravelCitySearchViewModel) : AdapterDelegate {
    override fun onCreateViewHolder(view: View): DelegateAdapter.ViewHolder = CitySearchGoogleViewHolder(view, searchViewModel)
}

class CitySearchGoogleViewHolder(override val containerView: View, private val searchViewModel: TravelCitySearchViewModel) : DelegateAdapter.ViewHolder(containerView) {

    var title: TextView = containerView.findViewById(R.id.title)
    override fun bind(uiModel: DelegateAdapter.UiModel) {
        uiModel as CitySearchGoogleUiModel
        title.text = uiModel.keyword
        containerView.setOnClickListener {
            searchViewModel.onGoogleSearchClicked(containerView.context, uiModel.keyword)
        }
    }
}

data class CitySearchGoogleUiModel(
    val keyword: String
) : DelegateAdapter.UiModel()
