package com.project.votify

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.*
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class RegisterStudentActivity : AppCompatActivity() {

    lateinit var course_year : String
    private val fStore = FirebaseFirestore.getInstance()
    private val fAuth = FirebaseAuth.getInstance()
    lateinit var dbRef: DatabaseReference
    lateinit var listener: ValueEventListener
    lateinit var adapter: ArrayAdapter<String>
    lateinit var spinnerDataList: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_student)

        dbRef = FirebaseDatabase.getInstance().getReference("Votify/CollegeName")
        spinnerDataList = ArrayList()
        adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, spinnerDataList)
        val disp_college_spinner = findViewById<AutoCompleteTextView>(R.id.actv_select_college_student)
        disp_college_spinner.setAdapter(adapter)
        retreiveClgNameFromDBIntoList()

        val actv_course_year_student = findViewById<AutoCompleteTextView>(R.id.actv_course_year_student)
        val nav_to_signin_page = findViewById<TextView>(R.id.navigate_to_sigin_page)
        val course_year_al = resources.getStringArray(R.array.reg_teacher_course_year)
        val arr_adap = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, course_year_al)
        val register_student_btn = findViewById<Button>(R.id.register_button_student)

        actv_course_year_student.setAdapter(arr_adap)
        actv_course_year_student.setOnItemClickListener { adapterView, view, i, l ->
            //Toast.makeText(applicationContext, actv_course_year_student.text.toString(), Toast.LENGTH_SHORT).show()
            course_year = actv_course_year_student.text.toString()
        }

        register_student_btn.setOnClickListener {
            saveStudentDeatailsToDB()
        }

        nav_to_signin_page.setOnClickListener {
            startActivity(Intent(applicationContext, SignInActivity::class.java))
            finish()
        }

    }

    private fun saveStudentDeatailsToDB() {
        //from RegisterActivity
        val student_user_name = intent.getStringExtra("user_name")
        val student_mob_no = intent.getStringExtra("mob_no")
        val student_reg_course_name = intent.getStringExtra("reg_course_name")
        val student_reg_email_id = intent.getStringExtra("reg_email_id")
        val student_reg_password = intent.getStringExtra("reg_password")
        val isTeacher = intent.getStringExtra("isTeacher")
        //from this@RegisterStudentActivity
        val student_year_of_course = course_year
        val student_section = findViewById<TextInputEditText>(R.id.reg_student_section)
        val student_roll_no = findViewById<TextInputEditText>(R.id.reg_student_roll_no)
        val stud_section = student_section.text.toString().trim()
        val student_rno = student_roll_no.text.toString().trim()
        val clgNameStud = findViewById<AutoCompleteTextView>(R.id.actv_select_college_student)
        val college_name = clgNameStud.text.toString().trim()

        if(stud_section.isEmpty() || student_rno.isEmpty()){
            Toast.makeText(applicationContext, "Enter Required Credentials", Toast.LENGTH_LONG).show()
        }
        else if(TextUtils.isEmpty(student_rno)){
            student_roll_no.error = "Enter Roll No."
        }
        else if(TextUtils.isEmpty(stud_section)){
            student_section.error = "Enter Section."
        }
        else if(college_name == ""){
            clgNameStud.error = "Select College!"
        }
        else{
            fAuth.createUserWithEmailAndPassword(student_reg_email_id, student_reg_password).addOnCompleteListener {
                if(it.isSuccessful){
                    val studentNew = HashMap<String, Any>()
                    studentNew["StudentName"] = student_user_name
                    studentNew["StudentMobileNumber"] = student_mob_no
                    studentNew["StudentCourseName"] = student_reg_course_name
                    studentNew["StudentEmailID"] = student_reg_email_id
                    studentNew["StudentCourseYear"] = student_year_of_course
                    studentNew["StudentSection"] = stud_section
                    studentNew["StudentCollegeName"] = college_name
                    studentNew["StudentRollNumber"] = student_rno
                    studentNew["isTeacher"] = isTeacher
                    studentNew["Studentuid"] = fAuth.uid.toString()

                    //FireBase
                    val fBase = FirebaseDatabase.getInstance().reference
                    fBase.child("Votify").child("Users").child(fAuth.uid.toString())
                        .setValue(studentNew)
                        .addOnSuccessListener {
                            Toast.makeText(applicationContext, "Registered Successfully!!", Toast.LENGTH_LONG).show()
                            startActivity(Intent(applicationContext, StudentHomeActivity::class.java))
                        }
                        .addOnFailureListener {
                            Toast.makeText(applicationContext, "Failed to Register!!", Toast.LENGTH_LONG).show()
                        }


                    //Firestore
    //                fStore.collection("Votify").document("Student")
    //                    .collection("$student_year_of_course $student_reg_course_name $stud_section")
    //                    .document(fAuth.uid.toString())
    //                    .set(studentNew, SetOptions.merge())
    //                    .addOnSuccessListener {
    //                        Toast.makeText(applicationContext, "Registered Successfully!!", Toast.LENGTH_LONG).show()
    //                        val intent = Intent(applicationContext, TeacherHomeActivity::class.java)
    //                        intent.putExtra("reg_course_name", student_reg_course_name)
    //                        intent.putExtra("student_year_of_course", student_year_of_course)
    //                        intent.putExtra("student_section", stud_section)
    //                        startActivity(intent)
    //                    }
    //                    .addOnFailureListener{
    //                        Toast.makeText(applicationContext, "Failed to Register!!", Toast.LENGTH_LONG).show()
    //                    }
                }
                else{
                    Toast.makeText(applicationContext, "Failed to Register into Database!!", Toast.LENGTH_LONG).show()
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