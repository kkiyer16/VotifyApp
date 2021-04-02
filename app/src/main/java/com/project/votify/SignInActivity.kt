package com.project.votify

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SignInActivity : AppCompatActivity() {

    lateinit var fAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        fAuth = FirebaseAuth.getInstance()

        val login_btn = findViewById<MaterialButton>(R.id.login_button)
        login_btn.setOnClickListener {
            loginUser()
        }

        val nav_to_sign_up = findViewById<TextView>(R.id.navigate_to_signup_page)
        nav_to_sign_up.setOnClickListener{
            startActivity(Intent(applicationContext, RegisterActivity::class.java))
            finish()
        }
    }

    private fun loginUser() {
        val email_id = findViewById<TextInputEditText>(R.id.email_id_login)
        val pwd_lg = findViewById<TextInputEditText>(R.id.password_login)
        val email = email_id.text.toString().trim()
        val pwd = pwd_lg.text.toString().trim()

        if(email.isEmpty() || pwd.isEmpty()){
            Toast.makeText(applicationContext, "Enter Required Credentials ", Toast.LENGTH_LONG).show()
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email_id.text.toString()).matches()){
            email_id.error = "Invalid Email ID"
            email_id.requestFocus()
            return
        }
        else if(TextUtils.isEmpty(email)){
            email_id.error = "Enter Email ID"
            return
        }
        else if(TextUtils.isEmpty(pwd)){
            pwd_lg.error = "Enter Password"
            return
        }
        else{
            fAuth.signInWithEmailAndPassword(email, pwd)
                .addOnCompleteListener{
                    if (it.isSuccessful) {
                        Toast.makeText(applicationContext, "Login Successful!!", Toast.LENGTH_LONG).show()
                        Toast.makeText(applicationContext, "Welcome to Votify!!", Toast.LENGTH_LONG).show()
                        checkUser(FirebaseAuth.getInstance().currentUser!!.uid)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(applicationContext, it.message.toString().trim(), Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun checkUser(uid: String) {
        val dbRef = FirebaseDatabase.getInstance().reference.child("Votify").child("Users").child(uid)
        dbRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val userType = snapshot.child("isTeacher").value.toString()
                if(userType == "1"){
                    startActivity(Intent(applicationContext, TeacherHomeActivity::class.java))
                    finish()
                }
                else if(userType == "0"){
                    startActivity(Intent(applicationContext, StudentHomeActivity::class.java))
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message.trim(), Toast.LENGTH_SHORT).show()
            }

        })

    }

    override fun onStart() {
        super.onStart()
        if(FirebaseAuth.getInstance().currentUser != null){
            val uid = FirebaseAuth.getInstance().currentUser!!.uid
            val dbRef = FirebaseDatabase.getInstance().reference.child("Votify").child("Users").child(uid)

            dbRef.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userType = snapshot.child("isTeacher").value.toString()
                    if(userType == "1"){
                        startActivity(Intent(applicationContext, TeacherHomeActivity::class.java))
                        finish()
                    }
                    else if(userType == "0"){
                        startActivity(Intent(applicationContext, StudentHomeActivity::class.java))
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(applicationContext, SignInActivity::class.java))
                    finish()
                }
            })
        }
    }
}