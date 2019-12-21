package com.example.studentsmanager.view

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.studentsmanager.R
import com.example.studentsmanager.commons.experimental.extensions.QuietCoroutineExceptionHandler
import com.example.studentsmanager.commons.experimental.extensions.awaitAndHandle
import com.example.studentsmanager.commons.ui.rec.ItemManager
import com.example.studentsmanager.commons.ui.rec.withItems
import com.example.studentsmanager.model.*
import com.example.studentsmanager.view.item.courseItem
import com.example.studentsmanager.view.item.scheduleItem
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.startActivity
import org.litepal.LitePal
import org.litepal.extension.deleteAll
import org.litepal.extension.findAll


class PersonFragment : Fragment() {
    lateinit var recyclerView: RecyclerView
    lateinit var progressBar: ConstraintLayout
    private lateinit var itemManager: ItemManager
    var typeOfFragment = SmPref.SM_SCHEDULE

    companion object {
        fun newInstance(type: String): PersonFragment {
            val args = Bundle()
            args.putString(SmPref.INDEX_KEY, type)
            val fragment = PersonFragment()
            fragment.arguments = args

            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        recyclerView = view.findViewById(R.id.rv_fragment_precious)
        progressBar = view.findViewById(R.id.cl_fragment_loading)
        recyclerView.layoutManager = LinearLayoutManager(this.activity)
        itemManager = recyclerView.withItems(listOf())
        val bundle = arguments
        typeOfFragment = bundle!!.getString(SmPref.INDEX_KEY)
        progressBar.visibility = View.GONE
        when (typeOfFragment) {
            SmPref.SM_COURSE -> refreshDataForCourses()
            SmPref.SM_SCHEDULE -> refreshDataForSchedules()
        }

        val button: Button = view.findViewById(R.id.bn_insert)
        button.visibility = when (typeOfFragment) {
            SmPref.SM_COURSE -> View.GONE
            else -> View.VISIBLE
        }
        button.setOnClickListener {
            when (typeOfFragment) {
                SmPref.SM_SCHEDULE -> it.context.startActivity<ScheduleActivity>(
                    SmPref.SM_SID to SmPref.INSERT,
                    SmPref.SM_CID to SmPref.INSERT
                )
            }
        }

        return view
    }

    fun refreshDataForCourses() {
        progressBar.visibility = View.VISIBLE
        itemManager.refreshAll { }
        var courses: MutableList<Course> = mutableListOf()
        launch(UI + QuietCoroutineExceptionHandler) {
            val list = Service.getStudentInfo(SmPref.PERSON_ID).awaitAndHandle { t ->
                t.printStackTrace()
                progressBar.visibility = View.GONE
                val sList = LitePal.findAll<SelectedCourse>()
                courses.clear()
                sList.forEach {
                    courses.add(
                        Course(
                            it.age_suit,
                            it.cancel_year,
                            it.course_id,
                            it.course_name,
                            it.credit,
                            it.teacher
                        )
                    )
                }

                Toasty.warning(this@PersonFragment.context!!, "加载缓存内容").show()
            }?.data

            progressBar.visibility = View.GONE
            if (list != null) {
                courses = list.courses.toMutableList()
            }

            LitePal.deleteAll<SelectedCourse>()
            courses.forEach {
                val selectedCourse = SelectedCourse().apply {
                    age_suit = it.age_suit
                    cancel_year = it.cancel_year
                    course_id = it.course_id
                    course_name = it.course_name
                    teacher = it.teacher
                    credit = it.credit
                }

                if (!selectedCourse.save()) {
                    Toasty.error(this@PersonFragment.context!!, "课程信息存储失败").show()
                }

            }
            Toasty.success(this@PersonFragment.context!!, "加载成功").show()

            itemManager.refreshAll {
                courses.forEach {
                    if (!it.cancel_year.equals("2019")) {
                        courseItem(it) { item ->
                            launch(UI + QuietCoroutineExceptionHandler) {
                                Service.deleteCourse(item.course.course_id).awaitAndHandle {
                                    it.printStackTrace()

                                    Toasty.error(this@PersonFragment.context!!, "删除失败").show()
                                }?.let { c ->
                                    if (c.error_code == -1 && c.data != null && c.data >= 1) {
                                        Toasty.success(this@PersonFragment.context!!, "删除成功").show()
                                    } else {
                                        Toasty.error(this@PersonFragment.context!!, "删除失败").show()
                                    }
                                }
                            }
                            refreshDataForCourses()
                        }
                    }
                }
            }

            recyclerView.scrollToPosition(0)
        }
    }

    fun refreshDataForSchedules() {
        progressBar.visibility = View.VISIBLE
        itemManager.refreshAll { }
        var schedules: MutableList<Schedule> = mutableListOf()
        launch(UI + QuietCoroutineExceptionHandler) {
            val list = Service.getStudentInfo(SmPref.PERSON_ID).awaitAndHandle {
                it.printStackTrace()
                progressBar.visibility = View.GONE
                val sList = LitePal.findAll<SelectedSchedule>()
                schedules.clear()
                sList.forEach {
                    schedules.add(Schedule(it.course_id, it.score, it.select_year, it.student_id))
                }
                Toasty.warning(this@PersonFragment.context!!, "加载缓存内容").show()
            }?.data

            progressBar.visibility = View.GONE
            if (list != null) {
                schedules = list.schedules.toMutableList()
                Toasty.success(this@PersonFragment.context!!, "加载成功").show()
            }


            LitePal.deleteAll<SelectedSchedule>()
            schedules.forEach {
                val selectedSchedule = SelectedSchedule().apply {
                    course_id = it.course_id
                    student_id = it.student_id
                    select_year = it.select_year
                    score = it.score
                }

                if (!selectedSchedule.save()) {
                    Toasty.error(this@PersonFragment.context!!, "课表储存失败").show()
                }
            }

            itemManager.refreshAll {
                schedules.forEach {
                    scheduleItem(it) {
                        launch(UI + QuietCoroutineExceptionHandler) {
                            Service.deleteSchedule(it.schedule.student_id, it.schedule.course_id).awaitAndHandle {
                                it.printStackTrace()

                                Toasty.error(this@PersonFragment.context!!, "删除失败").show()
                            }?.let { c ->
                                if (c.error_code == -1 && c.data != null && c.data >= 1) {
                                    Toasty.success(this@PersonFragment.context!!, "删除成功").show()
                                } else {
                                    Toasty.error(this@PersonFragment.context!!, "删除失败").show()
                                }
                            }
                        }
                        refreshDataForSchedules()
                    }
                }
            }
            recyclerView.scrollToPosition(0)
        }
    }
}