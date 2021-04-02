package com.project.votify

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class RegisterTeacherActivity : AppCompatActivity() {

    lateinit var course_year: String
    private val fStore = FirebaseFirestore.getInstance()
    //lateinit var actv_course_year : AutoCompleteTextView
    lateinit var dbRef: DatabaseReference
    private val fAuth = FirebaseAuth.getInstance()
    lateinit var listener: ValueEventListener
    lateinit var adapter: ArrayAdapter<String>
    lateinit var spinnerDataList: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_teacher)

        //add and Display College Spinner
        dbRef = FirebaseDatabase.getInstance().getReference("Votify/CollegeName")
        spinnerDataList = ArrayList()
        adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, spinnerDataList)
        val disp_college_spinner = findViewById<AutoCompleteTextView>(R.id.select_college_spinner)
        disp_college_spinner.setAdapter(adapter)
        retreiveClgNameFromDBIntoList()

        //display course year Spinner
        val course_year_al = resources.getStringArray(R.array.reg_teacher_course_year)
        val arr_adap = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, course_year_al)
        val actv_course_year = findViewById<AutoCompleteTextView>(R.id.actv_course_year)
        actv_course_year.setAdapter(arr_adap)
        actv_course_year.setOnItemClickListener { adapterView, view, i, l ->
            course_year = actv_course_year.text.toString()
        }
        course_year = actv_course_year.text.toString()

        val register_teacher_btn = findViewById<Button>(R.id.register_button_teacher)
        register_teacher_btn.setOnClickListener {
            saveTeacherDetailsToDB()
        }

        //to add college dialog
        val add_btn = findViewById<ImageButton>(R.id.add_college_btn)
        add_btn.setOnClickListener {
            val dialog = popup_window()
            val fm = supportFragmentManager.beginTransaction()
            dialog.show(fm, "AddCollegeDialog")
        }

        val nav_to_signin_page = findViewById<TextView>(R.id.navigate_to_sigin_page)
        nav_to_signin_page.setOnClickListener {
            startActivity(Intent(applicationContext, SignInActivity::class.java))
            finish()
        }
    }

    private fun saveTeacherDetailsToDB() {
        //from RegisterActivity
        val user_name = intent.getStringExtra("user_name")
        val mob_no = intent.getStringExtra("mob_no")
        val reg_course_name = intent.getStringExtra("reg_course_name")
        val reg_email_id = intent.getStringExtra("reg_email_id")
        val reg_password = intent.getStringExtra("reg_password")
        val isTeacher = intent.getStringExtra("isTeacher")
        //from this@RegisterTeacherActivity
        val year_of_course = course_year
        val clgName = findViewById<AutoCompleteTextView>(R.id.select_college_spinner)
        val college_name = clgName.text.toString().trim()
        val section = findViewById<TextInputEditText>(R.id.section_teacher_page)
        val teacher_id = findViewById<TextInputEditText>(R.id.teacher_id_teacher_page)
        val teacher_section = section.text.toString().trim()
        val reg_teacher_id = teacher_id.text.toString().trim()

        if (teacher_section.isEmpty() || reg_teacher_id.isEmpty() || college_name.isEmpty()) {
            Toast.makeText(applicationContext, "Enter Required Credentials", Toast.LENGTH_LONG).show()
        }
        else if (college_name == "") {
            clgName.error = "Select College Name"
        }
        else if (TextUtils.isEmpty(reg_teacher_id)) {
            teacher_id.error = "Enter ID."
        }
        else if (TextUtils.isEmpty(teacher_section)) {
            section.error = "Enter Section."
        }
        else {
            fAuth.createUserWithEmailAndPassword(reg_email_id, reg_password).addOnCompleteListener {
                if (it.isSuccessful) {
                    val teacherNew = HashMap<String, Any>()
                    teacherNew["TeacherName"] = user_name
                    teacherNew["TeacherMobileNumber"] = mob_no
                    teacherNew["TeachingCourseName"] = reg_course_name
                    teacherNew["TeacherEmailID"] = reg_email_id
                    teacherNew["TeachingCourseYear"] = year_of_course
                    teacherNew["TeacherCollegeName"] = college_name
                    teacherNew["TeachingSection"] = teacher_section
                    teacherNew["TeacherID"] = reg_teacher_id
                    teacherNew["isTeacher"] = isTeacher
                    teacherNew["Teacheruid"] = fAuth.uid.toString()

                    //FireBase
                    val fBase = FirebaseDatabase.getInstance().reference
                    fBase.child("Votify").child("Users").child(fAuth.uid.toString())
                        .setValue(teacherNew)
                        .addOnSuccessListener {
                            Toast.makeText(applicationContext, "Registered Successfully!!", Toast.LENGTH_LONG).show()
                            startActivity(Intent(applicationContext, TeacherHomeActivity::class.java))
                        }
                        .addOnFailureListener {
                            Toast.makeText(applicationContext, "Failed to Register!!", Toast.LENGTH_LONG).show()
                        }


//                //FireStore
//                fStore.collection("Votify").document("Teacher")
//                    .collection("$year_of_course $reg_course_name $teacher_section")
//                    .document(fAuth.uid.toString())
//                    .set(teacherNew, SetOptions.merge())
//                    .addOnSuccessListener {
//                        Toast.makeText(
//                            applicationContext,
//                            "Registered Successfully!!",
//                            Toast.LENGTH_LONG
//                        ).show()
//                        val intent = Intent(applicationContext, TeacherHomeActivity::class.java)
//                        intent.putExtra("reg_course_name", reg_course_name)
//                        intent.putExtra("year_of_course", year_of_course)
//                        intent.putExtra("teacher_section", teacher_section)
//                        startActivity(intent)
//                    }
//                    .addOnFailureListener{
//                        Toast.makeText(
//                            applicationContext,
//                            "Failed to Register!!",
//                            Toast.LENGTH_LONG
//                        ).show()
//                    }
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Failed to Register into Database!!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun retreiveClgNameFromDBIntoList() {
        listener = dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                spinnerDataList.clear()
                for (item in snapshot.children) {
                    spinnerDataList.add(item.value.toString())
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

}