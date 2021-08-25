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
import com.project.votify.databinding.ActivityStudentClgCouncilViewResultBinding
import com.project.votify.models.ClgCouncilWinner
import com.project.votify.models.Winner
import com.project.votify.models.modelnames

class StudentClgCouncilViewResultActivity : AppCompatActivity() {

    lateinit var binding: ActivityStudentClgCouncilViewResultBinding
    private val fBase = FirebaseDatabase.getInstance().reference
    lateinit var cardsAdap: cardsAdapter
    lateinit var arrayList: ArrayList<Any>
    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentClgCouncilViewResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        arrayList = ArrayList()
        binding.studentClgCouncilResultRecyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        binding.studentClgCouncilResultRecyclerView.layoutManager = linearLayoutManager
        cardsAdap = cardsAdapter(modelnames.clgcouncilstudviewresult, 5)
        cardsAdap.set(arrayList, applicationContext)
        binding.studentClgCouncilResultRecyclerView.adapter = cardsAdap

        fBase.child("Votify").child("Users").child(currentUser)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val clg_uid = snapshot.child("collegeuid").value.toString()
                    val courseName = snapshot.child("coursename").value.toString()
                    fBase.child("Votify").child("Institution").child(clg_uid).child("CollegeCouncil")
                        .child("PositionData").orderByChild("course_name").equalTo(courseName)
                        .addValueEventListener(object: ValueEventListener {
                            override fun onDataChange(snap: DataSnapshot) {
                                arrayList.clear()
                                if(snap.exists()){
                                    for (childSnapshot: DataSnapshot in snap.children) {
                                        val winnerData = ClgCouncilWinner(
                                            childSnapshot.child("position").value.toString(),
                                            childSnapshot.child("course_name").value.toString()
                                        )
                                        winnerData.posDataUID=childSnapshot.key.toString()
                                        arrayList.add(winnerData)
                                    }
                                    cardsAdap.update(arrayList)
                                }
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