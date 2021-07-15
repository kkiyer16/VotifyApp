package com.project.votify.models

data class User(var uid: String="", var name: String="", var collegeUid: String="",
                var collegeName: String="", var courseName: String="", var section: String="",
                var mobileNumber: String="", var courseYear: String="", var email: String="", var isTeacher: Int=-1,
                var profileimageurl: String="") {
    var isSelected = false

}