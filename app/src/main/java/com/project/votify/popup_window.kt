package com.project.votify

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*
import kotlin.collections.ArrayList


class popup_window : DialogFragment() {

    lateinit var fBase : DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val retView = inflater.inflate(R.layout.popup_window, container, false)
        fBase = FirebaseDatabase.getInstance().reference

        return retView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val add_btn = requireView().findViewById<MaterialButton>(R.id.add_college_name_btn)
        add_btn.setOnClickListener {
            addCollegeNametoDB()
        }
    }

    private fun addCollegeNametoDB() {
        val et_add_clg = requireView().findViewById<TextInputEditText>(R.id.add_college_name_edittext)
        val add_clg = et_add_clg.text.toString().trim()
        if (add_clg.isNotEmpty()) {
            var a = AlertDialog.Builder(requireContext())
                .setTitle("Add College")
                .setMessage(" Are you sure you want to add this College?")
                .setPositiveButton(
                    "Yes"
                ) { dialog, which ->
                    //val addClgHashMap = hashMapOf("CollegeName" to add_clg)
                    fBase.child("Votify").child("CollegeName")
                        .push()
                        .setValue(add_clg)
                        .addOnSuccessListener {
                            Toast.makeText(context, "College Added", Toast.LENGTH_SHORT).show()
                        }
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, which ->
                    dialog.cancel()
                }
                .create()
                .show()
        } else {
            Toast.makeText(context, "Please enter valid details ", Toast.LENGTH_SHORT).show()
        }
    }
}