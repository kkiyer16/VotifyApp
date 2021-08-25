package com.project.votify

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.anychart.APIlib
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Pie
import com.anychart.core.ui.Title
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.project.votify.databinding.ActivityClgCouncilDisplayResultBinding
import com.project.votify.models.CCPositionData
import com.project.votify.models.PositionData
import com.project.votify.models.User
import org.apache.commons.lang3.StringUtils
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ClgCouncilDisplayResultActivity : AppCompatActivity() {

    lateinit var binding: ActivityClgCouncilDisplayResultBinding
    lateinit var pollUid: String
    lateinit var collegeUid: String
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var pie: Pie
    lateinit var candidateVotes: HashMap<String, Int>
    lateinit var candidateList: ArrayList<User>
    lateinit var positionData: CCPositionData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClgCouncilDisplayResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.title = ""
        setSupportActionBar(binding.toolbar)

        if (intent.hasExtra("ccPollUid")) {
            pollUid = intent.extras?.getString("ccPollUid").toString()
        }
        if (intent.hasExtra("ccCollegeUid")) {
            collegeUid = intent.extras?.getString("ccCollegeUid").toString()
        }

        databaseReference = FirebaseDatabase.getInstance().reference
        firebaseAuth = FirebaseAuth.getInstance()
        pie = AnyChart.pie()
        candidateVotes = hashMapOf<String, Int>()
        candidateList = ArrayList<User>()

        loadVotingData()

        binding.ccDisplayResultsButton.setOnClickListener {
            displayResult(positionData, candidateVotes, candidateList)
        }
    }

    private fun loadVotingData() {
        databaseReference.child("Votify").child("Institution").child(collegeUid).child("CollegeCouncil")
            .child("Polls").child(pollUid).addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists() && snapshot.hasChildren()) {
                            binding.ccWidgetLoadingIndicator.visibility = View.VISIBLE
                            binding.ccDataStatus.visibility = View.GONE
                            positionData = CCPositionData(
                                snapshot.child("position").value.toString(),
                                snapshot.child("course_name").value.toString(),
                                snapshot.child("candidate_data_uid").value.toString()
                            )
                            positionData.collegeUid = collegeUid
                            positionData.pollUid = pollUid
                            if (snapshot.hasChild("Votes")) {
                                val votingList = hashMapOf<String, String>()
                                for (childSnapshot: DataSnapshot in snapshot.child("Votes").children) {
                                    votingList[childSnapshot.key.toString()] = childSnapshot.value.toString()
                                }
                                loadCandidateData(positionData, votingList)
                            }
                        }
                        else {
                            binding.ccDataStatus.visibility = View.VISIBLE
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                }
            )
    }

    private fun loadCandidateData(positionData: CCPositionData, votingList: HashMap<String, String>) {
        databaseReference.child("Votify").child("Institution").child(collegeUid).child("CollegeCouncil")
            .child("CandidateData").child(positionData.candidateUidRef).child("participants")
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists() && snapshot.hasChildren()) {
                            binding.ccDataStatus.visibility = View.GONE
                            candidateList.clear()
                            for (childSnapshot: DataSnapshot in snapshot.children) {
                                val user = User(
                                    uid = childSnapshot.child("uid").value.toString(),
                                    name = childSnapshot.child("name").value.toString(),
                                    profileimageurl = childSnapshot.child("profile_url").value.toString(),
                                    courseYear = childSnapshot.child("course_year").value.toString(),
                                    section = childSnapshot.child("section").value.toString()
                                )
                                candidateList.add(user)
                            }
                            if (candidateList.size > 0) {
                                countVotes(positionData, votingList, candidateList)
                            }
                        } else {
                            binding.ccDataStatus.visibility = View.VISIBLE
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                }
            )
    }

    private fun countVotes(positionData: CCPositionData, votingList: HashMap<String, String>, candidateList: ArrayList<User>) {
        APIlib.getInstance().setActiveAnyChartView(binding.ccVotingResultsChart);
        candidateList.forEach {
            candidateVotes[it.uid] = 0
        }
        votingList.forEach { (_, candidateUid) ->
            if (candidateVotes.containsKey(candidateUid)) {
                candidateVotes[candidateUid] = candidateVotes[candidateUid]!!.toInt() + 1
            }
        }
        val pieData: ArrayList<DataEntry> = ArrayList()
        candidateList.forEach {
            pieData.add(ValueDataEntry("${it.name} : ${candidateVotes[it.uid]}", candidateVotes[it.uid]))
        }
        pie.data(pieData)
        val title: Title = pie.title()
        pie.title("Position ${StringUtils.capitalize(positionData.position)}")
        pie.labels().position("outside")
        binding.ccVotingResultsChart.visibility = View.VISIBLE
        binding.ccVotingResultsChart.setChart(pie)
        binding.ccWidgetLoadingIndicator.visibility = View.GONE
        binding.ccDataStatus.visibility = View.GONE
    }

    private fun displayResult(positionData: CCPositionData, candidateVotes: HashMap<String, Int>, candidateList: ArrayList<User>) {
        if (candidateVotes.isNotEmpty() && candidateList.isNotEmpty()) {
            val data = HashMap<String, Any>()
            data["position"] = positionData.position
            data["course_name"] = positionData.courseName
//            data["resdetails"] = "${positionData.courseName}${positionData.position}"
            data["created_on"] = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()).toString()

            val winners = candidateVotes.toList().sortedByDescending { (_, value) -> value }.take(1).toMap()
            databaseReference.child("Votify").child("Institution").child(collegeUid).child("CollegeCouncil")
                .child("PositionData").orderByChild("course_name").equalTo(data["course_name"].toString())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists() && snapshot.hasChildren()) {
                            var flag = 0;
                            for (childSnapshot: DataSnapshot in snapshot.children) {
                                val course_name = childSnapshot.child("course_name").value.toString()
                                if (course_name == data["course_name"] ) {
                                    if (childSnapshot.hasChild("representatives")) {
                                        val oldData = HashMap<String, String>()
                                        for (subSnapshot: DataSnapshot in childSnapshot.child("representatives").children) {
                                            oldData[subSnapshot.key.toString()] = subSnapshot.value.toString()
                                        }
                                        addRepresentatives(data, oldData, winners, childSnapshot.key.toString(), childSnapshot.child("created_on").value.toString())
                                    }
                                    flag = 1
                                    break
                                }
                            }
                            if (flag == 0) {
                                addRepresentatives(data, HashMap(), winners,)
                            }
                        }
                        else {
                            addRepresentatives(data, HashMap(), winners,)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                }
                )

        } else {
            Toast.makeText(applicationContext, "No data available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addRepresentatives(data: HashMap<String, Any>, oldData: HashMap<String, String>, winners: Map<String, Int>, parentUid: String = "", createdOn: String = "") {
        if (parentUid.isEmpty()) {
            val uuid = UUID.randomUUID().toString()
            data["representatives"] = winners
            databaseReference.child("Votify").child("Institution").child(collegeUid).child("CollegeCouncil")
                .child("PositionData").child(uuid).setValue(data).addOnCompleteListener {
                    Toast.makeText(applicationContext, "Winners are added", Toast.LENGTH_SHORT).show()
                }
        }
        else {
            data["representatives"] = winners
            databaseReference.child("Votify").child("Institution").child(collegeUid).child("CollegeCouncil")
                .child("PositionData").child(parentUid).updateChildren(data).addOnCompleteListener {
                    if (oldData.isNotEmpty()) {
                        addOldData(oldData, parentUid, createdOn)
                    }
                }
        }
    }

    private fun addOldData(oldData: java.util.HashMap<String, String>, parentUid: String, createdOn: String) {
        val data = HashMap<String, Any>()
        data["representatives"] = oldData
        data["created_on"] = createdOn
        databaseReference.child("Votify").child("Institution").child(collegeUid).child("CollegeCouncil")
            .child("PositionData").child(parentUid).child("oldData").child(UUID.randomUUID().toString())
            .setValue(data).addOnCompleteListener {
                Toast.makeText(applicationContext, "Winners are added", Toast.LENGTH_SHORT).show()
            }
    }

}