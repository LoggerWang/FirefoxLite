package org.mozilla.rocket.msrp.ui.adapter

import android.view.View
import android.widget.TextView
import org.mozilla.focus.R
import org.mozilla.rocket.adapter.AdapterDelegate
import org.mozilla.rocket.adapter.DelegateAdapter
import org.mozilla.rocket.msrp.ui.MissionViewModel

class ExpiredMissionAdapterDelegate(
    private val missionViewModel: MissionViewModel
) : AdapterDelegate {
    override fun onCreateViewHolder(view: View): DelegateAdapter.ViewHolder =
            ExpiredMissionsViewHolder(missionViewModel, view)
}

class ExpiredMissionsViewHolder(
    private val missionViewModel: MissionViewModel,
    override val containerView: View
) : DelegateAdapter.ViewHolder(containerView) {
    var title: TextView = containerView.findViewById(R.id.title)
    override fun bind(uiModel: DelegateAdapter.UiModel) {
        uiModel as MissionUiModel.ExpiredMission

        title.text = uiModel.title

        itemView.setOnClickListener {
            missionViewModel.onRedeemItemClicked(adapterPosition)
        }
    }
}