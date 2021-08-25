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
import com.project.votify.databinding.ActivityClassCouncilPollsActicityBinding
import com.project.votify.models.PositionData
import com.project.votify.models.modelnames

class ClassCouncilPollsActicity : AppCompatActivity() {

    lateinit var binding: ActivityClassCouncilPollsActicityBinding
    private val fBase = FirebaseDatabase.getInstance().reference
    lateinit var cardsAdap: cardsAdapter
    lateinit var arrayList : ArrayList<Any>
    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClassCouncilPollsActicityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        arrayList = ArrayList()
        binding.fragPollsRecyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        binding.fragPollsRecyclerView.layoutManager = linearLayoutManager
        cardsAdap = cardsAdapter(modelnames.fragpollpos, 1)
        cardsAdap.set(arrayList, applicationContext)
        binding.fragPollsRecyclerView.adapter = cardsAdap

        val dbRef = fBase.child("Votify").child("Users").child(currentUser)
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val clg_uid = snapshot.child("collegeuid").value.toString()
                val courseName = snapshot.child("coursename").value.toString()
                fBase.child("Votify").child("Institution").child(clg_uid).child("Polls")
                    .orderByChild("course_name").equalTo(courseName)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            arrayList.clear()

                            for (childSnapshot: DataSnapshot in snapshot.children) {
                                val positionData = PositionData(
                                    childSnapshot.child("position").value.toString(),
                                    childSnapshot.child("course_name").value.toString(),
                                    childSnapshot.child("course_year").value.toString(),
                                    childSnapshot.child("section").value.toString(),
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