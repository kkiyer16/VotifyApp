package com.project.votify.models

class Candidates(
    val profile_url: String,
    val name: String,
    val course_year: String,
    val section: String,
    val uid: String
) {
}

class ClgCouncilCandidates(
    val profile_url: String,
    val name: String,
    val course_year: String,
    val section: String,
    val uid: String,
    val course_name: String
) {

}

class CastVote(
    var profimg: String,
    var name: String,
    var class_name: String,
    var class_year: String,
    var section: String,
    var position: String,
    var canduid: String,
    var participantUid: String,
) {
    var isSelected = false
}

data class Winner(
    var position: String = "",
    val courseName: String,
    var courseYear: String,
    var section: String
) {
    var posDataUID = ""
}

data class ClgCouncilWinner(
    var position: String = "",
    val courseName: String
) {
    var posDataUID = ""
}

class modelnames {
    companion object {
        var pollpos = "POSITION"
        var candidates = "CANDIDATES"
        var fragpollpos = "FRAGPOSITION"
        var castvotes = "VOTES"
        var viewResults = "VIEWDISPLAYRESULTS"
        var studViewResults = "STUDENTVIEWRESULTS"
        var winners = "WINNERS"

        //College Council
        var ClgCouncilPollPos = "ClgCouncilPollPos"
        var clgCouncilCandidates = "ClgCouncilCandidates"
        var clgcouncilfragpollpos = "CLGCOUNCILFRAGPOLLPOS"
        var clgcouncilcastvotes = "CLGCOUNCILVOTES"
        var clgcouncilviewresults = "CLGCOUNCILVIEWRESULTS"
        var clgcouncilstudviewresult = "CLGCOUNCILSTUDENTVIEWRESULTS"
        var clgcouncilwinners = "CLGCOUNCILWINNERS"
    }
}

