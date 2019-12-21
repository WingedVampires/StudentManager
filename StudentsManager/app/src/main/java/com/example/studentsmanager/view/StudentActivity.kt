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
import kotlinx.android.synthetic.main.activity_student.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.startActivity
import java.net.URLDecoder

class StudentActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private val itemManager by lazy { recyclerView.withItems(mutableListOf()) }
    var sid: String = ""
    lateinit var studentId: EditItem
    lateinit var age: EditItem
    lateinit var sex: EditItem
    lateinit var classId: EditItem
    lateinit var sname: EditItem
    lateinit var year: EditItem

    var type = 1
    lateinit var allInfo: AllInfo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_student)
        tb_student.apply {
            title = " "
            setSupportActionBar(this)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            setNavigationOnClickListener { onBackPressed() }
        }
        tv_student_title.text = "学生"
        recyclerView = findViewById(R.id.rv_student)
        recyclerView.layoutManager = LinearLayoutManager(this@StudentActivity)
        sid = intent.getStringExtra(SmPref.SM_SID)

        if (sid != SmPref.INSERT) {
            refresh()
        } else {
            refreshForEdit()
        }

        iv_student_right.setOnClickListener {
            launch(UI + QuietCoroutineExceptionHandler) {
                if (sex.value != "男" && sex.value != "女" ||
                    studentId.value.length != 10 || age.value.toInt() > 50 || age.value.toInt() < 10
                ) {
                    Toasty.error(this@StudentActivity, "数据输入有误").show()
                    return@launch
                }
                URLDecoder.decode(sex.value, "UTF-8")
                val data = when (type) {
                    1 -> Service.updateStudent(
                        sid,
                        studentId.value,
                        sname.value,
                        sex.value,
                        year.value.toInt(),
                        age.value.toInt(),
                        classId.value
                    ).awaitAndHandle {
                        it.printStackTrace()
                        Toasty.error(this@StudentActivity, "上传失败,数据输入有误").show()
                    } ?: return@launch
                    else -> Service.insertStudent(
                        studentId.value,
                        sname.value,
                        sex.value,
                        year.value.toInt(),
                        age.value.toInt(),
                        classId.value
                    ).awaitAndHandle {
                        it.printStackTrace()
                        Toasty.error(this@StudentActivity, "上传失败").show()
                    } ?: return@launch
                }

                if (data.error_code == -1 && data.data != null && data.data!! >= 1) {
                    Toasty.success(this@StudentActivity, "上传成功").show()
                    if (SmPref.isPerson) {
                        this@StudentActivity.startActivity<Main2Activity>()
                    } else {
                        this@StudentActivity.startActivity<MainActivity>()
                    }
                } else {
                    Toasty.error(this@StudentActivity, "上传失败").show()
                }
            }
        }
    }

    private fun refresh() {
        type = 1
        launch(UI + QuietCoroutineExceptionHandler) {
            val data = Service.getStudentInfo(sid).awaitAndHandle {
                it.printStackTrace()
                Toasty.error(this@StudentActivity, "加载失败").show()
            } ?: return@launch


            if (data.error_code == -1) {
                allInfo = data.data ?: return@launch


                if (allInfo.students.isNotEmpty()) {
                    val student = allInfo.students[0]
                    val avg = Service.countStudent(student.student_id).awaitAndHandle {
                        it.printStackTrace()
                        Toasty.error(this@StudentActivity, "加载失败").show()
                    } ?: return@launch
                    val avgGrade =
                        if (avg.error_code == -1 && avg.data != null && avg.data.isNotEmpty()) avg.data[0].number else 0

                    studentId = EditItem("学生ID", student.student_id)
                    sname = EditItem("学生姓名", student.student_name)
                    age = EditItem("年龄", student.age.toString(), true)
                    year = EditItem("入学年份", student.year.toString(), true)
                    sex = EditItem("性别", student.sex)
                    classId = EditItem("班级", student.class_id)

                    itemManager.refreshAll {
                        add(studentId)
                        add(sname)
                        add(age)
                        add(year)
                        add(sex)
                        add(classId)
                        detailItem("各科平均分", avgGrade.toString())

                        if (!SmPref.isPerson) {
                            allInfo.schedules.forEach {
                                scheduleItem(it) {
                                    launch(UI + QuietCoroutineExceptionHandler) {
                                        Service.deleteSchedule(it.schedule.student_id, it.schedule.course_id)
                                            .awaitAndHandle {
                                                it.printStackTrace()

                                                Toasty.error(this@StudentActivity, "删除失败")
                                            }?.let { c ->
                                                if (c.error_code == -1 && c.data != null && c.data >= 1) {
                                                    Toasty.success(this@StudentActivity, "删除成功").show()
                                                } else {
                                                    Toasty.error(this@StudentActivity, "删除失败").show()
                                                }
                                            }
                                    }
                                    refresh()
                                }
                            }
                        }
                    }
                } else {
                    refreshForEdit()
                    Toasty.error(this@StudentActivity, "暂无该学生数据，你可以选择插入该条数据").show()
                }
            }

        }
    }

    private fun refreshForEdit() {
        type = 2
        studentId = EditItem("学生ID", "")
        sname = EditItem("学生姓名", "")
        age = EditItem("年级", "", true)
        year = EditItem("入学年份", "", true)
        sex = EditItem("性别", "")
        classId = EditItem("班级", "")
        itemManager.refreshAll {
            add(studentId)
            add(sname)
            add(age)
            add(year)
            add(sex)
            add(classId)
        }
    }

}
