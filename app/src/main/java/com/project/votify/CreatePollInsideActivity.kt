package com.project.votify

import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.project.votify.adapter.cardsAdapter
import com.project.votify.databinding.ActivityCreatePollInsideBinding
import com.project.votify.models.Candidates
import com.project.votify.models.modelnames
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class CreatePollInsideActivity : AppCompatActivity() {

    lateinit var binding: ActivityCreatePollInsideBinding
    private val fBase = FirebaseDatabase.getInstance().reference
    lateinit var cardsAdap: cardsAdapter
    lateinit var arrayList: ArrayList<Any>
    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePollInsideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val candidate_uid = intent.extras?.getString("candidate_uid_ref")
        val cand_pos = intent.extras?.getString("position")
        val cand_sec = intent.extras?.getString("section")
        val cand_course_name = intent.extras?.getString("course_name")
        val cand_course_year = intent.extras?.getString("course_year")

        arrayList = ArrayList()
        binding.createPollsInsideRecyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        binding.createPollsInsideRecyclerView.layoutManager = linearLayoutManager
        cardsAdap = cardsAdapter(modelnames.candidates, 3)
        cardsAdap.set(arrayList, applicationContext)
        binding.createPollsInsideRecyclerView.adapter = cardsAdap

        fBase.child("Votify").child("Users").child(currentUser)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val clg_uid = snapshot.child("collegeuid").value.toString()
                    fBase.child("Votify").child("Institution").child(clg_uid).child("CandidateData")
                        .child(candidate_uid!!)
                        .child("participants")
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snap: DataSnapshot) {
                                arrayList.clear()
                                for(ds in snap.children){
                                    val userData = Candidates(
                                        ds.child("profile_url").value.toString(),
                                        ds.child("name").value.toString(),
                                        ds.child("course_year").value.toString(),
                                        ds.child("section").value.toString(),
                                        ds.child("uid").value.toString()
                                    )
                                    arrayList.add(userData)
                                }
                                cardsAdap.update(arrayList)
                            }

                            override fun onCancelled(err: DatabaseError) {
                            }

                        })
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

        binding.createPollsInsideStartTimeTv.setOnClickListener {
            val cal = Calendar.getInstance()
            val timesetListener = TimePickerDialog.OnTimeSetListener { _, hr, min ->
                cal.set(Calendar.HOUR_OF_DAY, hr)
                cal.set(Calendar.MINUTE, min)
                binding.cpiStartTime.text = SimpleDateFormat("HH:mm").format(cal.time)
            }
            TimePickerDialog(this, timesetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(this)).show()
        }

        binding.createPollsInsideEndTimeTv.setOnClickListener {
            val cal = Calendar.getInstance()
            val timesetListener = TimePickerDialog.OnTimeSetListener { _, hr, min ->
                cal.set(Calendar.HOUR_OF_DAY, hr)
                cal.set(Calendar.MINUTE, min)
                binding.cpiEndTime.text = SimpleDateFormat("HH:mm").format(cal.time)
            }
            TimePickerDialog(this, timesetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(this)).show()
        }

        binding.createPollButton.setOnClickListener {
            val created_on = SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().time)
            val start_time = binding.cpiStartTime.text.toString()
            val end_time = binding.cpiEndTime.text.toString()

            if (binding.cpiStartTime.text == "" || binding.cpiEndTime.text == ""){
                Toast.makeText(applicationContext, "Please Select Start Time and End Time", Toast.LENGTH_SHORT).show()
            }
            else{
                try {
                    val pollData = HashMap<String, Any>()
                    pollData["created_by"] = currentUser
                    pollData["created_on"] = created_on.toString()
                    pollData["position"] = cand_pos.toString()
                    pollData["section"] = cand_sec.toString()
                    pollData["course_name"] = cand_course_name.toString()
                    pollData["course_year"] = cand_course_year.toString()
                    pollData["candidate_data_uid"] = candidate_uid.toString()
                    pollData["poll_start_time"] = start_time
                    pollData["poll_end_time"] = end_time

                    fBase.child("Votify").child("Users").child(currentUser)
                        .addValueEventListener(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val clg_uid = snapshot.child("collegeuid").value.toString()
                                fBase.child("Votify").child("Institution").child(clg_uid)
                                    .child("Polls")
                                    .child(UUID.randomUUID().toString())
                                    .setValue(pollData)
                                    .addOnCompleteListener{
                                        if (it.isSuccessful){
                                            Toast.makeText(applicationContext, "Poll Created Successfully", Toast.LENGTH_SHORT).show()
                                            startActivity(Intent(applicationContext, TeacherHomeActivity::class.java))
                                            finish()
                                        }
                                        else{
                                            Toast.makeText(applicationContext, "Failed to Create Poll", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(applicationContext, "Failed to Create Poll", Toast.LENGTH_SHORT).show()
                                    }
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }

                        })
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }

        }
    }
}