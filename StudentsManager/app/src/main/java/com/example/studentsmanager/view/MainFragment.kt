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
import com.example.studentsmanager.view.item.*
import com.example.studentsmanager.commons.experimental.extensions.QuietCoroutineExceptionHandler
import com.example.studentsmanager.commons.experimental.extensions.awaitAndHandle
import com.example.studentsmanager.commons.ui.rec.ItemManager
import com.example.studentsmanager.commons.ui.rec.lightText
import com.example.studentsmanager.commons.ui.rec.withItems
import com.example.studentsmanager.commons.ui.text.spanned
import com.example.studentsmanager.model.SelectedCourse
import com.example.studentsmanager.model.Service
import com.example.studentsmanager.model.SmPref
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.dip
import org.jetbrains.anko.horizontalPadding
import org.jetbrains.anko.startActivity

class MainFragment : Fragment() {
    lateinit var recyclerView: RecyclerView
    lateinit var progressBar: ConstraintLayout
    private lateinit var itemManager: ItemManager
    var typeOfFragment = SmPref.SM_STUDENT

    companion object {
        fun newInstance(type: String): MainFragment {
            val args = Bundle()
            args.putString(SmPref.INDEX_KEY, type)
            val fragment = MainFragment()
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
            SmPref.SM_STUDENT -> refreshDataForStudents()
        }
        
        val button: Button = view.findViewById(R.id.bn_insert)
        button.visibility = if (SmPref.isPerson) View.GONE else View.VISIBLE
        button.setOnClickListener {
            when (typeOfFragment) {
                SmPref.SM_COURSE -> it.context.startActivity<CourseActivity>(SmPref.SM_CID to SmPref.INSERT)
                SmPref.SM_SCHEDULE -> it.context.startActivity<ScheduleActivity>(
                    SmPref.SM_SID to SmPref.INSERT,
                    SmPref.SM_CID to SmPref.INSERT
                )
                SmPref.SM_STUDENT -> it.context.startActivity<StudentActivity>(SmPref.SM_SID to SmPref.INSERT)
            }
        }

        return view
    }

     fun refreshDataForStudents() {
        progressBar.visibility = View.VISIBLE
        itemManager.refreshAll { }
        launch(UI + QuietCoroutineExceptionHandler) {
            val list = Service.getStudents().awaitAndHandle {
                it.printStackTrace()
                progressBar.visibility = View.GONE
                itemManager.refreshAll {
                    lightText("") {
                        horizontalPadding = dip(16)
                        text = ("加载失败，请看log").spanned
                    }
                }
            }?.data

            progressBar.visibility = View.GONE
            val students = list ?: return@launch
            Toasty.success(this@MainFragment.context!!, "加载成功").show()

            itemManager.refreshAll {
                students.forEach {
                    studentItem(it) { item, v ->
                        launch(UI + QuietCoroutineExceptionHandler) {
                            Service.deleteStudent(item.student.student_id).awaitAndHandle {
                                it.printStackTrace()

                                Toasty.error(this@MainFragment.context!!, "删除失败").show()
                            }?.let { c ->
                                if (c.error_code == -1 && c.data != null && c.data!! >= 1) {
                                    Toasty.success(this@MainFragment.context!!, "删除成功").show()
                                } else {
                                    Toasty.error(this@MainFragment.context!!, "删除失败").show()
                                }
                            }
                        }
                        refreshDataForStudents()
                    }
                }
            }
            recyclerView.scrollToPosition(0)
        }
    }

     fun refreshDataForCourses() {
        progressBar.visibility = View.VISIBLE
        itemManager.refreshAll { }
        launch(UI + QuietCoroutineExceptionHandler) {
            val list = Service.getCourses().awaitAndHandle {
                it.printStackTrace()
                progressBar.visibility = View.GONE
                itemManager.refreshAll {
                    lightText("") {
                        horizontalPadding = dip(16)
                        text = ("加载失败，请看log").spanned
                    }
                }
            }?.data

            progressBar.visibility = View.GONE
            val courses = list ?: return@launch

            Toasty.success(this@MainFragment.context!!, "加载成功").show()

            itemManager.refreshAll {
                courses.forEach {
                    if (!it.cancel_year.equals("2019")) {
                        courseItem(it) { item ->
                            launch(UI + QuietCoroutineExceptionHandler) {
                                Service.deleteCourse(item.course.course_id).awaitAndHandle {
                                    it.printStackTrace()

                                    Toasty.error(this@MainFragment.context!!, "删除失败").show()
                                }?.let { c ->
                                    if (c.error_code == -1 && c.data != null && c.data >= 1) {
                                        Toasty.success(this@MainFragment.context!!, "删除成功").show()
                                    } else {
                                        Toasty.error(this@MainFragment.context!!, "删除失败").show()
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
        launch(UI + QuietCoroutineExceptionHandler) {
            val list = Service.getSchedules().awaitAndHandle {
                it.printStackTrace()
                progressBar.visibility = View.GONE
                itemManager.refreshAll {
                    lightText("") {
                        horizontalPadding = dip(16)
                        text = ("加载失败，请看log").spanned
                    }
                }
            }?.data

            progressBar.visibility = View.GONE
            val schedules = list ?: return@launch

            Toasty.success(this@MainFragment.context!!, "加载成功").show()

            itemManager.refreshAll {
                schedules.forEach {
                    scheduleItem(it) {
                        launch(UI + QuietCoroutineExceptionHandler) {
                            Service.deleteSchedule(it.schedule.student_id, it.schedule.course_id).awaitAndHandle {
                                it.printStackTrace()

                                Toasty.error(this@MainFragment.context!!, "删除失败").show()
                            }?.let { c ->
                                if (c.error_code == -1 && c.data != null && c.data >= 1) {
                                    Toasty.success(this@MainFragment.context!!, "删除成功").show()
                                } else {
                                    Toasty.error(this@MainFragment.context!!, "删除失败").show()
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