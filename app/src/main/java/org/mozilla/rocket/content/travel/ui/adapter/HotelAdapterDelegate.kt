package org.mozilla.rocket.content.travel.ui.adapter

import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import org.mozilla.focus.R
import org.mozilla.focus.glide.GlideApp
import org.mozilla.rocket.adapter.AdapterDelegate
import org.mozilla.rocket.adapter.DelegateAdapter
import org.mozilla.rocket.content.travel.ui.TravelCityViewModel
import org.mozilla.rocket.extension.obtainBackgroundColor
import java.text.DecimalFormat

class HotelAdapterDelegate(private val travelCityViewModel: TravelCityViewModel) : AdapterDelegate {
    override fun onCreateViewHolder(view: View): DelegateAdapter.ViewHolder =
            HotelViewHolder(view, travelCityViewModel)
}

class HotelViewHolder(
    override val containerView: View,
    private val travelCityViewModel: TravelCityViewModel
) : DelegateAdapter.ViewHolder(containerView) {

    var hotel_source: TextView = containerView.findViewById(R.id.hotel_source)
    var hotel_name: TextView = containerView.findViewById(R.id.hotel_name)
    var hotel_description: TextView = containerView.findViewById(R.id.hotel_description)
    var hotel_rating: TextView = containerView.findViewById(R.id.hotel_rating)
    var hotel_currency: TextView = containerView.findViewById(R.id.hotel_currency)
    var hotel_price: TextView = containerView.findViewById(R.id.hotel_price)
    var hotel_free_wifi: TextView = containerView.findViewById(R.id.hotel_free_wifi)
    var hotel_no_creditcard_required: TextView = containerView.findViewById(R.id.hotel_no_creditcard_required)
    var hotel_pay_at_hotel: TextView = containerView.findViewById(R.id.hotel_pay_at_hotel)
    var hotel_extras: LinearLayout = containerView.findViewById(R.id.hotel_extras)
    var hotel_image: ImageView = containerView.findViewById(R.id.hotel_image)
    override fun bind(uiModel: DelegateAdapter.UiModel) {
        val hotelUiModel = uiModel as HotelUiModel

        GlideApp.with(itemView.context)
                .asBitmap()
                .load(hotelUiModel.imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.placeholder)
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(e: GlideException?, model: Any, target: com.bumptech.glide.request.target.Target<Bitmap>, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Bitmap?, model: Any, target: com.bumptech.glide.request.target.Target<Bitmap>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        if (resource != null) {
                            hotel_image.setBackgroundColor(resource.obtainBackgroundColor())
                        }
                        return false
                    }
                })
                .into(hotel_image)

        hotel_source.text = hotelUiModel.sourceName
        hotel_name.text = hotelUiModel.name
        hotel_description.text = hotelUiModel.description

        hotel_rating.text = itemView.context.getString(R.string.travel_hotel_rating, hotelUiModel.rating, hotelUiModel.fullScore)
        hotel_rating.isInvisible = hotelUiModel.rating.isNaN()

        hotel_currency.text = hotelUiModel.currency

        val dec = DecimalFormat("#,###.##")
        hotel_price.text = dec.format(hotelUiModel.price)

        hotel_free_wifi.isVisible = hotelUiModel.hasFreeWifi

        hotel_no_creditcard_required.isVisible = !hotelUiModel.creditCardRequired
        hotel_pay_at_hotel.isVisible = hotelUiModel.canPayAtProperty
        hotel_extras.isVisible = !hotelUiModel.creditCardRequired || hotelUiModel.canPayAtProperty

        itemView.setOnClickListener { travelCityViewModel.onHotelClicked(hotelUiModel) }
    }
}

data class HotelUiModel(
    val imageUrl: String,
    val sourceName: String,
    val name: String,
    val description: String,
    val rating: Float,
    val creditCardRequired: Boolean,
    val fullScore: Int,
    val hasFreeWifi: Boolean,
    val price: Float,
    val currency: String,
    val canPayAtProperty: Boolean,
    val linkUrl: String,
    val source: String
) : DelegateAdapter.UiModel()