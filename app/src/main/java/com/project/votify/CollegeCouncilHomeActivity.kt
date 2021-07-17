package com.project.votify

import android.content.Intent
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.project.votify.databinding.ActivityCollegeCouncilHomeBinding

class CollegeCouncilHomeActivity : AppCompatActivity() {

    lateinit var binding: ActivityCollegeCouncilHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCollegeCouncilHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.title = ""
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            toolbar.overflowIcon!!.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP);
        }

        binding.collegeCouncilAddCandidates.setOnClickListener {
            startActivity(Intent(applicationContext, CollegeCouncilAddCandidateActivity::class.java))
        }
    }
}