package com.husenansari.mykotlindemo.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.hashtechhub.mrhomeexpert.R
import com.hashtechhub.mrhomeexpert.custom.TfTextView
import com.hashtechhub.mrhomeexpert.api.model.Timeslot

class RecycleAdapterLOGICINONBINDVIEW(context: Context, dateTimeInfo: ArrayList<Timeslot>) :
    RecyclerView.Adapter<RecycleAdapterLOGICINONBINDVIEW.MyViewHolder?>() {
    var row_index : Int = -1
    //var row_index : Int = -1 if don't want to check first item
    var context: Context
    private var OfferList: ArrayList<Timeslot> = dateTimeInfo
    private var dateClickListener: onDateClickListener? = null

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var txtDateTime: TextView = view.findViewById<View>(R.id.txtDateTime) as TfTextView
        var llDateBackground: LinearLayout = view.findViewById<View>(R.id.llDateBackground) as LinearLayout
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_date_list, parent, false)
        dateClickListener = context as onDateClickListener
        return MyViewHolder(itemView)
    }


    interface onDateClickListener {
        fun onItemDateClickListener(timeSlot: Timeslot)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        @NonNull holder: MyViewHolder,
        position: Int
    ) {
        val lists: Timeslot = OfferList[position]

        holder.txtDateTime.text = lists.date + "\n"+ lists.day

        holder.itemView.setOnClickListener {
            row_index = position;
            this.dateClickListener!!.onItemDateClickListener(lists)
            notifyDataSetChanged();
        }

        if(row_index==position){
            holder.llDateBackground.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.rect_date_time_fill))
            holder.txtDateTime.setTextColor((ContextCompat.getColor(context, R.color.white)))
        }
        else
        {
            holder.llDateBackground.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.rect_date_time))
            holder.txtDateTime.setTextColor((ContextCompat.getColor(context, R.color.textColor)))
        }
    }

    override fun getItemCount(): Int {
        return OfferList.size
    }

    fun setTimeSlot(categoryinfo: ArrayList<Timeslot>) {
        this.OfferList = categoryinfo
        notifyDataSetChanged()
    }

    init {
        this.context = context
    }
}


