package com.project.votify

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.project.votify.databinding.FragmentProfileBinding
import com.project.votify.databinding.FragmentTeacherAddPostBinding
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import id.zelory.compressor.Compressor
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*

class TeacherAddPostFragment : Fragment() {

    private var _binding: FragmentTeacherAddPostBinding? = null
    private val binding get() = _binding!!
    private val fBase = FirebaseDatabase.getInstance().reference
    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
    private var selectedPhotoUri: Uri? = null
    private var coms: Bitmap? = null
    lateinit var byteArrayOutputStream: ByteArrayOutputStream
    lateinit var imgPath: UploadTask
    lateinit var imgData: ByteArray
    lateinit var fStorage: FirebaseStorage
    private var storageReference: StorageReference? = null
    lateinit var fAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentTeacherAddPostBinding.inflate(inflater, container, false)
        val view = binding.root

        fStorage = FirebaseStorage.getInstance()
        storageReference = fStorage.reference
        fAuth = FirebaseAuth.getInstance()

        binding.fragAddPostPhoto.setOnClickListener {
            val permission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context, "Permission Granted!", Toast.LENGTH_SHORT).show()
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
                }
                else {
                    showFileChooser()
                }
            }
            else {
                showFileChooser()
            }
        }

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        binding.fragAddPostSelectDateTextview.setOnClickListener {
            DatePickerDialog(requireContext(), { _, i, i2, i3 ->
                    val m = i2 + 1
                    var st = ""
                    if(m == 1)
                        st = "JAN"
                        binding.fragAddPostSelectDateDisplay.text = "$st $i3 $i"
                    if(m == 2)
                        st = "FEB"
                        binding.fragAddPostSelectDateDisplay.text = "$st $i3 $i"
                    if(m == 3)
                        st = "MAR"
                        binding.fragAddPostSelectDateDisplay.text = "$st $i3 $i"
                    if(m == 4)
                        st = "APR"
                        binding.fragAddPostSelectDateDisplay.text = "$st $i3 $i"
                    if(m == 5)
                        st = "MAY"
                        binding.fragAddPostSelectDateDisplay.text = "$st $i3 $i"
                    if(m == 6)
                        st = "JUN"
                        binding.fragAddPostSelectDateDisplay.text = "$st $i3 $i"
                    if(m == 7)
                        st = "JUL"
                        binding.fragAddPostSelectDateDisplay.text = "$st $i3 $i"
                    if(m == 8)
                        st = "AUG"
                        binding.fragAddPostSelectDateDisplay.text = "$st $i3 $i"
                    if(m == 9)
                        st = "SEP"
                        binding.fragAddPostSelectDateDisplay.text = "$st $i3 $i"
                    if(m == 10)
                        st = "OCT"
                        binding.fragAddPostSelectDateDisplay.text = "$st $i3 $i"
                    if(m == 11)
                        st = "NOV"
                        binding.fragAddPostSelectDateDisplay.text = "$st $i3 $i"
                    if(m == 12)
                        st = "DEC"
                        binding.fragAddPostSelectDateDisplay.text = "$st $i3 $i"
                }, year, month, day).show()
        }

        binding.fragAddPostButton.setOnClickListener {
            addEventDetailsPost()
        }

        return view
    }

    private fun addEventDetailsPost() {
        uploadFile()
    }

    private fun showFileChooser() {
        CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1).start(requireContext(), this)
    }

    private fun uploadFile(){
        if (selectedPhotoUri != null){
            val newFile = File(selectedPhotoUri!!.path)
            try {
                coms = Compressor(context).setMaxWidth(125)
                    .setMaxHeight(125)
                    .setQuality(50)
                    .compressToBitmap(newFile)
            }catch(e : IOException){
                e.printStackTrace()
            }
            byteArrayOutputStream = ByteArrayOutputStream()
            coms?.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            imgData = byteArrayOutputStream.toByteArray()
            imgPath = storageReference!!.child("EventImages").child(currentUser)
                .child(binding.fragAddPostTitleOfEvent.text.toString().trim()).putBytes(imgData)
            imgPath.addOnCompleteListener {
                if (it.isSuccessful){
                    storeData(it as UploadTask)
                }else{
                    Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun storeData(uploadTask: UploadTask) {
        val event_title = binding.fragAddPostTitleOfEvent.text.toString().trim()
        val event_desc = binding.fragAddPostDetails.text.toString().trim()
        if(event_title.isEmpty() || event_desc.isEmpty()){
            Toast.makeText(requireContext(), "Enter Required Details", Toast.LENGTH_SHORT).show()
        }
        else if(TextUtils.isEmpty(event_title)){
            binding.fragAddPostTitleOfEvent.error = "Enter event title"
        }
        else if(TextUtils.isEmpty(event_desc)){
            binding.fragAddPostDetails.error = "Enter event Details"
        }
        else {
            uploadTask.result.storage.downloadUrl.addOnSuccessListener { uri ->
                val dbRef = fBase.child("Votify").child("Users").child(currentUser)
                dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val clg_uid = snapshot.child("collegeuid").value.toString()
                        val eventData = HashMap<String, Any>()
                        eventData["eventtitle"] = event_title
                        eventData["eventdetails"] = event_desc
                        eventData["eventdate"] = binding.fragAddPostSelectDateDisplay.text.toString().trim()
                        eventData["eventimageurl"] = uri.toString()
                        eventData["created_by"] = currentUser
                        eventData["createdbyname"] = snapshot.child("name").value.toString()
                        Log.d("clg_uid:", clg_uid)
                        fBase.child("Votify").child("Institution").child(clg_uid).child("Post")
                            .child(UUID.randomUUID().toString().trim())
                            .setValue(eventData)
                            .addOnCompleteListener {
                                Toast.makeText(requireContext(), "Event Details Added Successfully", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(requireContext(), TeacherHomeActivity::class.java))
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Failed to add Event Pic", Toast.LENGTH_SHORT).show()
                            }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val event_pic = binding.fragAddPostPhoto
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            val result : CropImage.ActivityResult = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK){
                selectedPhotoUri = result.uri
                try {
                    val b: Bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, selectedPhotoUri)
                    Glide.with(this).load(b).into(event_pic)
                }catch (e : IOException){
                    e.printStackTrace()
                }
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                val ex : Exception = result.error
                Log.d("Main", ex.toString())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}