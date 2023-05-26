package org.mozilla.rocket.home.contenthub.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.MarginLayoutParamsCompat
import androidx.core.view.isVisible
import org.mozilla.focus.R
import org.mozilla.rocket.extension.dpToPx

class ContentHub : LinearLayout {

    private var clickListener: ((Item) -> Unit)? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var showText = false

    lateinit var icon:ImageView
    lateinit  var red_dot:View
    lateinit  var text: TextView
    init {
        orientation = HORIZONTAL
        clipToPadding = false
        clipChildren = false
        dpToPx(PADDING_IN_DP).let { setPadding(it, it, it, it) }
    }

    fun setShowText(showText: Boolean) {
        this.showText = showText
    }

    fun setItems(items: List<Item>) {
        removeAllViews()
        items.forEachIndexed { i, item ->
            val isLast = i == items.size - 1
            val isUnread = item.isUnread
            val itemView = View.inflate(context, R.layout.item_content_hub, null)
            icon = itemView.findViewById(R.id.icon)
            icon.setImageResource(item.iconResId)
            icon. setOnClickListener { clickListener?.invoke(item)}
//                itemView.icon.apply {
//                setImageResource(item.iconResId)
//                setOnClickListener { clickListener?.invoke(item) }
//            }
            text = itemView.findViewById(R.id.text)
            text. setText(item.textResId)
            text.isVisible = true
//            itemView.text.apply {
//                if (showText) {
//                    setText(item.textResId)
//                    isVisible = true
//                }
//            }
            red_dot = itemView.findViewById(R.id.red_dot)
            red_dot.isVisible = isUnread
//            itemView.red_dot.apply {
//                isVisible = isUnread
//            }
            addView(itemView, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                if (!isLast) {
                    MarginLayoutParamsCompat.setMarginEnd(this, dpToPx(ITEM_MARGIN_IN_DP))
                }
            })
        }
    }

    fun setOnItemClickListener(listener: (Item) -> Unit) {
        clickListener = listener
    }

    sealed class Item(open val iconResId: Int, open val textResId: Int, open var isUnread: Boolean) {
        data class Travel(override val iconResId: Int, override val textResId: Int, override var isUnread: Boolean) : Item(iconResId, textResId, isUnread)
        data class Shopping(override val iconResId: Int, override val textResId: Int, override var isUnread: Boolean) : Item(iconResId, textResId, isUnread)
        data class News(override val iconResId: Int, override val textResId: Int, override var isUnread: Boolean) : Item(iconResId, textResId, isUnread)
        data class Games(override val iconResId: Int, override val textResId: Int, override var isUnread: Boolean) : Item(iconResId, textResId, isUnread)
    }

    companion object {
        private const val PADDING_IN_DP = 12f
        private const val ITEM_MARGIN_IN_DP = 24f
    }
}