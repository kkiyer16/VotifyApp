package com.project.votify.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mikhaellopez.circularimageview.CircularImageView
import com.project.votify.*
import com.project.votify.models.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class cardsAdapter(var type: String, var cardType: Int) : RecyclerView.Adapter<cardViewHolder>() {

    lateinit var context: Context
    lateinit var list: ArrayList<Any>
    var position_card_view = 1
    var cast_vote_card_view = 2
    var candidate_display_card_view = 3
    var winner_display_card = 4
    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
    private val fBase = FirebaseDatabase.getInstance().reference
    var clickedPosition: Int? = null
    private var pollUid = ""
    private var collegeUid = ""
    var currentuserSection = ""
    var currentUserCourseYear = ""

    fun setPollDetails(pollUid: String, collegeUid: String) {
        this.pollUid = pollUid
        this.collegeUid = collegeUid
    }

    fun set(ob: ArrayList<Any>, context: Context) {
        this.list = ob
        this.context = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): cardViewHolder {
        var view: View? = null

        when (viewType) {
            position_card_view -> {
                view = LayoutInflater.from(context)
                    .inflate(R.layout.create_polls_recycler_view_layout, parent, false)
            }
            cast_vote_card_view -> {
                view = LayoutInflater.from(context)
                    .inflate(R.layout.student_cast_vote_card_layout, parent, false)
            }
            candidate_display_card_view -> {
                view = LayoutInflater.from(context)
                    .inflate(R.layout.add_candidate_student_row, parent, false)
            }
            winner_display_card -> {
                view = LayoutInflater.from(context)
                    .inflate(R.layout.winner_display_layout, parent, false)
            }
        }
        return cardViewHolder(view!!)
    }

    override fun getItemViewType(position: Int): Int {
        when (cardType) {
            position_card_view -> {
                return position_card_view
            }
            cast_vote_card_view -> {
                return cast_vote_card_view
            }
            candidate_display_card_view -> {
                return candidate_display_card_view
            }
            winner_display_card -> {
                return winner_display_card
            }
            else -> {
                return 0
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun update(list: ArrayList<Any>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: cardViewHolder, position: Int) {
        when (type) {
            modelnames.pollpos -> {
                val positionData = list[position] as PositionData
                val createPollPosition =
                    holder.itemview.findViewById<TextView>(R.id.create_poll_position)
                val createPollClass = holder.itemview.findViewById<TextView>(R.id.create_poll_class)
                val createPollSection =
                    holder.itemview.findViewById<TextView>(R.id.create_poll_section)
                val viewMore = holder.itemview.findViewById<MaterialButton>(R.id.view_more)
                val positionPollCard =
                    holder.itemview.findViewById<MaterialCardView>(R.id.position_poll_card)
                createPollClass.text =
                    "Class: ${positionData.courseName} ${positionData.courseYear}"
                createPollPosition.text = "Position: ${positionData.position}"
                createPollSection.text = "Section: ${positionData.section}"

                viewMore.setOnClickListener {
                    context.startActivity(
                        Intent(
                            context,
                            CreatePollInsideActivity::class.java
                        ).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            putExtra("candidate_uid_ref", positionData.candidateUidRef)
                            putExtra("position", positionData.position)
                            putExtra("section", positionData.section)
                            putExtra("course_name", positionData.courseName)
                            putExtra("course_year", positionData.courseYear)
                        })
                }
            }

            modelnames.candidates -> {
                val userData = list[position] as Candidates
                val userProfImg = holder.itemView.findViewById<CircularImageView>(R.id.user_image)
                val userName = holder.itemView.findViewById<TextView>(R.id.username)
                val userClass = holder.itemView.findViewById<TextView>(R.id.student_class)
                val userSection = holder.itemView.findViewById<TextView>(R.id.student_section)
                Glide.with(context).load(userData.profile_url).placeholder(R.drawable.unisex_avatar)
                    .dontAnimate()
                    .fitCenter().into(userProfImg)
                userName.text = userData.name
                userClass.text = "Class: ${userData.course_year}"
                userSection.text = "Section: ${userData.section}"
            }

            modelnames.fragpollpos -> {
                val positionData = list[position] as PositionData
                val createPollPosition =
                    holder.itemview.findViewById<TextView>(R.id.create_poll_position)
                val createPollClass = holder.itemview.findViewById<TextView>(R.id.create_poll_class)
                val createPollSection =
                    holder.itemview.findViewById<TextView>(R.id.create_poll_section)
                val viewMore = holder.itemview.findViewById<MaterialButton>(R.id.view_more)
                val positionPollCard =
                    holder.itemview.findViewById<MaterialCardView>(R.id.position_poll_card)
                createPollClass.text =
                    "Class: ${positionData.courseName} ${positionData.courseYear}"
                createPollPosition.text = "Position: ${positionData.position}"
                createPollSection.text = "Section: ${positionData.section}"

                viewMore.setOnClickListener {
                    context.startActivity(Intent(context, StudentVotingActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra("candidate_uid_ref", positionData.candidateUidRef)
                        putExtra("position", positionData.position)
                        putExtra("section", positionData.section)
                        putExtra("course_name", positionData.courseName)
                        putExtra("course_year", positionData.courseYear)
                        putExtra("pollUid", positionData.pollUid)
                        putExtra("collegeUid", positionData.collegeUid)
                    })
                }
                fBase.child("Votify").child("Users").child(currentUser)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val current_time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Calendar.getInstance().time).toString()
                                currentUserCourseYear =
                                    snapshot.child("courseyear").value.toString()
                                currentuserSection = snapshot.child("section").value.toString()
                                var flag = 0
                                if (!currentuserSection.trim().equals(positionData.section.trim())) {
                                    positionPollCard.visibility = View.GONE
                                    flag = 1
                                }
                                if (!currentUserCourseYear.trim().equals(positionData.courseYear.trim())) {
                                    positionPollCard.visibility = View.GONE
                                    flag = 1
                                }
                                if (flag == 0) {
                                    if (current_time >= positionData.starttime) {
                                        positionPollCard.visibility = View.VISIBLE
                                    }
                                    if (current_time >= positionData.endtime) {
                                        positionPollCard.visibility = View.GONE
                                    }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
            }

            modelnames.castvotes -> {
                val castVote = list[position] as CastVote
                val cast_vote_card_view =
                    holder.itemView.findViewById<MaterialCardView>(R.id.votingMainCard)
                val cast_vote_profile_img =
                    holder.itemView.findViewById<CircularImageView>(R.id.voting_lay_profile_pic)
                val cast_vote_nameCand =
                    holder.itemView.findViewById<TextView>(R.id.voting_lay_name_of_cand)
                val cast_vote_classCand =
                    holder.itemView.findViewById<TextView>(R.id.voting_lay_class)
                val cast_vote_sectionCand =
                    holder.itemView.findViewById<TextView>(R.id.voting_lay_section)
                val cast_vote_positionCand =
                    holder.itemView.findViewById<TextView>(R.id.voting_lay_position_of_cand)
                val cast_vote_btnCand =
                    holder.itemView.findViewById<MaterialButton>(R.id.voting_lay_cast_vote_button)
                Glide.with(context).load(castVote.profimg).placeholder(R.drawable.unisex_avatar)
                    .dontAnimate().fitCenter()
                    .into(cast_vote_profile_img)
                cast_vote_nameCand.text = castVote.name
                cast_vote_classCand.text = "Class: ${castVote.class_name} ${castVote.class_year}"
                cast_vote_sectionCand.text = "Section: ${castVote.section}"
                cast_vote_positionCand.text = "Position: ${castVote.position}"

                cast_vote_btnCand.isEnabled = !(castVote.isSelected)

                cast_vote_btnCand.setOnClickListener {
                    clickedPosition = holder.adapterPosition
                    addVote(castVote)
                    castVote.isSelected = true
                    disableAll()
                    notifyDataSetChanged()
                }
            }

            modelnames.viewResults -> {
                val resData = list[position] as PositionData
                val createPollPosition =
                    holder.itemview.findViewById<TextView>(R.id.create_poll_position)
                val createPollClass = holder.itemview.findViewById<TextView>(R.id.create_poll_class)
                val createPollSection =
                    holder.itemview.findViewById<TextView>(R.id.create_poll_section)
                val viewMore = holder.itemview.findViewById<MaterialButton>(R.id.view_more)
                val positionPollCard =
                    holder.itemview.findViewById<MaterialCardView>(R.id.position_poll_card)
                createPollClass.text = "Class: ${resData.courseName} ${resData.courseYear}"
                createPollPosition.text = "Position: ${resData.position}"
                createPollSection.text = "Section: ${resData.section}"

                viewMore.setOnClickListener {
                    context.startActivity(
                        Intent(
                            context,
                            TeacherDisplayResultActivity::class.java
                        ).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            putExtra("pollUid", resData.pollUid)
                            putExtra("collegeUid", resData.collegeUid)
                        })
                }

//                val resDetails = "${resData.courseName} ${resData.courseYear} ${resData.section} ${resData.position}"
//                fBase.child("Votify").child("Institution").child(resData.collegeUid)
//                    .child("PositionData")
//                    .orderByChild("resdetails").equalTo(resDetails)
//                    .addValueEventListener(object : ValueEventListener {
//                        override fun onDataChange(snapshot: DataSnapshot) {
//                            if (snapshot.exists() && snapshot.hasChildren()) {
//                                positionPollCard.visibility = View.GONE
//                            }
//                        }
//
//                        override fun onCancelled(error: DatabaseError) {
//                        }
//                    })
            }

            modelnames.studViewResults -> {
                val winnerData = list[position] as Winner
                val createPollPosition =
                    holder.itemview.findViewById<TextView>(R.id.create_poll_position)
                val createPollClass = holder.itemview.findViewById<TextView>(R.id.create_poll_class)
                val createPollSection = holder.itemview.findViewById<TextView>(R.id.create_poll_section)
                val viewMore = holder.itemview.findViewById<MaterialButton>(R.id.view_more)
                val positionPollCard = holder.itemview.findViewById<MaterialCardView>(R.id.position_poll_card)
                createPollClass.text = "Class: ${winnerData.courseName} ${winnerData.courseYear}"
                createPollPosition.text = "Position: ${winnerData.position}"
                createPollSection.text = "Section: ${winnerData.section}"


//                fBase.child("Votify").child("Users").child(currentUser)
//                    .addValueEventListener(object : ValueEventListener {
//                        override fun onDataChange(snapshot: DataSnapshot) {
//                            if (snapshot.exists()) {
//                                val current_time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Calendar.getInstance().time).toString()
//                                currentUserCourseYear =
//                                    snapshot.child("courseyear").value.toString()
//                                currentuserSection = snapshot.child("section").value.toString()
//                                var flag = 0
//                                if (!currentuserSection.trim().equals(winnerData.section.trim())) {
//                                    positionPollCard.visibility = View.GONE
//                                    flag = 1
//                                }
//                                if (!currentUserCourseYear.trim().equals(winnerData.courseYear.trim())) {
//                                    positionPollCard.visibility = View.GONE
//                                    flag = 1
//                                }
//                            }
//                        }
//
//                        override fun onCancelled(error: DatabaseError) {
//                        }
//                    })

                viewMore.setOnClickListener {
                    context.startActivity(Intent(context, WinnersActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra("positionDataUid", winnerData.posDataUID)
                    })
                }

            }

            modelnames.winners -> {
//                val winner = list[position] as Winner
                val user = list[position] as User
                val winnerProfileImg =
                    holder.itemView.findViewById<CircularImageView>(R.id.winner_student_image)
                val winnerName = holder.itemView.findViewById<TextView>(R.id.winner_student_name)
                Glide.with(context).load(user.profileimageurl).placeholder(R.drawable.unisex_avatar)
                    .dontAnimate()
                    .fitCenter().into(winnerProfileImg)
                winnerName.text = user.name
            }

        }
    }

    private fun addVote(participant: CastVote) {
        val reference = FirebaseDatabase.getInstance().reference
        reference.child("Votify").child("Institution")
            .child(collegeUid).child("Polls").child(pollUid).child("Votes")
            .child(FirebaseAuth.getInstance().currentUser!!.uid.toString())
            .setValue(participant.participantUid).addOnSuccessListener {
                Toast.makeText(context, "Voted!!", Toast.LENGTH_SHORT).show()
                context.startActivity(Intent(context, StudentHomeActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })

            }

    }

    fun disableAll() {
        val run = GlobalScope.async {
            list.forEach { castVote ->
                (castVote as CastVote).isSelected = true
            }
            notifyDataSetChanged()
        }
    }
}

class cardViewHolder(var itemview: View) : RecyclerView.ViewHolder(itemview) {

}
