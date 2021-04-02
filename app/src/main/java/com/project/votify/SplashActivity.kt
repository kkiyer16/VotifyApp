package com.project.votify

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Window
import android.view.WindowManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_splash)
        Handler().postDelayed({
            val intent = Intent(applicationContext, SignInActivity::class.java)
            startActivity(intent)
            finish()
        },5000)
    }

    override fun onStart() {
        super.onStart()
        if(FirebaseAuth.getInstance().currentUser != null){
            val uid = FirebaseAuth.getInstance().currentUser!!.uid
            val dbRef = FirebaseDatabase.getInstance().reference.child("Votify").child("Users").child(uid)

            dbRef.addValueEventListener(object : ValueEventListener {
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