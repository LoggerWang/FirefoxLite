package org.mozilla.rocket.msrp.ui.adapter

import android.view.View
import android.widget.TextView
import org.mozilla.focus.R
import org.mozilla.rocket.adapter.AdapterDelegate
import org.mozilla.rocket.adapter.DelegateAdapter
import org.mozilla.rocket.msrp.ui.MissionViewModel

class RedeemableMissionAdapterDelegate(
    private val missionViewModel: MissionViewModel
) : AdapterDelegate {
    override fun onCreateViewHolder(view: View): DelegateAdapter.ViewHolder =
            RedeemableMissionsViewHolder(missionViewModel, view)
}

class RedeemableMissionsViewHolder(
    private val missionViewModel: MissionViewModel,
    override val containerView: View
) : DelegateAdapter.ViewHolder(containerView) {
    override fun bind(uiModel: DelegateAdapter.UiModel) {
        var title: TextView = containerView.findViewById(R.id.title)
        var expiration_text: TextView = containerView.findViewById(R.id.expiration_text)
        uiModel as MissionUiModel.RedeemableMission

        title.text = uiModel.title
        expiration_text.text = itemView.resources.getString(R.string.msrp_voucher_expire, uiModel.expirationTime)

        itemView.setOnClickListener {
            missionViewModel.onRedeemItemClicked(adapterPosition)
        }
    }
}