package com.example.studentsmanager.model

import org.litepal.crud.LitePalSupport

class SelectedCourse : LitePalSupport() {
    var age_suit: Int = 0
    var cancel_year: String? = null
    var course_id: String = ""
    var course_name: String = ""
    var credit: Int = 0
    var teacher: String = ""
}