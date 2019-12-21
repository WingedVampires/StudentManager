package com.example.studentsmanager.model

import android.app.PendingIntent

object SmPref {
    val SM_STUDENT = "student"
    val SM_COURSE = "course"
    val SM_SCHEDULE = "schedule"
    val INDEX_KEY = "key"
    val SM_SID = "sid"
    val SM_CID = "cid"
    val INSERT = "insert"
    var PERSON_ID = ""
    val SM_TITLE = "title_of_notification"
    val SM_TEXT = "text_of_notification"
    val pendingIntentList = mutableListOf<PendingIntent>()
    var isPerson = false
}