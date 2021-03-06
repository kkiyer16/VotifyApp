package com.project.votify

import android.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.project.votify.adapter.CollegeCouncilStudentAdapter
import com.project.votify.adapter.SelectedCandidateAdapter
import com.project.votify.databinding.ActivityCollegeCouncilAddCandidateBinding
import com.project.votify.databinding.AddCandidateDialogBinding
import com.project.votify.models.ChangeValue
import com.project.votify.models.User
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CollegeCouncilAddCandidateActivity : AppCompatActivity() {

    lateinit var binding: ActivityCollegeCouncilAddCandidateBinding
    lateinit var studentList: ArrayList<User>
    lateinit var adapter: CollegeCouncilStudentAdapter
    lateinit var selectedCandidateAdapter: SelectedCandidateAdapter
    lateinit var reference: DatabaseReference
    var addItem: MenuItem? = null
    private var bottomSheetBehavior: BottomSheetBehavior<RelativeLayout>? = null
    private var bottomSheetBinder: AddCandidateDialogBinding? = null
    private var teacherCollegeUid: String = ""
    lateinit var sectionList: ArrayList<String>
    lateinit var classList: ArrayList<String>
    lateinit var positionList: ArrayList<String>
    lateinit var list: ArrayList<String>
    lateinit var winnersArrList : ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCollegeCouncilAddCandidateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        winnersArrList = ArrayList()
        list = ArrayList()
        setSupportActionBar(binding.toolbar)
        bottomSheetBinder = binding.bottomDialog
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetBinder!!.root)
        studentList = ArrayList()
        sectionList = ArrayList()
        classList = ArrayList()
        positionList = ArrayList()
        selectedCandidateAdapter = SelectedCandidateAdapter(studentList, applicationContext)
        adapter = CollegeCouncilStudentAdapter(studentList, applicationContext, object : ChangeValue {
            override fun onChange(isvisible: Boolean) {
                if (addItem != null) {
                    addItem!!.isVisible = isvisible
                    if (!isvisible) {
                        bottomSheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                }
            }

        })
//        binding.sectionFilter.setAdapter(ArrayAdapter(this, R.layout.simple_spinner_dropdown_item, sectionList))
        binding.sectionFilter.setAdapter(ArrayAdapter(this, R.layout.simple_spinner_dropdown_item, positionList))
        binding.classFilter.setAdapter(ArrayAdapter(this, R.layout.simple_spinner_dropdown_item, classList))
        binding.candidateRecycler.layoutManager = LinearLayoutManager(applicationContext)
        binding.candidateRecycler.adapter = adapter
        reference = FirebaseDatabase.getInstance().reference

        reference.child("Votify").child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && snapshot.hasChildren()) {
                        val collegeUid = snapshot.child("collegeuid").value.toString()
                        val course = snapshot.child("coursename").value.toString()
                        teacherCollegeUid = collegeUid
                        reference.child("Votify").child("Institution").child(collegeUid).child("PositionData")
                            .addValueEventListener(object: ValueEventListener{
                                override fun onDataChange(snap: DataSnapshot) {
                                    for(dataSnapshot in snap.children){
                                        println("PositionDataUid: ${dataSnapshot.key.toString()}")
                                        val positionDataUid = dataSnapshot.key.toString()
                                        reference.child("Votify").child("Institution").child(collegeUid)
                                            .child("PositionData").child(positionDataUid).addValueEventListener(object: ValueEventListener{
                                                override fun onDataChange(snapShot: DataSnapshot) {
                                                    if(snapShot.exists()){
                                                        val positionName = snapShot.child("position").value.toString()
                                                        reference.child("Votify").child("Institution").child(collegeUid)
                                                            .child("PositionData").child(positionDataUid).child("representatives")
                                                            .addValueEventListener(object : ValueEventListener{
                                                                override fun onDataChange(sn: DataSnapshot) {
                                                                    sn.children.forEach { winUid->
                                                                        println("WinnersUID: ${winUid.key.toString()}")
                                                                        loadStudentData(winUid.key.toString(), course, positionName)
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
                                }
                                override fun onCancelled(err: DatabaseError) {
                                }
                            })
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })

        binding.searchStudent.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (TextUtils.isEmpty(query)) {
                    adapter.update(studentList)
                } else {
                    adapter.performFiltering(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (TextUtils.isEmpty(newText)) {
                    adapter.update(studentList)
                } else {
                    adapter.performFiltering(newText)
                }
                return true
            }
        })
        binding.sectionFilter.setOnItemClickListener { _, _, position, _ ->
//            adapter.filterBySection(sectionList[position])
            adapter.filterByPosition(positionList[position])
        }
        binding.classFilter.setOnItemClickListener { _, _, position, _ ->
            adapter.filterByClass(classList[position])
        }
    }


    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.clear();
        menu?.add(0, 101, Menu.NONE, "Add")
            ?.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)?.isVisible = false
        addItem = menu!!.getItem(0)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == 101) {
            Toast.makeText(applicationContext, "Cliked on add", Toast.LENGTH_SHORT).show()
            val list = adapter.getCurrentStudentList()
            if (list.isNotEmpty()) {
                if (bottomSheetBehavior!!.state != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
                    val selectedStudentList = ArrayList<User>()
                    list.forEach { user ->
                        if (user.isSelected) {
                            selectedStudentList.add(user)
                        }
                    }
                    selectedCandidateAdapter.update(selectedStudentList)
                    bottomSheetBinder!!.selectedCandidateRecycler.layoutManager = LinearLayoutManager(applicationContext)
                    bottomSheetBinder!!.selectedCandidateRecycler.adapter = selectedCandidateAdapter
                    bottomSheetBinder!!.add.setOnClickListener {
                        if (selectedStudentList.isNotEmpty()) {
                            if (bottomSheetBinder!!.position.text.toString().isNotEmpty()) {
                                addCandidateDataToDb(bottomSheetBinder!!.position.text.toString(), selectedStudentList)
                            } else {
                                bottomSheetBinder!!.position.error = "Field Required"
                            }
                        }
                    }

                } else {
                    bottomSheetBehavior!!.setState(BottomSheetBehavior.STATE_COLLAPSED)
                }
            }
        }
        return true
    }

    private fun addCandidateDataToDb(postion: String, selectedStudentList: ArrayList<User>) {
        val data = hashMapOf<Any, Any>()
        val candidateData = hashMapOf<String, HashMap<String, String>>()
        var tempUser: User? = null
        selectedStudentList.forEach { user ->
            val candidateHashMap = hashMapOf<String, String>()
            candidateHashMap["name"] = user.name
            candidateHashMap["uid"] = user.uid
            candidateHashMap["course_year"] = user.courseYear
            candidateHashMap["course_name"] = user.courseName
            candidateHashMap["section"] = user.section
            candidateHashMap["profile_url"] = user.profileimageurl.toString()
            candidateData[UUID.randomUUID().toString()] = candidateHashMap
            tempUser = user
        }
        data["participants"] = candidateData
        data["position"] = postion
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        data["created_on"] = simpleDateFormat.format(Date())
        data["created_by"] = FirebaseAuth.getInstance().currentUser!!.uid
        data["council"] = "college"
        data["coursename"] = tempUser!!.courseName
        if (teacherCollegeUid != "") {
            val dataUid = UUID.randomUUID().toString()
            reference.child("Votify").child("Institution").child(teacherCollegeUid)
                .child("CollegeCouncil").child("CandidateData").child(dataUid)
                .setValue(data).addOnSuccessListener {
                    bottomSheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
                    Toast.makeText(applicationContext, "Candidates are added successfully!!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    bottomSheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
                    Toast.makeText(applicationContext, "Something went wrong!!", Toast.LENGTH_SHORT).show()
                }

        }
    }

    private fun loadStudentData(winnersUid: String, course: String, positionName: String) {
        reference.child("Votify").child("Users").orderByChild("uid").equalTo(winnersUid)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && snapshot.hasChildren()) {
//                        studentList.clear()
                        for (childSnapshot: DataSnapshot in snapshot.children) {
                            if (childSnapshot.key != FirebaseAuth.getInstance().currentUser!!.uid) {
                                val uid = childSnapshot.child("uid").value.toString()
                                val course_name = childSnapshot.child("coursename").value.toString()
                                val isTeacher = childSnapshot.child("isTeacher").value.toString()
                                val positionHolding = childSnapshot.child("positionHolding").value.toString()
                                println("positionHolding $positionHolding")
                                if (uid == winnersUid && course_name == course && isTeacher == "0") {
                                    val user = User(
                                        childSnapshot.key.toString(),
                                        childSnapshot.child("name").value.toString(),
                                        childSnapshot.child("collegeuid").value.toString(),
                                        childSnapshot.child("collegename").value.toString(),
                                        childSnapshot.child("coursename").value.toString(),
                                        childSnapshot.child("section").value.toString(),
                                        childSnapshot.child("mobilenumber").value.toString(),
                                        childSnapshot.child("courseyear").value.toString(),
                                        childSnapshot.child("emailid").value.toString(),
                                        childSnapshot.child("isTeacher").value.toString().toInt(),
                                        childSnapshot.child("profileimageurl").value.toString()
                                    )
                                    if (!classList.contains(user.courseYear)) {
                                        classList.add(user.courseYear)
                                    }
                                    if (!sectionList.contains(user.section)) {
                                        sectionList.add(user.section)
                                    }
                                    if (!positionList.contains(positionHolding)){
                                        positionList.add(positionHolding)
                                    }

                                    studentList.add(user)
                                }
                            }
                        }
                        adapter.update(studentList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }
}