package org.mozilla.rocket.content.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import org.mozilla.focus.R
import org.mozilla.focus.widget.EqualDistributeGrid

class MenuLayout : FrameLayout {
    private lateinit var grid: EqualDistributeGrid
    private var onItemClickListener: OnItemClickListener? = null
    private var onItemLongClickListener: OnItemLongClickListener? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    init {
        addItemContainer()
    }

    private fun addItemContainer() {
        EqualDistributeGrid(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            rowCapacity = ROW_CAPACITY
        }.let {
            grid = it
            addView(it)
        }
    }

    fun setItems(items: List<MenuItem>) {
        grid.removeAllViews()
        items.forEachIndexed { index, item ->
            item.createView(context, grid).apply {
                setOnClickListener { onItemClickListener?.onItemClick(item.type, index) }
                setOnLongClickListener { onItemLongClickListener?.onItemLongClick(item.type, index) ?: false }
            }.let { view ->
                item.view = view
                grid.addView(view)
            }
        }
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener) {
        onItemLongClickListener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(type: Int, position: Int)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(type: Int, position: Int): Boolean
    }

    abstract class MenuItem(val type: Int, val viewId: Int) {
        var view: View? = null

        abstract fun createView(context: Context, parent: ViewGroup): View

        open class TextImageItem(
            type: Int,
            id: Int,
            private val textResId: Int,
            private val drawableResId: Int,
            private val tintResId: Int?
        ) : MenuItem(type, id) {
            override fun createView(context: Context, parent: ViewGroup): View {
               var view = LayoutInflater.from(context)
                    .inflate(R.layout.menu_item_text_image, parent, false)
                       view.id = viewId
                var menu_item_image:ImageView =  view.findViewById(R.id.menu_item_image)
                        menu_item_image.setImageResource(drawableResId)
                if (tintResId != null) {
                    menu_item_image.imageTintList = ContextCompat.getColorStateList(context, tintResId)
                }

                var menu_item_text:TextView =  view.findViewById(R.id.menu_item_text)
                menu_item_text.setText(textResId)
                if (tintResId != null) {
                    menu_item_text.setTextColor(ContextCompat.getColorStateList(context, tintResId))
                            }

                return view
            }
        }
    }

    companion object {
        const val ROW_CAPACITY = 4
    }
}