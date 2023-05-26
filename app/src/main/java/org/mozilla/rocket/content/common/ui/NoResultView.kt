package org.mozilla.rocket.content.common.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import org.mozilla.focus.R

class NoResultView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val button: TextView
    private val no_result_view_text: TextView
//    private val no_result_view_button: TextView
    private val no_result_view_image: ImageView

    init {
        View.inflate(context, R.layout.no_result_view, this)
        button = findViewById(R.id.no_result_view_button)
        no_result_view_text = findViewById(R.id.no_result_view_text)
//        no_result_view_button = findViewById(R.id.no_result_view_button)
        no_result_view_image = findViewById(R.id.no_result_view_image)

        isClickable = false
    }

    fun setIconResource(@DrawableRes resourceId: Int) {
        no_result_view_image.setImageResource(resourceId)
    }

    fun setMessage(message: String) {
        no_result_view_text.text = message
    }

    fun setButtonText(text: String) {
        button.text = text
    }

    fun release() {
        button.setOnClickListener(null)
    }

    fun setButtonOnClickListener(listener: OnClickListener?) {
        button.setOnClickListener(listener)
    }
}
