package com.project.votify

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.project.votify.adapter.EventAdapter
import com.project.votify.models.ModelEvents
import java.lang.Exception

class HomeFragment : Fragment() {

    private val fBase = FirebaseDatabase.getInstance().reference
    lateinit var recyclerView: RecyclerView
    lateinit var eventAdapter: EventAdapter
    private val mArrayList : ArrayList<ModelEvents> = ArrayList()
    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_home, container, false)

        //for transparent status bar
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true)
        }
        if (Build.VERSION.SDK_INT >= 19) {
            requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            requireActivity().window.statusBarColor = Color.TRANSPARENT
        }

        val toolbar = v.findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = ""
        (context as StudentHomeActivity).setSupportActionBar(toolbar)
        (context as StudentHomeActivity).setDrawer(toolbar)
        setHasOptionsMenu(true)

        val dbRef = fBase.child("Votify").child("Users").child(currentUser)
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val clg_uid = snapshot.child("collegeuid").value.toString()
                val userName = snapshot.child("name").value.toString()
                fBase.child("Votify").child("Institution").child(clg_uid).child("Post")
                    .addValueEventListener(object : ValueEventListener{
                        override fun onCancelled(error: DatabaseError) {
                        }

                        override fun onDataChange(snap: DataSnapshot) {
                            mArrayList.clear()
                            for(ds in snap.children){
                                try{
                                    val typesModelEvents = ModelEvents(
                                        ds.child("eventimageurl").value.toString(),
                                        ds.child("eventtitle").value.toString(),
                                        ds.child("eventdetails").value.toString(),
                                        ds.child("eventdate").value.toString(),
                                        ds.child("created_by").value.toString(),
                                        ds.child("createdbyname").value.toString(),
                                        clg_uid
                                    )
                                    mArrayList.add(typesModelEvents)
                                }catch (e: Exception){
                                    e.printStackTrace()
                                }
                            }
                            eventAdapter.update(mArrayList)
                        }
                    })
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

        recyclerView = v.findViewById(R.id.hf_recycler_view)
        recyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager
        eventAdapter = EventAdapter(requireActivity().applicationContext, mArrayList)
        recyclerView.adapter = eventAdapter

        return v
    }

    private fun setWindowFlag(bits: Int, on: Boolean) {
        val win = requireActivity().window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

    override fun onStart() {
        super.onStart()
        if(FirebaseAuth.getInstance().currentUser == null){
            startActivity(Intent(activity, SignInActivity::class.java))
            requireActivity().finish()
        }
    }
}