package com.project.votify

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.anychart.APIlib
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Pie
import com.anychart.core.ui.Title
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.project.votify.databinding.ActivityTeacherDisplayResultBinding
import com.project.votify.models.PositionData
import com.project.votify.models.User
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.apache.commons.lang3.StringUtils
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class TeacherDisplayResultActivity : AppCompatActivity() {

    lateinit var binding: ActivityTeacherDisplayResultBinding
    lateinit var pollUid: String
    lateinit var collegeUid: String
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var pie: Pie
    lateinit var candidateVotes: HashMap<String, Int>
    lateinit var candidateList: ArrayList<User>
    lateinit var positionData: PositionData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherDisplayResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.title = ""
        setSupportActionBar(binding.toolbar)

        if (intent.hasExtra("pollUid")) {
            pollUid = intent.extras?.getString("pollUid").toString()
        }
        if (intent.hasExtra("collegeUid")) {
            collegeUid = intent.extras?.getString("collegeUid").toString()
        }

        databaseReference = FirebaseDatabase.getInstance().reference
        firebaseAuth = FirebaseAuth.getInstance()
        pie = AnyChart.pie()
        candidateVotes = hashMapOf<String, Int>()
        candidateList = ArrayList<User>()

        loadVotingData()

        binding.displayResultsButton.setOnClickListener {
            displayResult(positionData, candidateVotes, candidateList)
        }
    }

    private fun loadVotingData() {
        databaseReference.child("Votify").child("Institution").child(collegeUid).child("Polls")
            .child(pollUid).addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists() && snapshot.hasChildren()) {
                            binding.widgetLoadingIndicator.visibility = View.VISIBLE
                            binding.dataStatus.visibility = View.GONE
                            positionData = PositionData(
                                snapshot.child("position").value.toString(),
                                snapshot.child("course_name").value.toString(),
                                snapshot.child("course_year").value.toString(),
                                snapshot.child("section").value.toString(),
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
                            binding.dataStatus.visibility = View.VISIBLE
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                }
            )
    }

    private fun loadCandidateData(positionData: PositionData, votingList: HashMap<String, String>) {
        databaseReference.child("Votify").child("Institution")
            .child(collegeUid).child("CandidateData").child(positionData.candidateUidRef)
            .child("participants").addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists() && snapshot.hasChildren()) {
                            binding.dataStatus.visibility = View.GONE
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
                            binding.dataStatus.visibility = View.VISIBLE
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                }
            )
    }

    private fun countVotes(positionData: PositionData, votingList: HashMap<String, String>, candidateList: ArrayList<User>) {
        APIlib.getInstance().setActiveAnyChartView(binding.votingResultsChart);
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
        pie.title("Position ${StringUtils.capitalize(positionData.position)} Section ${positionData.section}")
        pie.labels().position("outside")
        binding.votingResultsChart.visibility = View.VISIBLE
        binding.votingResultsChart.setChart(pie)
        binding.widgetLoadingIndicator.visibility = View.GONE
        binding.dataStatus.visibility = View.GONE
    }

    private fun displayResult(positionData: PositionData, candidateVotes: HashMap<String, Int>, candidateList: ArrayList<User>) {
        if (candidateVotes.isNotEmpty() && candidateList.isNotEmpty()) {
            val data = HashMap<String, Any>()
            data["position"] = positionData.position
            data["section"] = positionData.section
            data["course_name"] = positionData.courseName
            data["course_year"] = positionData.courseYear
            data["created_on"] = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()).toString()

            val winners = candidateVotes.toList().sortedByDescending { (_, value) -> value }.take(2).toMap()
            databaseReference.child("Votify").child("Institution").child(collegeUid).child("PositionData")
                .orderByChild("course_name").equalTo(data["course_name"].toString())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists() && snapshot.hasChildren()) {
                                var flag = 0;
                                for (childSnapshot: DataSnapshot in snapshot.children) {
                                    val course_year = childSnapshot.child("course_year").value.toString()
                                    val section = childSnapshot.child("section").value.toString()
                                    if (course_year == data["course_year"] && section == data["section"]) {
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

                    })

            winners.forEach { winnerUid->
                val pos = HashMap<String, Any>()
                pos["isClassCouncilMember"] = 1
                pos["positionHolding"] = data["position"].toString()
                println("Winners: ${winnerUid.key}")
                databaseReference.child("Votify").child("Users").child(winnerUid.key).updateChildren(pos)
            }

        } else {
            Toast.makeText(applicationContext, "No data available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addRepresentatives(data: HashMap<String, Any>, oldData: HashMap<String, String>, winners: Map<String, Int>, parentUid: String = "", createdOn: String = "") {
        if (parentUid.isEmpty()) {
            val uuid = UUID.randomUUID().toString()
            data["representatives"] = winners
            databaseReference.child("Votify").child("Institution").child(collegeUid)
                .child("PositionData").child(uuid).setValue(data).addOnCompleteListener {
                    Toast.makeText(applicationContext, "Winners are added", Toast.LENGTH_SHORT).show()
                }
        }
        else {
            data["representatives"] = winners
            databaseReference.child("Votify").child("Institution").child(collegeUid)
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
        databaseReference.child("Votify").child("Institution").child(collegeUid).child("PositionData")
            .child(parentUid).child("oldData").child(UUID.randomUUID().toString())
            .setValue(data).addOnCompleteListener {
                Toast.makeText(applicationContext, "Winners are added", Toast.LENGTH_SHORT).show()
            }
    }
}