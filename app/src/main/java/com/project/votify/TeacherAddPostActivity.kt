package com.project.votify

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.project.votify.adapter.FragmentAdapter
import com.project.votify.databinding.ActivityTeacherAddPostBinding

class TeacherAddPostActivity : AppCompatActivity() {

    lateinit var binding : ActivityTeacherAddPostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherAddPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpTabs()
    }

    private fun setUpTabs() {
        val adapter = FragmentAdapter(supportFragmentManager)
        adapter.addFragment(TeacherAddPostFragment())
        adapter.addFragment(TeacherViewPostFragment())
        binding.teacherAddPostViewpager.adapter = adapter
        binding.teacherAddPostTabLayout.setupWithViewPager(binding.teacherAddPostViewpager)
        binding.teacherAddPostTabLayout.getTabAt(0)!!.text = "Add Post"
        binding.teacherAddPostTabLayout.getTabAt(1 )!!.text = "View Post"

    }
}