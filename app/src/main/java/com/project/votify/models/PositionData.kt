package com.project.votify.models

data class PositionData(var position:String="",val courseName:String,var courseYear:String,var section:String,var candidateUidRef:String) {
    var pollUid=""
    var collegeUid=""

}