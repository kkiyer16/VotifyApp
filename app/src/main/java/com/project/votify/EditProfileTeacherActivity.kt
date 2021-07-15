package com.project.votify

import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import com.google.api.LogDescriptor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.project.votify.databinding.ActivityEditProfileTeacherBinding
import org.w3c.dom.Text

class EditProfileTeacherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileTeacherBinding
    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
    private val fBase = FirebaseDatabase.getInstance().reference
    lateinit var ep_course_name: String
    lateinit var ep_course_year: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileTeacherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //for transparent status bar
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true)
        }
        if (Build.VERSION.SDK_INT >= 19) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = Color.TRANSPARENT
        }

        val course_name_al = resources.getStringArray(R.array.reg_course_name)
        val arr_adap = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, course_name_al)

        val actv_course_name = findViewById<AutoCompleteTextView>(R.id.course_name_ep_page)
        actv_course_name.setAdapter(arr_adap)
        actv_course_name.setOnItemClickListener { adapterView, view, i, l ->
            if (!actv_course_name.isSelected) {
                ep_course_name = actv_course_name.text.toString()
            }
            ep_course_name = actv_course_name.text.toString()
        }

        val course_year_al = resources.getStringArray(R.array.reg_teacher_course_year)
        val arr_adap1 = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, course_year_al)
        val actv_course_year = findViewById<AutoCompleteTextView>(R.id.course_year_ep_page)
        actv_course_year.setAdapter(arr_adap1)
        actv_course_year.setOnItemClickListener { adapterView, view, i, l ->
            ep_course_year = actv_course_year.text.toString()
        }
        ep_course_year = actv_course_year.text.toString()

        binding.saveChangesBtn.setOnClickListener {
            editProfileSaveChanges()
        }

    }

    private fun editProfileSaveChanges() {
        val name = binding.nameOfUserEpPage.text.toString().trim()
        val mobno = binding.mobNoEpPage.text.toString().trim()
        val section = binding.teachingSectionEpPage.text.toString().trim()
        val teacherID = binding.teacherIdEpPage.text.toString().trim()

        if(name.isEmpty() || mobno.isEmpty() || section.isEmpty() || teacherID.isEmpty()){
            Toast.makeText(applicationContext, "Provide all Credentials", Toast.LENGTH_SHORT).show()
        }
        else if(TextUtils.isEmpty(name)){
            binding.nameOfUserEpPage.error = "Name is Empty!!"
        }
        else if(TextUtils.isEmpty(mobno)){
            binding.mobNoEpPage.error = "Mobile Number is Empty!!"
        }
        else if(TextUtils.isEmpty(section)){
            binding.teachingSectionEpPage.error = "Section is Empty!!"
        }
        else if(TextUtils.isEmpty(teacherID)){
            binding.teacherIdEpPage.error = "Teacher ID is Empty!!"
        }
        else if(ep_course_name.isEmpty()){
            binding.courseNameEpPage.error = "Course Year is empty Please choose one"
        }
        else if(ep_course_year.isEmpty()){
            binding.courseYearEpPage.error = "Course Year is empty Please choose one"
        }
        else if (mobno.length > 10 || mobno.length < 10) {
            binding.mobNoEpPage.error = "Invalid Mobile Number Format"
            return
        }
        else{
            try {
                val epTeacher = HashMap<String, Any>()
                epTeacher["name"] = name
                epTeacher["mobilenumber"] = mobno
                epTeacher["coursename"] = ep_course_name
                epTeacher["courseyear"] = ep_course_year
                epTeacher["section"] = section
                epTeacher["collegeid"] = teacherID

                if(FirebaseAuth.getInstance().currentUser != null){
                    val dbRef = FirebaseDatabase.getInstance().reference.child("Votify").child("Users").child(currentUser)
                    dbRef.addValueEventListener(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val userType = snapshot.child("isTeacher").value.toString()
                            if(userType == "1"){
                                fBase.child("Votify").child("Users").child(currentUser).updateChildren(epTeacher)
                                    .addOnCompleteListener {
                                        Toast.makeText(applicationContext, "Details updated Successfully", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(applicationContext, TeacherProfileActivity::class.java))
                                        finish()
                                    }
                                    .addOnFailureListener{
                                        Toast.makeText(applicationContext, "Failed to update", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            else if(userType == "0"){
                                fBase.child("Votify").child("Users").child(currentUser).updateChildren(epTeacher)
                                    .addOnCompleteListener {
                                        Toast.makeText(applicationContext, "Details updated Successfully", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(applicationContext, StudentHomeActivity::class.java))
                                        finish()
                                    }
                                    .addOnFailureListener{
                                        Toast.makeText(applicationContext, "Failed to update", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(applicationContext, "Failed to update", Toast.LENGTH_SHORT).show()
                        }
                    })
                }

            }
            catch (e: Exception){
                Log.d("error", e.toString().trim())
            }
        }
    }

    private fun setWindowFlag(bits: Int, on: Boolean) {
        val win = window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }
}