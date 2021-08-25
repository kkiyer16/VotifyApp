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
import com.project.votify.databinding.ActivityCollegeCouncilCreatePollsBinding
import com.project.votify.models.CCPositionData
import com.project.votify.models.modelnames

class CollegeCouncilCreatePollsActivity : AppCompatActivity() {

    lateinit var binding: ActivityCollegeCouncilCreatePollsBinding
    private val fBase = FirebaseDatabase.getInstance().reference
    lateinit var cardsAdap: cardsAdapter
    lateinit var arrayList: ArrayList<Any>
    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCollegeCouncilCreatePollsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        arrayList = ArrayList()
        binding.createPollsRecyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        binding.createPollsRecyclerView.layoutManager = linearLayoutManager
        cardsAdap = cardsAdapter(modelnames.ClgCouncilPollPos, 5)
        cardsAdap.set(arrayList, applicationContext)
        binding.createPollsRecyclerView.adapter = cardsAdap

        val dbRef = fBase.child("Votify").child("Users").child(currentUser)
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val clg_uid = snapshot.child("collegeuid").value.toString()
                val courseName = snapshot.child("coursename").value.toString()
                println(clg_uid)
                println(courseName)
                fBase.child("Votify").child("Institution").child(clg_uid).child("CollegeCouncil")
                    .child("CandidateData").orderByChild("council").equalTo("college")
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            arrayList.clear()
                            for (childSnapshot: DataSnapshot in snapshot.children) {
                                val positionData = CCPositionData(
                                    childSnapshot.child("position").value.toString(),
                                    childSnapshot.child("coursename").value.toString(),
                                    childSnapshot.key.toString()
                                )
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