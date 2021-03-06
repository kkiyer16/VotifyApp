package com.project.votify

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.project.votify.adapter.RemoveCandClgCouncilAdapter
import com.project.votify.adapter.RemoveCandidatePositionAdapter
import com.project.votify.databinding.ActivityCollegeCouncilRemoveCandidateBinding
import com.project.votify.models.Position

class CollegeCouncilRemoveCandidateActivity : AppCompatActivity() {

    lateinit var binding: ActivityCollegeCouncilRemoveCandidateBinding
    lateinit var positionList: ArrayList<Position>
    lateinit var adapter: RemoveCandClgCouncilAdapter
    lateinit var reference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCollegeCouncilRemoveCandidateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        positionList = ArrayList()
        adapter = RemoveCandClgCouncilAdapter(positionList, applicationContext)
        binding.removePositionRecycler.layoutManager = LinearLayoutManager(applicationContext)
        binding.removePositionRecycler.adapter = adapter
        reference = FirebaseDatabase.getInstance().reference
        reference.child("Votify").child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && snapshot.hasChildren()) {
                        val collegeUid = snapshot.child("collegeuid").value.toString()
                        loadPositionData(collegeUid)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        binding.searchPosition.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (TextUtils.isEmpty(query)) {
                    adapter.update(positionList)
                } else {
                    adapter.performFiltering(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (TextUtils.isEmpty(newText)) {
                    adapter.update(positionList)
                } else {
                    adapter.performFiltering(newText)
                }
                return true
            }
        })
    }

    private fun loadPositionData(collegeUid: String) {
        reference.child("Votify").child("Institution").child(collegeUid).child("CollegeCouncil")
            .child("CandidateData").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && snapshot.hasChildren()) {
                        positionList.clear()
                        for (childSnapshot: DataSnapshot in snapshot.children) {
                            val position = Position(
                                childSnapshot.child("position").value.toString(),
                                childSnapshot.child("coursename").value.toString(),
                                childSnapshot.child("courseyear").value.toString(),
                                childSnapshot.child("section").value.toString(),
                                childSnapshot.child("created_on").value.toString(),
                                collegeUid,
                                childSnapshot.key.toString(),
                            )
                            positionList.add(position)
                        }
                        adapter.update(positionList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

    }
}