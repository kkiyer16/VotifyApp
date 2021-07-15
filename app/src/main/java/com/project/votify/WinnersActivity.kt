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
import com.project.votify.databinding.ActivityWinnersBinding
import com.project.votify.models.User
import com.project.votify.models.modelnames

class WinnersActivity : AppCompatActivity() {

    lateinit var binding: ActivityWinnersBinding
    lateinit var positionDataUid: String
    private val fBase = FirebaseDatabase.getInstance().reference
    lateinit var cardsAdap: cardsAdapter
    lateinit var arrayList: ArrayList<Any>
    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWinnersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.hasExtra("positionDataUid")) {
            positionDataUid = intent.extras?.getString("positionDataUid").toString()
        }


        arrayList = ArrayList()
        binding.winnerRecyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        binding.winnerRecyclerView.layoutManager = linearLayoutManager
        cardsAdap = cardsAdapter(modelnames.winners, 4)
        cardsAdap.set(arrayList, applicationContext)
        binding.winnerRecyclerView.adapter = cardsAdap

        fBase.child("Votify").child("Users").child(currentUser)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val clg_uid = snapshot.child("collegeuid").value.toString()
                    fBase.child("Votify").child("Institution").child(clg_uid).child("PositionData")
                        .child(positionDataUid).child("representatives")
                        .addValueEventListener(object : ValueEventListener {
                            val arrList = ArrayList<String>()
                            override fun onDataChange(snap: DataSnapshot) {
                                for (ds in snap.children) {
                                    arrList.add(ds.key.toString())
                                }
                                arrList.forEach{
                                    fBase.child("Votify").child("Users").child(it)
                                        .addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(winnerSnapshot: DataSnapshot) {
                                                if (winnerSnapshot.exists()) {
                                                        val userData = User(
                                                            name = winnerSnapshot.child("name").value.toString(),
                                                            profileimageurl = winnerSnapshot.child("profileimageurl").value.toString()
                                                        )
                                                        arrayList.add(userData)
                                                        cardsAdap.update(arrayList)
                                                    }
                                                }
                                            override fun onCancelled(error: DatabaseError) {
                                            }
                                        })
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