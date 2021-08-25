package com.project.votify.models

data class PositionData(var position:String="",val courseName:String,var courseYear:String,var section:String,var candidateUidRef:String) {
    var pollUid=""
    var collegeUid=""
    var endtime=""
    var starttime=""

}

data class CCPositionData(var position:String="",val courseName:String, val candidateUidRef: String=""){
    var pollUid=""
    var collegeUid=""
    var endtime=""
    var starttime=""
}