package org.mozilla.rocket.update

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import org.mozilla.focus.R

/**
 * desc:
 */
class UpdateItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var tvIndex:TextView?=null
    private var tvContent:TextView?=null

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_update_item_view,this)
        tvIndex = findViewById(R.id.tv_index)
        tvContent = findViewById(R.id.tv_content)
    }


    fun setContent(indexStr: String, contentStr: String) {
        tvIndex?.setText(indexStr)
        tvContent?.setText(contentStr)
    }
}