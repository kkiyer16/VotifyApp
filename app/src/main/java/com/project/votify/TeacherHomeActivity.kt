package com.project.votify

import android.content.Intent
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.project.votify.databinding.ActivityTeacherHomeBinding

class TeacherHomeActivity : AppCompatActivity() {

    private lateinit var binding : ActivityTeacherHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.title = ""
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            toolbar.overflowIcon!!.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP);
        }

        binding.createPolls.setOnClickListener {  }

        binding.removeCandidate.setOnClickListener {  }

        binding.createPolls.setOnClickListener {
            startActivity(Intent(applicationContext, TeacherCreatePollActivity::class.java))
        }

        binding.displayResults.setOnClickListener {
            startActivity(Intent(applicationContext, TeacherViewResultActivity::class.java))
        }

        binding.addPost.setOnClickListener {
            startActivity(Intent(applicationContext, TeacherAddPostActivity::class.java))
        }

        binding.teacherProfile.setOnClickListener {
            startActivity(Intent(applicationContext, TeacherProfileActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.side_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.sign_out_teacher->{
                AlertDialog.Builder(this).apply {
                    setTitle("Are you sure?")
                    setPositiveButton("OK"){ _, _ ->
                        FirebaseAuth.getInstance().signOut()
                        Toast.makeText(applicationContext, "Logged Out!", Toast.LENGTH_LONG).show()
                        startActivity(Intent(applicationContext, SignInActivity::class.java))
                        finish()
                    }
                    setNegativeButton("Cancel"){ _, _ ->
                    }
                }.create().show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        if(FirebaseAuth.getInstance().currentUser == null){
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }
}