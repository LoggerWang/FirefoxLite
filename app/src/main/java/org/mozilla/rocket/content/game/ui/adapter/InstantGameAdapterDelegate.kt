package org.mozilla.rocket.content.game.ui.adapter

import android.view.ContextMenu
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import org.mozilla.focus.R
import org.mozilla.focus.glide.GlideApp
import org.mozilla.rocket.adapter.AdapterDelegate
import org.mozilla.rocket.adapter.DelegateAdapter
import org.mozilla.rocket.content.game.ui.InstantGameViewModel
import org.mozilla.rocket.content.game.ui.model.Game

class InstantGameAdapterDelegate(private val instantGameViewModel: InstantGameViewModel) : AdapterDelegate {
    override fun onCreateViewHolder(view: View): DelegateAdapter.ViewHolder =
        InstantGameViewHolder(view, instantGameViewModel)
}

class InstantGameViewHolder(
    override val containerView: View,
    private val instantGameViewModel: InstantGameViewModel
) : DelegateAdapter.ViewHolder(containerView), View.OnCreateContextMenuListener {
    var game_name: TextView = containerView.findViewById(R.id.game_name)
    var game_image: ImageView = containerView.findViewById(R.id.game_image)
    override fun bind(uiModel: DelegateAdapter.UiModel) {
        val gameItem = uiModel as Game
        game_name.text = gameItem.name
        GlideApp.with(itemView.context)
            .asBitmap()
            .placeholder(R.drawable.placeholder)
            .load(gameItem.imageUrl)
            .into(game_image)

        itemView.setOnClickListener { instantGameViewModel.onGameItemClicked(gameItem) }
        itemView.setOnLongClickListener { instantGameViewModel.onGameItemLongClicked(gameItem) }
        itemView.setOnCreateContextMenuListener(this)
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        menu?.let {
            instantGameViewModel.onCreateContextMenu(it)
        }
    }
}