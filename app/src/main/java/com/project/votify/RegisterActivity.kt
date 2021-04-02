package com.project.votify

import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.annotation.RequiresApi
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {


    lateinit var reg_course_name: String
    private var isTeacher = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val chkbox_teacher = findViewById<CheckBox>(R.id.register_checkbox_teacher)
        val chkbox_student = findViewById<CheckBox>(R.id.register_checkbox_student)
        val nav_to_signin_page = findViewById<TextView>(R.id.navigate_to_sigin_page)
        val reg_fwd_btn = findViewById<Button>(R.id.register_fwd_button)
        val course_name_al = resources.getStringArray(R.array.reg_course_name)
        val arr_adap = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, course_name_al)

        val actv_course_name = findViewById<AutoCompleteTextView>(R.id.actv_course_name)
        actv_course_name.setAdapter(arr_adap)
        actv_course_name.setOnItemClickListener { adapterView, view, i, l ->
            if (!actv_course_name.isSelected) {
                Toast.makeText(applicationContext, "Please Select Course Name", Toast.LENGTH_SHORT)
                    .show()
            }
            reg_course_name = actv_course_name.text.toString()
        }

        chkbox_teacher.setOnCheckedChangeListener { compoundButton, b ->
            if (compoundButton.isChecked) {
                chkbox_student.isChecked = false
            }
        }
        chkbox_student.setOnCheckedChangeListener { compoundButton, b ->
            if (compoundButton.isChecked) {
                chkbox_teacher.isChecked = false
            }
        }

        reg_fwd_btn.setOnClickListener {
            sendToFinalRegisterPage()
        }

        nav_to_signin_page.setOnClickListener {
            startActivity(Intent(applicationContext, SignInActivity::class.java))
            finish()
        }
    }

    private fun sendToFinalRegisterPage() {
        val name = findViewById<TextInputEditText>(R.id.name_of_user_reg_page)
        val mob = findViewById<TextInputEditText>(R.id.mob_no_of_user_reg_page)
        val email = findViewById<TextInputEditText>(R.id.email_id_of_user_reg_page)
        val pwd = findViewById<TextInputEditText>(R.id.password_of_user_reg_page)

        val user_name = name.text.toString().trim()
        val mob_no = mob.text.toString().trim()
        val reg_email_id = email.text.toString().trim()
        val reg_password = pwd.text.toString().trim()

        if (user_name.isEmpty() || mob_no.isEmpty() || reg_email_id.isEmpty() || reg_password.isEmpty()) {
            Toast.makeText(this, "Enter Required Credentials ", Toast.LENGTH_LONG).show()
        }
        else if (TextUtils.isEmpty(user_name)) {
            name.error = "User name is Required"
            return
        }
        else if (TextUtils.isEmpty(mob_no)) {
            mob.error = "Mobile Number is Required"
            return
        }
        else if (TextUtils.isEmpty(reg_email_id)) {
            email.error = "User name is Required"
            return
        }
        else if (TextUtils.isEmpty(reg_password)) {
            pwd.error = "User name is Required"
            return
        }
        else if (mob_no.length > 10 || mob_no.length < 10) {
            mob.error = "Invalid Mobile Number Format"
            return
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches()){
            email.error = "Invalid Email ID"
        }
        else {
            val chkbox_teacher = findViewById<CheckBox>(R.id.register_checkbox_teacher)
            val chkbox_student = findViewById<CheckBox>(R.id.register_checkbox_student)
            if (chkbox_teacher.isChecked) {
                isTeacher = 1;
                val intent = Intent(applicationContext, RegisterTeacherActivity::class.java)
                intent.putExtra("user_name", user_name)
                intent.putExtra("mob_no", mob_no)
                intent.putExtra("reg_course_name", reg_course_name)
                intent.putExtra("reg_email_id", reg_email_id)
                intent.putExtra("reg_password", reg_password)
                intent.putExtra("isTeacher", isTeacher.toString().trim())
                startActivity(intent)
                finish()
            }
            if (chkbox_student.isChecked) {
                isTeacher = 0;
                val intent = Intent(applicationContext, RegisterStudentActivity::class.java)
                intent.putExtra("user_name", user_name)
                intent.putExtra("mob_no", mob_no)
                intent.putExtra("reg_course_name", reg_course_name)
                intent.putExtra("reg_email_id", reg_email_id)
                intent.putExtra("reg_password", reg_password)
                intent.putExtra("isTeacher", isTeacher.toString().trim())
                startActivity(intent)
                finish()
            }
        }
    }
}