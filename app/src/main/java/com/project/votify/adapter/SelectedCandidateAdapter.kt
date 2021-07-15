package com.project.votify.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.votify.databinding.AddCandidateStudentRowBinding
import com.project.votify.models.User
import com.project.votify.utils.StudentHolder
import java.util.*

class SelectedCandidateAdapter(
    var studentList: ArrayList<User>,
    var context: Context,
) : RecyclerView.Adapter<StudentHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentHolder {
        val binding =
            AddCandidateStudentRowBinding.inflate(LayoutInflater.from(context), parent, false)
        return StudentHolder(binding)
    }

    override fun onBindViewHolder(holder: StudentHolder, position: Int) {
        val user: User = studentList[position]
        holder.binding.username.text = user.name.capitalize(Locale.getDefault())
        holder.binding.studentClass.text = "Class: ${user.courseYear}"
        holder.binding.studentSection.text = "Section: ${user.section}"
        if (user.profileimageurl != "null") {
            Glide.with(holder.binding.root).load(user.profileimageurl)
                .into(holder.binding.userImage)
        }    }

    override fun getItemCount(): Int {
        return studentList.size
    }

    fun update(studentList: ArrayList<User>) {
        this.studentList = studentList
        notifyDataSetChanged()

    }
}