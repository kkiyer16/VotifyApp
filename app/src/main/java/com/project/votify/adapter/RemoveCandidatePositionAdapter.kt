package com.project.votify.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.project.votify.databinding.RemoveCandidatePositionRowBinding
import com.project.votify.models.Position
import com.project.votify.utils.PositionHolder
import java.util.*

class RemoveCandidatePositionAdapter(
    var positionList: ArrayList<Position>,
    var context: Context
) :
    RecyclerView.Adapter<PositionHolder>() {
    private var originalList: ArrayList<Position> = positionList
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PositionHolder {
        val binding =
            RemoveCandidatePositionRowBinding.inflate(LayoutInflater.from(context), parent, false)
        return PositionHolder(binding)
    }

    override fun onBindViewHolder(holder: PositionHolder, position: Int) {
        val positionData: Position = positionList[position]
        holder.binding.positionName.text = positionData.position.capitalize(Locale.getDefault())
        holder.binding.studentClass.text = "Class: ${positionData.courseYear}"
        holder.binding.studentSection.text = "Section: ${positionData.section}"
        holder.binding.createdOn.text = "Created On: ${positionData.createdOn}"
        holder.binding.remove.setOnClickListener {
            removePosition(positionData)
        }
    }

    private fun removePosition(positionData: Position) {
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
        databaseReference.child("Votify").child("Institution").child(positionData.collegeUid)
            .child("CandidateData")
            .child(positionData.postionUid).removeValue().addOnSuccessListener {

            }
    }

    fun performFiltering(constraint: CharSequence) {
        val queryString = constraint.toString()
        if (queryString.isEmpty()) {
            positionList = originalList
        } else {
            val results = ArrayList<Position>()
            for (g: Position in originalList) {
                if (g.position.toLowerCase(Locale.getDefault())
                        .contains(queryString.toLowerCase(Locale.getDefault()))
                ) {
                    results.add(g)
                }
            }
            positionList = results
        }
        update(positionList)
    }

    override fun getItemCount(): Int {
        return positionList.size
    }

    fun update(positionList: ArrayList<Position>) {
        this.positionList = positionList
        notifyDataSetChanged()

    }
}