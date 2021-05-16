package com.project.votify.models

data class VotingData(
    var postionData: PositionData, var votingList: Map<String, String>,
    var candidateList: List<User>
)
