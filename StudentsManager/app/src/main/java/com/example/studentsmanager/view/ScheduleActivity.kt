package com.example.studentsmanager.view

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Window
import android.widget.EditText
import com.example.studentsmanager.R
import com.example.studentsmanager.commons.experimental.extensions.QuietCoroutineExceptionHandler
import com.example.studentsmanager.commons.experimental.extensions.awaitAndHandle
import com.example.studentsmanager.commons.ui.rec.withItems
import com.example.studentsmanager.model.Schedule
import com.example.studentsmanager.model.Service
import com.example.studentsmanager.model.SmPref
import com.example.studentsmanager.view.item.EditItem
import com.example.studentsmanager.view.item.detailItem
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_schedule.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.startActivity

class ScheduleActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private val itemManager by lazy { recyclerView.withItems(mutableListOf()) }
    var cid: String = ""
    var sid: String = ""
    lateinit var courseId: EditItem
    lateinit var studentId: EditItem
    lateinit var score: EditItem
    lateinit var year: EditItem
    var type = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_schedule)
        tv_schedule_title.text = "课表"
        tb_schedule.apply {
            title = " "
            setSupportActionBar(this)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            setNavigationOnClickListener { onBackPressed() }
        }
        recyclerView = findViewById(R.id.rv_schedule)
        recyclerView.layoutManager = LinearLayoutManager(this@ScheduleActivity)

        sid = intent.getStringExtra(SmPref.SM_SID)
        cid = intent.getStringExtra(SmPref.SM_CID)

        if (sid != SmPref.INSERT) {
            refresh()
        } else {
            refreshForEdit()
        }

        iv_schedule_right.setOnClickListener {
            launch(UI + QuietCoroutineExceptionHandler) {
                val data = when (type) {
                    1 -> Service.updateSchedule(sid, cid, score.value.toInt(), year.value.toInt()).awaitAndHandle {
                        it.printStackTrace()
                        Toasty.error(this@ScheduleActivity, "上传失败").show()
                    } ?: return@launch
                    else -> Service.insertSchedule(
                        studentId.value,
                        courseId.value,
                        score.value.toInt(),
                        year.value.toInt()
                    ).awaitAndHandle {
                        it.printStackTrace()
                        Toasty.error(this@ScheduleActivity, "上传失败,输入数据有误")
                    } ?: return@launch
                }

                if (data.error_code == -1 && data.data != null && data.data >= 1) {
                    Toasty.success(this@ScheduleActivity, "上传成功").show()
                    if (SmPref.isPerson) {
                        this@ScheduleActivity.startActivity<Main2Activity>()
                    } else {
                        this@ScheduleActivity.startActivity<MainActivity>()
                    }
                }
            }
        }
    }

    fun refresh() {
        type = 1
        launch(UI + QuietCoroutineExceptionHandler) {
            Service.getGrade(sid, cid).awaitAndHandle {
                it.printStackTrace()
            }?.let {
                if (it.error_code == -1) {
                    val schedules = it.data ?: return@launch
                    if (schedules.isNotEmpty()) {
                        val schedule = schedules[0]
                        if (SmPref.isPerson) {
                            score = EditItem("小时", schedule.score.toString(), true)
                            year = EditItem("分钟", schedule.select_year.toString(), true)
                        } else {
                            score = EditItem("成绩", schedule.score.toString(), true)
                            year = EditItem("选择年份", schedule.select_year.toString(), true)
                        }
                        score = EditItem("成绩", schedule.score.toString(), true)
                        year = EditItem("选择年份", schedule.select_year.toString(), true)
                        itemManager.refreshAll {
                            detailItem("课程ID", schedule.course_id)
                            detailItem("学生ID", schedule.student_id)
                            add(score)
                            add(year)
                        }
                    } else {
                        refreshForEdit()
                        Toasty.error(this@ScheduleActivity, "暂无该课表数据，你可以选择插入该条数据").show()
                    }
                }
            }
        }
    }

    fun refreshForEdit() {
        type = 2
        courseId = EditItem("课程ID", "")
        studentId = EditItem("学生ID", "")
        if (SmPref.isPerson) {
            score = EditItem("小时", "", true)
            year = EditItem("分钟", "", true)
        } else {
            score = EditItem("成绩", "", true)
            year = EditItem("选择年份", "", true)
        }


        itemManager.refreshAll {
            add(courseId)
            add(studentId)
            add(score)
            add(year)
        }
    }
}
