package com.project.votify

import android.app.AlertDialog
import android.graphics.Canvas
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.project.votify.adapter.PostsAdapter
import com.project.votify.databinding.FragmentTeacherViewPostBinding
import com.project.votify.models.ModelEvents
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlin.Exception

class TeacherViewPostFragment : Fragment() {

    private var _binding: FragmentTeacherViewPostBinding? = null
    private val binding get() = _binding!!
    lateinit var postsAdapter: PostsAdapter
    private val mArrayList : ArrayList<ModelEvents> = ArrayList()
    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
    private val fBase = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentTeacherViewPostBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.fragViewPostRecyclerView.setHasFixedSize(true)
        binding.fragViewPostRecyclerView.layoutManager = LinearLayoutManager(context)
        postsAdapter = PostsAdapter(requireContext().applicationContext, mArrayList)
        binding.fragViewPostRecyclerView.adapter = postsAdapter

        fBase.child("Votify").child("Users").child(currentUser).addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val clg_uid = snapshot.child("collegeuid").value.toString()
                var user_uid: ArrayList<Any> = ArrayList()
                val userName = snapshot.child("name").value.toString()
                fBase.child("Votify").child("Institution").child(clg_uid).child("Post")
                    .addValueEventListener(object : ValueEventListener{
                        override fun onDataChange(snap: DataSnapshot) {
                            mArrayList.clear()
                            for(ds: DataSnapshot in snap.children){
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
                            postsAdapter.update(mArrayList)
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(binding.fragViewPostRecyclerView)

        return view
    }

    val simpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val pos = viewHolder.adapterPosition
            when (direction) {
                ItemTouchHelper.LEFT -> {
                    deletePosts(pos)
                }
            }
        }

        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
                                 actionState: Int, isCurrentlyActive: Boolean) {
            RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                .addSwipeLeftBackgroundColor(ContextCompat.getColor(context!!, R.color.delete_red))
                .addSwipeLeftActionIcon(R.drawable.ic_delete)
                .create()
                .decorate()

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    private fun deletePosts(pos: Int) {
        val data = mArrayList[pos]
        AlertDialog.Builder(context)
            .setTitle("Order Delivered?")
            .setMessage("Are you sure?")
            .setPositiveButton("Yes") { dialog, _ ->
                postsAdapter.notifyItemRemoved(pos)
                mArrayList.removeAt(pos)
                postsAdapter.notifyItemChanged(pos, mArrayList.size)
                postsAdapter.update(mArrayList)
                removePost(data, pos)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
            }
            .create().show()
    }

    private fun removePost(data: ModelEvents, pos: Int) {
        val eventTitle = data.eventTitle
        val userUid = data.created_by
        val clgUid = data.clgUid
        try{
            fBase.child("Votify").child("Institution").child(clgUid).child("Post")
                .orderByChild("eventtitle").equalTo(eventTitle).addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            for(ds : DataSnapshot in snapshot.children){
                                ds.ref.removeValue()
                                    .addOnSuccessListener {
                                        FirebaseStorage.getInstance().getReferenceFromUrl(data.eventImage).delete()
                                        Toast.makeText(context!!, "Removed Successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener{
                                        Toast.makeText(context!!, "Failed to Remove", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
        }
        catch(e: Exception){
            e.printStackTrace()
        }
    }

}