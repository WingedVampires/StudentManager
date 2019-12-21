package com.example.studentsmanager.model

import org.litepal.crud.LitePalSupport

class SelectedSchedule : LitePalSupport() {
    var course_id: String = ""
    var score: Int = 0
    var select_year: Int = 0
    var student_id: String = ""
}