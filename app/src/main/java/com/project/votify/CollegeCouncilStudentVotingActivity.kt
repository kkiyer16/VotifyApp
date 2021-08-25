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
import com.project.votify.databinding.ActivityCollegeCouncilStudentVotingBinding
import com.project.votify.models.CastVote
import com.project.votify.models.modelnames
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class CollegeCouncilStudentVotingActivity : AppCompatActivity() {

    lateinit var binding : ActivityCollegeCouncilStudentVotingBinding
    private val fBase = FirebaseDatabase.getInstance().reference
    lateinit var cardsAdap: cardsAdapter
    lateinit var arrayList: ArrayList<Any>
    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCollegeCouncilStudentVotingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val candidate_uid = intent.extras?.getString("clgc_candidate_uid_ref")
        val cand_pos = intent.extras?.getString("clgc_position")
        val cand_course_name = intent.extras?.getString("clgc_courseName")
        val pollUid=intent.extras!!.getString("clgc_pollUid").toString()
        val collegeUid=intent.extras!!.getString("clgc_collegeUid").toString()

        arrayList = ArrayList()
        binding.clgcCastVotesRecyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        binding.clgcCastVotesRecyclerView.layoutManager = linearLayoutManager
        cardsAdap = cardsAdapter(modelnames.clgcouncilcastvotes, 2)
        cardsAdap.set(arrayList, applicationContext)
        cardsAdap.setPollDetails(pollUid,collegeUid)
        binding.clgcCastVotesRecyclerView.adapter = cardsAdap
        loadParticipantData(collegeUid,candidate_uid,cand_course_name,cand_pos,pollUid)


    }
    fun loadParticipantData(
        collegeUid: String,
        candidate_uid: String?,
        cand_course_name: String?,
        cand_pos: String?,
        pollUid: String
    ) {
        fBase.child("Votify").child("Institution").child(collegeUid).child("CollegeCouncil")
            .child("CandidateData").child(candidate_uid!!).child("participants")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    arrayList.clear()
                    for (ds in snap.children) {
                        val userData = CastVote(
                            ds.child("profile_url").value.toString(),
                            ds.child("name").value.toString(),
                            cand_course_name.toString(),
                            ds.child("course_year").value.toString(),
                            ds.child("section").value.toString(),
                            cand_pos.toString(),
                            candidate_uid.toString(),
                            ds.child("uid").value.toString(),
                        )
                        arrayList.add(userData)
                    }
                    cardsAdap.update(arrayList)
                    checkVotingStatus(collegeUid,pollUid)
                }

                override fun onCancelled(err: DatabaseError) {
                }

            })
    }

    private fun checkVotingStatus(collegeUid: String, pollUid: String) {
        val reference=FirebaseDatabase.getInstance().reference
        reference.child("Votify").child("Institution").child(collegeUid).child("CollegeCouncil")
            .child("Polls").child(pollUid).child("Votes").child(FirebaseAuth.getInstance().currentUser!!.uid.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        arrayList.forEach{
                            GlobalScope.async{
                                arrayList.forEach{ castVote->
                                    (castVote as CastVote).isSelected=true
                                }
                            }
                        }
                        cardsAdap.update(arrayList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }
}