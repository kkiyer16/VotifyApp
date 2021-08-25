package com.project.votify.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.votify.databinding.AddCandidateStudentRowBinding
import com.project.votify.models.ChangeValue
import com.project.votify.models.User
import com.project.votify.utils.StudentHolder
import java.util.*

class CollegeCouncilStudentAdapter(var studentList: ArrayList<User>, var context: Context, var changeListner: ChangeValue) :
    RecyclerView.Adapter<StudentHolder>(){
    var itemSelected: Int = 0
    var commonCourseYear = ""
    var commonSection = ""
    private var originalList: ArrayList<User> = studentList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentHolder {
        val binding = AddCandidateStudentRowBinding.inflate(LayoutInflater.from(context), parent, false)
        return StudentHolder(binding)
    }

    override fun onBindViewHolder(holder: StudentHolder, position: Int) {
        val user: User = studentList[position]
        holder.binding.username.text = user.name.capitalize(Locale.getDefault())
        holder.binding.studentClass.text = "Class: ${user.courseYear}"
        holder.binding.studentSection.text = "Section: ${user.section}"
        if (user.profileimageurl != "null") {
            Glide.with(holder.binding.root).load(user.profileimageurl).into(holder.binding.userImage)
        }
        holder.binding.rowLayout.setOnLongClickListener {
            if (!user.isSelected) {
//                if (itemSelected == 0) {
//                    commonCourseYear = user.courseYear
//                    commonSection = user.section
//                }
//                if (user.courseYear == commonCourseYear && user.section == commonSection) {
                    user.isSelected = true
                    println("Data inside ${user.isSelected} $itemSelected")
                    holder.binding.rowCard.setBackgroundColor(Color.parseColor("#73B9EF"))
                    holder.binding.rowLayout.setBackgroundColor(Color.parseColor("#73B9EF"))
                    holder.binding.rowLayout.alpha = 0.7f
                    itemSelected += 1
//                } else {
//                    Toast.makeText(context, "You can only select common class and section candidates", Toast.LENGTH_SHORT).show()
//                }

            } else {
                holder.binding.rowCard.setBackgroundColor(Color.WHITE)
                holder.binding.rowLayout.setBackgroundColor(Color.WHITE)
                holder.binding.rowLayout.alpha = 1.0F
                itemSelected -= 1;
                user.isSelected = false
            }
            if (itemSelected >= 2) {
                changeListner.onChange(true)
            } else {
                changeListner.onChange(false)
            }

            true
        }

    }

    override fun getItemCount(): Int {
        return studentList.size
    }

    fun update(studentList: ArrayList<User>) {
        this.studentList = studentList
        notifyDataSetChanged()

    }

    fun updateToOriginal() {
        this.studentList = originalList
        notifyDataSetChanged()
    }

    fun getCurrentStudentList(): ArrayList<User> {
        return this.studentList
    }

    fun filterBySection(section: String) {
        val results = ArrayList<User>()
        studentList.forEach { user ->
            if (user.section == section) {
                results.add(user)
            }
        }
        studentList.removeAll(results)
        studentList.addAll(0, results)
        notifyDataSetChanged()
    }

    fun performFiltering(constraint: CharSequence) {
        val queryString = constraint.toString()
        if (queryString.isEmpty()) {
            studentList = originalList
        } else {
            val results = ArrayList<User>()
            for (g: User in originalList) {
                if (g.name.toLowerCase(Locale.getDefault())
                        .contains(queryString.toLowerCase(Locale.getDefault()))
                ) {
                    results.add(g)
                }
            }
            studentList = results
        }
        update(studentList)
    }

    fun filterByClass(classGroup: String) {
        val results = ArrayList<User>()
        studentList.forEach { user ->
            if (user.courseYear == classGroup) {
                results.add(user)
            }
        }
        studentList.removeAll(results)
        studentList.addAll(0, results)
        notifyDataSetChanged()
    }
}