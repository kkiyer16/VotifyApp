package com.project.votify

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.project.votify.adapter.cardsAdapter
import com.project.votify.databinding.FragmentPollsBinding

class PollsFragment : Fragment() {

    private var _binding: FragmentPollsBinding? = null
    private val binding get() = _binding!!
    private val reference = FirebaseDatabase.getInstance().reference
    lateinit var cardsAdap: cardsAdapter
    lateinit var arrayList : ArrayList<Any>
    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid.toString()
    private var teacherCollegeUid: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPollsBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.classCouncil.setOnClickListener {
            val intent = Intent(activity, ClassCouncilPollsActicity::class.java)
            (activity)!!.startActivity(intent)
        }

       reference.child("Votify").child("Users").child(currentUser).addValueEventListener(object: ValueEventListener{
           override fun onDataChange(snapshot: DataSnapshot) {
               val classCouncilMember = snapshot.child("isClassCouncilMember").value.toString()
               if(classCouncilMember == "1"){
                   binding.collegeCouncil.isActivated = true
                   binding.collegeCouncil.setOnClickListener {
                       val intent = Intent(activity, ClgCouncilPollsActicity::class.java)
                       (activity)!!.startActivity(intent)
                   }
               }else{
                   binding.collegeCouncil.isActivated = false
                   binding.collegeCouncil.setOnClickListener {
                       if(!binding.collegeCouncil.isActivated){
                           Toast.makeText(context, "Sorry! you are not allowed to this voting", Toast.LENGTH_SHORT).show()
                       }
                   }
               }
           }

           override fun onCancelled(error: DatabaseError) {
           }

       })

        return view
    }
}