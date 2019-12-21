package com.example.studentsmanager.view

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Window
import com.example.studentsmanager.R
import com.example.studentsmanager.commons.experimental.extensions.QuietCoroutineExceptionHandler
import com.example.studentsmanager.commons.experimental.extensions.awaitAndHandle
import com.example.studentsmanager.commons.ui.rec.withItems
import com.example.studentsmanager.model.AllInfo
import com.example.studentsmanager.model.Service
import com.example.studentsmanager.model.SmPref
import com.example.studentsmanager.view.item.EditItem
import com.example.studentsmanager.view.item.detailItem
import com.example.studentsmanager.view.item.scheduleItem
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_course.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.startActivity

class CourseActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private val itemManager by lazy { recyclerView.withItems(mutableListOf()) }
    var type = 1

    var cid: String = ""
    lateinit var courseId: EditItem
    lateinit var credit: EditItem
    lateinit var ageSuit: EditItem
    lateinit var cYear: EditItem
    lateinit var cname: EditItem
    lateinit var teacher: EditItem

    lateinit var allInfo: AllInfo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_course)
        tv_course_title.text = "课程"
        tb_course.apply {
            title = " "
            setSupportActionBar(this)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            setNavigationOnClickListener { onBackPressed() }
        }
        recyclerView = findViewById(R.id.rv_course)
        recyclerView.layoutManager = LinearLayoutManager(this@CourseActivity)
        cid = intent.getStringExtra(SmPref.SM_CID)

        if (cid != SmPref.INSERT) {
            refresh()
        } else {
            refreshForEdit()
        }

        iv_course_right.setOnClickListener {
            launch(UI + QuietCoroutineExceptionHandler) {
                if (courseId.value.length != 7) {
                    Toasty.error(this@CourseActivity, "数据输入有误").show()
                    return@launch
                }
                val cancelYear = if (cYear.value.isBlank()) null else cYear.value
                val data = when (type) {
                    1 -> Service.updateCourse(
                        cid,
                        courseId.value,
                        cname.value,
                        teacher.value,
                        credit.value.toInt(),
                        ageSuit.value.toInt(),
                        cancelYear
                    ).awaitAndHandle {
                        it.printStackTrace()
                        Toasty.error(this@CourseActivity, "上传失败").show()
                    } ?: return@launch
                    else -> Service.insertCourse(
                        courseId.value,
                        cname.value,
                        teacher.value,
                        credit.value.toInt(),
                        ageSuit.value.toInt(),
                        cancelYear
                    ).awaitAndHandle {
                        it.printStackTrace()
                        Toasty.error(this@CourseActivity, "上传失败").show()
                    } ?: return@launch
                }

                if (data.error_code == -1 && data.data != null && data.data >= 1) {
                    Toasty.success(this@CourseActivity, "上传成功").show()
                    this@CourseActivity.startActivity<MainActivity>()
                }
            }
        }
    }

    private fun refresh() {
        type = 1
        launch(UI + QuietCoroutineExceptionHandler) {
            val data = Service.getCourseInfo(cid).awaitAndHandle {
                it.printStackTrace()
                Toasty.error(this@CourseActivity, "加载失败").show()
            } ?: return@launch


            if (data.error_code == -1) {
                allInfo = data.data ?: return@launch

                if (allInfo.courses.isNotEmpty()) {
                    val course = allInfo.courses[0]
                    val avg = Service.countCourse(cid).awaitAndHandle {
                        it.printStackTrace()
                        Toasty.error(this@CourseActivity, "加载失败").show()
                    } ?: return@launch
                    val avgGrade =
                        if (avg.error_code == -1 && avg.data != null && avg.data.isNotEmpty()) avg.data[0].number else 0
                    val cnYear = course.cancel_year ?: ""

                    courseId = EditItem("课程ID", course.course_id)
                    cname = EditItem("课程名称", course.course_name)
                    credit = EditItem("学分", course.credit.toString(), true)
                    teacher = EditItem("老师", course.teacher)
                    ageSuit = EditItem("适合年级", course.age_suit.toString(), true)
                    cYear = EditItem("取消年分", cnYear, true, true)

                    itemManager.refreshAll {
                        add(courseId)
                        add(cname)
                        add(credit)
                        add(teacher)
                        add(ageSuit)
                        add(cYear)
                        detailItem("所选课的学生平均分", avgGrade.toString())
                        detailItem("满分", "${allInfo.schedules.filter { it.score == 100 }.size}人")
                        detailItem("99 - 90", "${allInfo.schedules.filter { it.score in 90..99 }.size}人")
                        detailItem("89 - 80", "${allInfo.schedules.filter { it.score in 80..89 }.size}人")
                        detailItem("79 - 70", "${allInfo.schedules.filter { it.score in 70..79 }.size}人")
                        detailItem("69 - 60", "${allInfo.schedules.filter { it.score in 60..69 }.size}人")
                        detailItem("不及格", "${allInfo.schedules.filter { it.score < 60 }.size}人")


                        allInfo.schedules.forEach {
                            scheduleItem(it) {
                                launch(UI + QuietCoroutineExceptionHandler) {
                                    Service.deleteSchedule(it.schedule.student_id, it.schedule.course_id)
                                        .awaitAndHandle {
                                            it.printStackTrace()

                                            Toasty.error(this@CourseActivity, "删除失败").show()
                                        }?.let { c ->
                                            if (c.error_code == -1 && c.data != null && c.data!! >= 1) {
                                                Toasty.success(this@CourseActivity, "删除成功").show()
                                            } else {
                                                Toasty.error(this@CourseActivity, "删除失败").show()
                                            }
                                        }
                                }
                                refresh()
                            }
                        }
                    }

                } else {
                    refreshForEdit()
                    Toasty.error(this@CourseActivity, "暂无该课程数据，你可以选择插入该条数据").show()

                }
            }
        }
    }

    private fun refreshForEdit() {
        type = 2
        courseId = EditItem("课程ID", "")
        cname = EditItem("课程名称", "")
        credit = EditItem("学分", "", true)
        teacher = EditItem("老师", "")
        ageSuit = EditItem("适合年级", "", true)
        cYear = EditItem("取消年分", "", true, true)

        itemManager.refreshAll {
            add(courseId)
            add(cname)
            add(credit)
            add(teacher)
            add(ageSuit)
            add(cYear)
        }
    }
}
