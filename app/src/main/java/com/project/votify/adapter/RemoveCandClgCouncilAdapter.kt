package com.project.votify.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.project.votify.databinding.ClgCouncilRemoveCandPositionRowBinding
import com.project.votify.databinding.RemoveCandidatePositionRowBinding
import com.project.votify.models.Position
import com.project.votify.utils.PositionClgCouncilHolder
import com.project.votify.utils.PositionHolder
import java.util.*

class RemoveCandClgCouncilAdapter(var positionList: ArrayList<Position>, var context: Context) :
    RecyclerView.Adapter<PositionClgCouncilHolder>() {

    private var originalList: ArrayList<Position> = positionList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PositionClgCouncilHolder {
        val binding = ClgCouncilRemoveCandPositionRowBinding.inflate(LayoutInflater.from(context), parent, false)
        return PositionClgCouncilHolder(binding)
    }

    override fun onBindViewHolder(holder: PositionClgCouncilHolder, position: Int) {
        val positionData: Position = positionList[position]
        holder.binding.positionName.text = positionData.position.capitalize(Locale.getDefault())
        holder.binding.createdOn.text = "Created On: ${positionData.createdOn}"
        holder.binding.remove.setOnClickListener {
            removePosition(positionData)
        }
    }

    private fun removePosition(positionData: Position) {
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
        databaseReference.child("Votify").child("Institution").child(positionData.collegeUid)
            .child("CollegeCouncil").child("CandidateData").child(positionData.postionUid).removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Removed Successfully", Toast.LENGTH_SHORT).show()
            }
    }

    fun performFiltering(constraint: CharSequence) {
        val queryString = constraint.toString()
        if (queryString.isEmpty()) {
            positionList = originalList
        } else {
            val results = ArrayList<Position>()
            for (g: Position in originalList) {
                if (g.position.toLowerCase(Locale.getDefault()).contains(queryString.toLowerCase(Locale.getDefault()))) {
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