package com.project.votify

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.project.votify.adapter.cardsAdapter
import com.project.votify.models.modelnames
import com.project.votify.databinding.FragmentPollsBinding
import com.project.votify.models.PositionData

class PollsFragment : Fragment() {

    private var _binding: FragmentPollsBinding? = null
    private val binding get() = _binding!!
    private val fBase = FirebaseDatabase.getInstance().reference
    lateinit var cardsAdap: cardsAdapter
    lateinit var arrayList : ArrayList<Any>
    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPollsBinding.inflate(inflater, container, false)
        val view = binding.root

        arrayList = ArrayList()
        binding.fragPollsRecyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(context)
        binding.fragPollsRecyclerView.layoutManager = linearLayoutManager
        cardsAdap = cardsAdapter(modelnames.fragpollpos, 1)
        cardsAdap.set(arrayList, requireContext())
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

        return view
    }
}