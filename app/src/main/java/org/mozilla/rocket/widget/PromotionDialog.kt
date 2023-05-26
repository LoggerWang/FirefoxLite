@file:JvmName("PromotionDialogExt")
package org.mozilla.rocket.widget

import android.content.Context
import android.media.Image
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import org.mozilla.focus.R
import org.mozilla.rocket.landing.DialogQueue

class PromotionDialog(
    private val context: Context,
    private val data: CustomViewDialogData
) {
    val view: View = View.inflate(context, R.layout.layout_promotion_dialog, null)

    private var onPositiveListener: (() -> Unit)? = null
    private var onNegativeListener: (() -> Unit)? = null
    private var onCloseListener: (() -> Unit)? = null
    private var onCancelListener: (() -> Unit)? = null
    private var onDoNotAskMeAgainListener: ((Boolean) -> Unit)? = null

    private val onShowListeners = mutableListOf<() -> Unit>()
    private val onDismissListeners = mutableListOf<() -> Unit>()
    private lateinit var image : ImageView
    private lateinit var title : TextView
    private lateinit var description : TextView
    private lateinit var positive_button : Button
    private lateinit var negative_button : Button
    private lateinit var close_button : ImageButton
    private lateinit var do_not_ask_again_checkbox : CheckBox

    private var cancellable = false

    init {
        initView()
    }

    fun onPositive(listener: () -> Unit): PromotionDialog {
        this.onPositiveListener = listener
        return this
    }

    fun onNegative(listener: () -> Unit): PromotionDialog {
        this.onNegativeListener = listener
        return this
    }

    fun onClose(listener: () -> Unit): PromotionDialog {
        this.onCloseListener = listener
        return this
    }

    fun onCancel(listener: () -> Unit): PromotionDialog {
        this.onCancelListener = listener
        return this
    }

    fun onDoNotAskMeAgain(listener: (Boolean) -> Unit): PromotionDialog {
        this.onDoNotAskMeAgainListener = listener
        return this
    }

    fun addOnShowListener(listener: () -> Unit): PromotionDialog {
        onShowListeners.add(listener)
        return this
    }

    fun addOnDismissListener(listener: () -> Unit): PromotionDialog {
        onDismissListeners.add(listener)
        return this
    }

    fun setCancellable(cancellable: Boolean): PromotionDialog {
        this.cancellable = cancellable
        return this
    }

    fun show() {
        createDialog().show()
    }

    private fun initView() {
        image = view.findViewById<ImageView>(R.id.image)
        with(image) {
            val width = data.imgWidth
            val height = data.imgHeight
            data.drawable?.let { image.setImageDrawable(it) } ?: run { image.visibility = View.GONE }
        }

        title = view.findViewById(R.id.title)
        with(title) {
            data.title?.let { text = it } ?: run { visibility = View.GONE }
        }
        description = view.findViewById(R.id.description)
        with(description) {
            data.description?.let { text = it } ?: run { visibility = View.GONE }
        }

        positive_button = view.findViewById<Button>(R.id.positive_button)
        with(positive_button) {
            data.positiveText?.let { text = it } ?: run {
                visibility = View.GONE
                (view.findViewById(R.id.button_divider1) as View).visibility = View.GONE
            }
        }

        negative_button = view.findViewById(R.id.negative_button)
        with(negative_button) {
            data.negativeText?.let { text = it } ?: run {
                visibility = View.GONE
                (view.findViewById(R.id.button_divider2) as View).visibility = View.GONE
            }
        }

        close_button = view.findViewById(R.id.close_button)
        close_button.visibility = if (data.showCloseButton) {
            View.VISIBLE
        } else {
            View.GONE
        }

        do_not_ask_again_checkbox = view.findViewById(R.id.do_not_ask_again_checkbox)
        do_not_ask_again_checkbox.visibility = if (data.showDoNotAskMeAgainButton) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun createDialog(): AlertDialog {
        val dialog = AlertDialog.Builder(context)
                .setView(view)
                .setOnCancelListener {
                    onCancelListener?.invoke()
                }
                .setCancelable(cancellable)
                .create()

        positive_button.setOnClickListener {
            dialog.dismiss()
            onPositiveListener?.invoke()
        }

        negative_button.setOnClickListener {
            dialog.dismiss()
            onNegativeListener?.invoke()
        }

        close_button.setOnClickListener {
            dialog.dismiss()
            onCloseListener?.invoke()
        }

        do_not_ask_again_checkbox.setOnClickListener {
            onDoNotAskMeAgainListener?.invoke(do_not_ask_again_checkbox.isChecked)
        }

        dialog.setOnShowListener {
            onShowListeners.forEach { it() }
        }

        dialog.setOnDismissListener {
            onDismissListeners.forEach { it() }
        }

        return dialog
    }
}

fun DialogQueue.enqueue(dialog: PromotionDialog, onShow: () -> Unit) {
    enqueue(object : DialogQueue.DialogDelegate {
        override fun setOnDismissListener(listener: () -> Unit) {
            dialog.addOnDismissListener(listener)
        }

        override fun show() {
            dialog.show()
            onShow()
        }
    })
}

@Suppress("unused")
fun DialogQueue.tryShow(dialog: PromotionDialog, onShow: () -> Unit): Boolean {
    return tryShow(object : DialogQueue.DialogDelegate {
        override fun setOnDismissListener(listener: () -> Unit) {
            dialog.addOnDismissListener(listener)
        }

        override fun show() {
            dialog.show()
            onShow()
        }
    })
}
