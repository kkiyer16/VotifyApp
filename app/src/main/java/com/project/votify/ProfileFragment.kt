package com.project.votify

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.mikhaellopez.circularimageview.CircularImageView
import com.project.votify.databinding.FragmentProfileBinding
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import id.zelory.compressor.Compressor
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val fBase = FirebaseDatabase.getInstance().reference
    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
    private var selectedPhotoUri : Uri? = null
    private var coms : Bitmap? = null
    lateinit var byteArrayOutputStream : ByteArrayOutputStream
    lateinit var imgPath : UploadTask
    lateinit var imgData : ByteArray
    lateinit var fStorage : FirebaseStorage
    private var storageReference : StorageReference? = null
    lateinit var fAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root

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

        fStorage = FirebaseStorage.getInstance()
        storageReference = fStorage.reference
        fAuth = FirebaseAuth.getInstance()

        binding.profilePicOfStudent.setOnClickListener {
            val permission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (permission != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(context, "Permission Granted!", Toast.LENGTH_SHORT).show()
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
                }
                else {
                    showFileChooser()
                }
            }
            else{
                showFileChooser()
            }
        }

        fBase.child("Votify").child("Users").child(currentUser).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(ds: DataSnapshot) {
                if(ds.exists()){
                    binding.nameOfStudent.text = ds.child("name").value.toString().trim()
                    Glide.with(this@ProfileFragment).load(ds.child("profileimageurl").value.toString())
                        .placeholder(R.drawable.unisex_avatar).dontAnimate().fitCenter().into(binding.profilePicOfStudent)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message.toString().trim(), Toast.LENGTH_SHORT).show()
            }
        })

        binding.editProfileStudent.setOnClickListener {
            startActivity(Intent(requireActivity().applicationContext, EditProfileTeacherActivity::class.java))
            requireActivity().finish()
        }

        binding.personalInformationStudent.initListener {
            Log.d("expand_listener", "isExpand: $it")
            val pro_teacher_name_tv = binding.personalInformationStudent.childView.findViewById<TextView>(R.id.pro_teacher_name_tv)
            val pro_teacher_email_id_tv = binding.personalInformationStudent.childView.findViewById<TextView>(R.id.pro_teacher_email_id_tv)
            val pro_teacher_mob_no_tv = binding.personalInformationStudent.childView.findViewById<TextView>(R.id.pro_teacher_mob_no_tv)
            fBase.child("Votify").child("Users").child(currentUser).addValueEventListener(object : ValueEventListener{
                override fun onDataChange(ds: DataSnapshot) {
                    if(ds.exists()){
                        pro_teacher_name_tv.text = ds.child("name").value.toString().trim()
                        pro_teacher_email_id_tv.text = ds.child("emailid").value.toString().trim()
                        pro_teacher_mob_no_tv.text = ds.child("mobilenumber").value.toString().trim()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, error.message.toString().trim(), Toast.LENGTH_SHORT).show()
                }
            })
        }

        binding.collegeInformationStudent.initListener {
            Log.d("expand_listener", "isExpand: $it")
            val clg_name = binding.collegeInformationStudent.childView.findViewById<TextView>(R.id.pro_teacher_clg_name_tv)
            val course_name = binding.collegeInformationStudent.childView.findViewById<TextView>(R.id.pro_teacher_course_name_tv)
            val course_year = binding.collegeInformationStudent.childView.findViewById<TextView>(R.id.pro_teacher_course_yr_tv)
            val  div =  binding.collegeInformationStudent.childView.findViewById<TextView>(R.id.pro_teacher_teaching_div_tv)
            val  teacher_id = binding.collegeInformationStudent.childView.findViewById<TextView>(R.id.pro_teacher_teacher_id_tv)
            fBase.child("Votify").child("Users").child(currentUser).addValueEventListener(object : ValueEventListener{
                override fun onDataChange(ds: DataSnapshot) {
                    if(ds.exists()){
                        clg_name.text = ds.child("collegename").value.toString().trim()
                        course_name.text = ds.child("coursename").value.toString().trim()
                        course_year.text = ds.child("courseyear").value.toString().trim()
                        div.text = ds.child("section").value.toString().trim()
                        teacher_id.text = ds.child("collegeid").value.toString().trim()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, error.message.trim(), Toast.LENGTH_SHORT).show()
                }

            })
        }

        binding.changePasswordStudent.initListener {
            Log.d("expand_listener", "isExpand: $it")
            val change_pwd_btn = binding.changePasswordStudent.childView.findViewById<MaterialButton>(R.id.change_password_btn)
            change_pwd_btn.setOnClickListener {
                val old_password = binding.changePasswordStudent.childView.findViewById<TextInputEditText>(R.id.old_password_cp)
                val new_password = binding.changePasswordStudent.childView.findViewById<TextInputEditText>(R.id.new_password_cp)
                val old_pwd = old_password.text.toString().trim()
                val new_pwd = new_password.text.toString().trim()
                if(old_pwd.isEmpty() || new_pwd.isEmpty()){
                    Toast.makeText(context, "Enter required credentials", Toast.LENGTH_SHORT).show()
                }
                else if (TextUtils.isEmpty(old_pwd)){
                    old_password.error = "Old Password Required"
                    old_password.requestFocus()
                }
                else if (TextUtils.isEmpty(new_pwd)){
                    new_password.error = "Old Password Required"
                }
                else{
                    val user = fAuth.currentUser!!
                    val credential = EmailAuthProvider.getCredential(user.email!!, old_pwd)
                    user.reauthenticate(credential).addOnCompleteListener {task->
                        if (task.isSuccessful){
                            user.updatePassword(new_pwd).addOnCompleteListener { tk->
                                if (tk.isSuccessful){
                                    Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show()
                                    old_password.setText("")
                                    new_password.setText("")
                                    old_password.requestFocus()
                                }
                                else{
                                    Toast.makeText(context, "Password Not changed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            }
        }

        binding.changeEmailIdStudent.initListener {
            Log.d("expand_listener", "isExpand: $it")
            val change_email_id_btn = binding.changeEmailIdStudent.childView.findViewById<MaterialButton>(R.id.change_emailid_btn)
            change_email_id_btn.setOnClickListener {
                val old_emailid = binding.changeEmailIdStudent.childView.findViewById<TextInputEditText>(R.id.old_emailid_ce)
                val new_emailid = binding.changeEmailIdStudent.childView.findViewById<TextInputEditText>(R.id.new_emailid_ce)
                val old_emid = old_emailid.text.toString().trim()
                val new_emid = new_emailid.text.toString().trim()
                if(old_emid.isEmpty() || new_emid.isEmpty()){
                    Toast.makeText(context, "Enter required credentials", Toast.LENGTH_SHORT).show()
                }
                else if (TextUtils.isEmpty(old_emid)){
                    old_emailid.error = "Old Email ID Required"
                    old_emailid.requestFocus()
                }
                else if(!Patterns.EMAIL_ADDRESS.matcher(old_emid).matches()){
                    old_emailid.error = "Invalid Old Email ID"
                    old_emailid.requestFocus()
                }
                else if (TextUtils.isEmpty(new_emid)){
                    new_emailid.error = "Old Email ID Required"
                }
                else if(!Patterns.EMAIL_ADDRESS.matcher(new_emid).matches()){
                    new_emailid.error = "Invalid New Email ID"
                    new_emailid.requestFocus()
                }
                else{
                    val user = fAuth.currentUser!!
                    user.updateEmail(new_emid).addOnCompleteListener { task->
                        if(task.isSuccessful){
                            Toast.makeText(context, "Email ID changed successfully", Toast.LENGTH_SHORT).show()
                            old_emailid.setText("")
                            new_emailid.setText("")
                            old_emailid.requestFocus()
                            try{
                                val newemailid = HashMap<String, Any>()
                                newemailid["emailid"] = new_emid
                                fBase.child("Votify").child("Users").child(currentUser).updateChildren(newemailid)
                                    .addOnCompleteListener {
                                        Log.d("email", "Updated Email ID added into DB")
                                    }
                                    .addOnFailureListener{
                                        Log.d("email", "Failed to added Email ID into DB")
                                    }
                            }catch (e: Exception){
                                Log.d("error", e.toString().trim())
                            }
                        }
                        else{
                            Toast.makeText(context, "Email ID Not changed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        return view
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
            imgPath = storageReference!!.child("ProfileImages").child(FirebaseAuth.getInstance().currentUser!!.uid).putBytes(imgData)
            imgPath.addOnCompleteListener {
                if (it.isSuccessful){
                    storeData(it as UploadTask)
                }else{
                    Toast.makeText(requireView().context, "Something went wrong", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun storeData(uploadTask: UploadTask) {
        uploadTask.result.storage.downloadUrl.addOnSuccessListener {uri->
            val teacherData = HashMap<String, Any>()
            teacherData["profileimageurl"] = uri.toString()

            val ref = fBase.child("Votify").child("Users").child(currentUser)
            ref.updateChildren(teacherData)
                .addOnCompleteListener {
                    Toast.makeText(requireActivity().applicationContext, "Profile Pic Added Successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireActivity().applicationContext, "Failed to add details", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            val result: CropImage.ActivityResult = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK){
                selectedPhotoUri = result.uri
                try {
                    val b = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, selectedPhotoUri)
                    Glide.with(this).load(b).into(binding.profilePicOfStudent)
                    uploadFile()
                }catch (e: IOException){
                    e.printStackTrace()
                }
            }
            else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                val e = result.error
                Log.d("er", e.toString())
            }
        }
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}