package com.project.votify

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TeacherHomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_home)

        val sign_out = findViewById<Button>(R.id.teacher_sign_out)
        sign_out.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(applicationContext, "Logged Out!", Toast.LENGTH_LONG).show()
            startActivity(Intent(applicationContext, SignInActivity::class.java))
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        if(FirebaseAuth.getInstance().currentUser == null){
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }
}