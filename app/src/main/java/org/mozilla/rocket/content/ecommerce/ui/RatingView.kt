package org.mozilla.rocket.content.ecommerce.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import org.mozilla.focus.R
import org.mozilla.focus.utils.DrawableUtils

class RatingView : LinearLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val ratingStar: Drawable?
    private val ratingBlankStar: Drawable?
    lateinit var rating_star_1: ImageView
    lateinit var rating_star_2: ImageView
    lateinit var rating_star_3: ImageView
    lateinit var rating_star_4: ImageView
    lateinit var rating_star_5: ImageView
    lateinit var rating_reviews: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_rating, this, true)
        rating_star_1 = findViewById(R.id.rating_star_1)
        rating_star_2 = findViewById(R.id.rating_star_2)
        rating_star_3 = findViewById(R.id.rating_star_3)
        rating_star_4 = findViewById(R.id.rating_star_4)
        rating_star_5 = findViewById(R.id.rating_star_5)
        rating_reviews = findViewById(R.id.rating_reviews)
        ratingStar = DrawableUtils.loadAndTintDrawable(context, R.drawable.ic_rating, ContextCompat.getColor(context, R.color.paletteDarkBlueC100))
        ratingBlankStar = DrawableUtils.loadAndTintDrawable(context, R.drawable.ic_rating_blank, ContextCompat.getColor(context, R.color.paletteDarkBlueC100))
    }

    fun updateRatingInfo(rating: Int, reviews: String) {
        rating_star_1.setImageDrawable(if (rating >= 1) ratingStar else ratingBlankStar)
        rating_star_2.setImageDrawable(if (rating >= 2) ratingStar else ratingBlankStar)
        rating_star_3.setImageDrawable(if (rating >= 3) ratingStar else ratingBlankStar)
        rating_star_4.setImageDrawable(if (rating >= 4) ratingStar else ratingBlankStar)
        rating_star_5.setImageDrawable(if (rating == 5) ratingStar else ratingBlankStar)
        rating_reviews.text = reviews
    }
}