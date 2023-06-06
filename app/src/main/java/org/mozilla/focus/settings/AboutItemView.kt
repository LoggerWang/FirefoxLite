package org.mozilla.focus.settings

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
class AboutItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var tvTitle:TextView?=null
    //private var tv_version_name:TextView?=null
    private var divider : View ?= null

    private var rlRoot:RelativeLayout ?=null
    init {
        LayoutInflater.from(context).inflate(R.layout.layout_about_item_view,this)
        tvTitle = findViewById(R.id.tv_title)
        //tv_version_name = findViewById(R.id.tv_version_name)
        divider = findViewById(R.id.divider)
    }

    private fun setContent(ivId: Int? = 0, title: String) {
        tvTitle?.text = title
    }

    fun setVersion(){
        //tv_version_name?.visibility = View.VISIBLE
        //tv_version_name?.text = "V"+context.packageManager.getPackageInfo(context.packageName,0).versionName
        divider?.visibility = VISIBLE
    }

    fun setContent(ivId: Int? = 0, titleId: Int?=0) {
        tvTitle?.text = titleId?.let { resources.getText(it) }
    }

}