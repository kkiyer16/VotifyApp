package com.project.votify

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.project.votify.adapter.cardsAdapter
import com.project.votify.databinding.ActivityClgCouncilPollsActicityBinding
import com.project.votify.models.CCPositionData
import com.project.votify.models.PositionData
import com.project.votify.models.modelnames

class ClgCouncilPollsActicity : AppCompatActivity() {

    lateinit var binding: ActivityClgCouncilPollsActicityBinding
    private val fBase = FirebaseDatabase.getInstance().reference
    lateinit var cardsAdap: cardsAdapter
    lateinit var arrayList : ArrayList<Any>
    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClgCouncilPollsActicityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        arrayList = ArrayList()
        binding.clgCouncilPollsRecyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        binding.clgCouncilPollsRecyclerView.layoutManager = linearLayoutManager
        cardsAdap = cardsAdapter(modelnames.clgcouncilfragpollpos, 5)
        cardsAdap.set(arrayList, applicationContext)
        binding.clgCouncilPollsRecyclerView.adapter = cardsAdap

        val dbRef = fBase.child("Votify").child("Users").child(currentUser)
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val clg_uid = snapshot.child("collegeuid").value.toString()
                val courseName = snapshot.child("coursename").value.toString()
                fBase.child("Votify").child("Institution").child(clg_uid).child("CollegeCouncil")
                    .child("Polls").orderByChild("course_name").equalTo(courseName)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            arrayList.clear()

                            for (childSnapshot: DataSnapshot in snapshot.children) {
                                val positionData = CCPositionData(
                                    childSnapshot.child("position").value.toString(),
                                    childSnapshot.child("course_name").value.toString(),
                                    childSnapshot.child("candidate_data_uid").value.toString()
                                )
                                positionData.pollUid=childSnapshot.key.toString()
                                positionData.starttime=childSnapshot.child("poll_start_time").value.toString()
                                positionData.endtime=childSnapshot.child("poll_end_time").value.toString()
                                positionData.collegeUid=clg_uid
                                arrayList.add(positionData)
                            }
                            cardsAdap.update(arrayList)
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
}