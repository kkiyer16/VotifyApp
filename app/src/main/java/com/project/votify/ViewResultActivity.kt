package com.project.votify

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.project.votify.databinding.ActivityViewResultBinding

class ViewResultActivity : AppCompatActivity() {

    lateinit var binding : ActivityViewResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.collegeCouncilViewResult.setOnClickListener {
            startActivity(Intent(applicationContext, StudentClgCouncilViewResultActivity::class.java))
        }

        binding.classCouncilViewResult.setOnClickListener {
            startActivity(Intent(applicationContext, StudentViewResultActivity::class.java))
        }
    }
}