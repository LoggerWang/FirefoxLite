package org.mozilla.focus.vpn

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import de.blinkt.openvpn.model.ZoneBean
import org.mozilla.focus.R
import org.mozilla.focus.glide.GlideApp

class ServerAdapter(
    private val nodeList: ArrayList<ZoneBean>,
    private val iOnItemClick: IOnItemClick,
    private var mSelectPosition: Int = 0
) : RecyclerView.Adapter<SortTypeHolder>() {

    private var mPreSelectPosition = 0


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SortTypeHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_zone_msg, parent, false)
    )

    override fun onBindViewHolder(holder: SortTypeHolder, position: Int) {
        val nodeBean = nodeList[position]
        val ivZoneIcon = holder.itemView.findViewById<ImageView>(R.id.ivZoneIcon)
        val tvZoneName = holder.itemView.findViewById<TextView>(R.id.tvZoneName)
        val ivZoneState = holder.itemView.findViewById<ImageView>(R.id.ivZoneState)
        tvZoneName.text = nodeBean.zone_name
        ivZoneIcon.setImageResource(
            when (nodeBean.zone_name) {
                "America", "america" -> R.mipmap.net_america;"Asia", "asia" -> R.mipmap.net_asia
                "Europe", "europe" -> R.mipmap.net_europe;else -> R.mipmap.net_europe
            }
        )
        if (position == mSelectPosition) ivZoneState.setImageResource(R.mipmap.zone_select)
        else ivZoneState.setImageResource(R.mipmap.zone_unselect)

        holder.itemView.setOnClickListener {
            iOnItemClick.iOnItemClick(position, nodeBean)
            setSelectPosition(position)
        }
    }

    override fun getItemCount(): Int = nodeList.size
    private fun setSelectPosition(selectPosition: Int) {
        mPreSelectPosition = mSelectPosition
        mSelectPosition = selectPosition
        notifyItemChanged(mPreSelectPosition)
        notifyItemChanged(mSelectPosition)
    }
}

class SortTypeHolder(view: View) : RecyclerView.ViewHolder(view)

interface IOnItemClick {
    fun iOnItemClick(position: Int, nodeBean: ZoneBean)
}