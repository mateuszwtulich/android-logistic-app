package com.example.podriver

import java.security.Timestamp

class JobDto(
    val number: Long,
    var status: String,
    val date: String,
    val commissionedParty: String,
    val principalId: Long,
    val managerId: Long,
    val driverId: Long,
    val cargoId: Long,
    val unloadingId: Long,
    val loadingId: Long,
    val placeOfIssue: String,
    val weight: Double,
    val payRate: Int,
    val comment: String
)