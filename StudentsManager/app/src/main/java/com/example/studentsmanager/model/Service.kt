package com.example.studentsmanager.model

import com.example.studentsmanager.commons.CommonBody
import com.example.studentsmanager.commons.ServiceFactory
import kotlinx.coroutines.experimental.Deferred
import retrofit2.http.GET
import retrofit2.http.Query

interface Service {
    @GET("list/students")
    fun getStudents(): Deferred<CommonBody<List<Student>>>

    @GET("list/courses")
    fun getCourses(): Deferred<CommonBody<List<Course>>>

    @GET("list/schedules")
    fun getSchedules(): Deferred<CommonBody<List<Schedule>>>

    @GET("delete/deleteStudent")
    fun deleteStudent(@Query("student_id") id: String): Deferred<CommonBody<Int>>

    @GET("delete/deleteSchedule")
    fun deleteSchedule(@Query("student_id") sid: String, @Query("course_id") cid: String): Deferred<CommonBody<Int>>

    @GET("delete/deleteCourse")
    fun deleteCourse(@Query("course_id") id: String): Deferred<CommonBody<Int>>

    @GET("search/getStudentInfo")
    fun getStudentInfo(@Query("name_id") nameId: String): Deferred<CommonBody<AllInfo>>

    @GET("search/getCourseInfo")
    fun getCourseInfo(@Query("name_id") nameId: String): Deferred<CommonBody<AllInfo>>

    @GET("search/getGrade")
    fun getGrade(@Query("student_name_id") sNameId: String, @Query("course_name_id") cNameId: String): Deferred<CommonBody<List<Schedule>>>

    @GET("update/updateStudent")
    fun updateStudent(
        @Query("id") id: String, @Query("student_id") sid: String,
        @Query("student_name") sname: String, @Query("sex") sex: String,
        @Query("year") year: Int, @Query("age") age: Int,
        @Query("class_id") cid: String
    ): Deferred<CommonBody<Int>>

    @GET("update/updateCourse")
    fun updateCourse(
        @Query("id") id: String, @Query("course_id") cid: String,
        @Query("course_name") sname: String, @Query("teacher") teacher: String,
        @Query("credit") credit: Int, @Query("age_suit") age: Int,
        @Query("cancel_year") year: String?
    ): Deferred<CommonBody<Int>>

    @GET("update/updateSchedule")
    fun updateSchedule(
        @Query("student_id") sid: String, @Query("course_id") cid: String,
        @Query("score") score: Int, @Query("select_year") year: Int
    ): Deferred<CommonBody<Int>>


    @GET("insert/insertStudent")
    fun insertStudent(
        @Query("student_id") sid: String,
        @Query("student_name") sname: String, @Query("sex") sex: String,
        @Query("year") year: Int, @Query("age") age: Int,
        @Query("class_id") cid: String
    ): Deferred<CommonBody<Int>>

    @GET("insert/insertCourse")
    fun insertCourse(
        @Query("course_id") cid: String,
        @Query("course_name") sname: String, @Query("teacher") teacher: String,
        @Query("credit") credit: Int, @Query("age_suit") age: Int,
        @Query("cancel_year") year: String?
    ): Deferred<CommonBody<Int>>

    @GET("insert/insertSchedule")
    fun insertSchedule(
        @Query("student_id") sid: String, @Query("course_id") cid: String,
        @Query("score") score: Int, @Query("select_year") year: Int
    ): Deferred<CommonBody<Int>>

    @GET("count/countStudent")
    fun countStudent(@Query("student_name_id") sNameId: String): Deferred<CommonBody<List<NamNum>>>

    @GET("count/countCourse")
    fun countCourse(@Query("course_name_id") cNameId: String): Deferred<CommonBody<List<NamNum>>>

    @GET("count/countClass")
    fun countClass(@Query("class_id") sId: String, @Query("course_name_id") cNameId: String): Deferred<CommonBody<List<NamNum>>>

    companion object : Service by ServiceFactory()
}


data class Student(
    val age: Int,
    val class_id: String,//
    val sex: String,
    val student_id: String,//
    val student_name: String,//
    val year: Int
)

data class Course(
    val age_suit: Int,
    val cancel_year: String?,
    val course_id: String,
    val course_name: String,
    val credit: Int,
    val teacher: String
)

data class Schedule(
    val course_id: String,
    val score: Int,
    val select_year: Int,
    val student_id: String
)

data class AllInfo(
    val students: List<Student>,
    val courses: List<Course>,
    val schedules: List<Schedule>
)

data class NamNum(
    val name: String,
    val number: Double
)