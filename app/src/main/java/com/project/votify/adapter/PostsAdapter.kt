package com.project.votify.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.project.votify.R
import com.project.votify.models.ModelEvents
import java.util.ArrayList

class PostsAdapter(var context : Context, var arrayList: ArrayList<ModelEvents>):
    RecyclerView.Adapter<PostsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsViewHolder {
        return PostsViewHolder(LayoutInflater.from(context).inflate(R.layout.home_recycler_view_card, parent, false))
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    fun update(list: ArrayList<ModelEvents>){
        arrayList = list
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: PostsViewHolder, position: Int) {
        val eventItem = arrayList[position]

        Glide.with(context).load(eventItem.eventImage).centerCrop().dontAnimate().into(holder.image_of_event)
        holder.title_of_event.text = eventItem.eventTitle
        holder.desc_of_event.text = eventItem.eventDesc
        holder.date_of_event.text = eventItem.eventDate
        holder.created_person_name.text = "Created BY: ${eventItem.createdName}"
    }

}

class PostsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val image_of_event = itemView.findViewById<ImageView>(R.id.rvc_event_image)
    val title_of_event = itemView.findViewById<TextView>(R.id.rvc_event_title)
    val desc_of_event = itemView.findViewById<TextView>(R.id.rvc_event_desc)
    val date_of_event = itemView.findViewById<TextView>(R.id.rvc_event_date)
    val created_person_name = itemView.findViewById<TextView>(R.id.rvc_created_person_name)
    val post_card = itemView.findViewById<MaterialCardView>(R.id.rvc_post_card_view)
}
