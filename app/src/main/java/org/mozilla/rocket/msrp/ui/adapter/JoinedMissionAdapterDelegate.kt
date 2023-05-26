package org.mozilla.rocket.msrp.ui.adapter

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import org.mozilla.focus.R
import org.mozilla.rocket.adapter.AdapterDelegate
import org.mozilla.rocket.adapter.DelegateAdapter
import org.mozilla.rocket.extension.dpToPx
import org.mozilla.rocket.msrp.ui.MissionViewModel

class JoinedMissionsAdapterDelegate(
    private val missionViewModel: MissionViewModel
) : AdapterDelegate {
    override fun onCreateViewHolder(view: View): DelegateAdapter.ViewHolder =
            JoinedMissionsViewHolder(missionViewModel, view)
}

class JoinedMissionsViewHolder(
    private val missionViewModel: MissionViewModel,
    override val containerView: View
) : DelegateAdapter.ViewHolder(containerView) {

    private val imgReqOpts = RequestOptions().apply { transforms(CenterCrop(), RoundedCorners(containerView.dpToPx(4f))) }

    var title: TextView = containerView.findViewById(R.id.title)
    var expiration_text: TextView = containerView.findViewById(R.id.expiration_text)
    var progress_text: TextView = containerView.findViewById(R.id.progress_text)
    var progress: ProgressBar = containerView.findViewById(R.id.progress)
    var image: ImageView = containerView.findViewById(R.id.image)
    override fun bind(uiModel: DelegateAdapter.UiModel) {
        uiModel as MissionUiModel.JoinedMission

        title.text = uiModel.title
        expiration_text.text = itemView.resources.getString(R.string.msrp_reward_challenge_expire, uiModel.expirationTime)
        progress.progress = uiModel.progress
        @SuppressLint("SetTextI18n")
        progress_text.text = "${uiModel.progress}%"

        Glide.with(containerView.context)
                .load(uiModel.imageUrl)
                .apply(imgReqOpts)
                .into(image)

        itemView.setOnClickListener {
            missionViewModel.onChallengeItemClicked(adapterPosition)
        }
    }
}