package com.example.studentsmanager.view

import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity
import android.view.Window
import com.example.studentsmanager.R
import com.example.studentsmanager.model.SmPref
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity() {
    lateinit var pagerAdapter: PagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_main)
        window.statusBarColor = Color.parseColor("#D81B60")

        val studentFragment = MainFragment.newInstance(SmPref.SM_STUDENT)
        val courseFragment = MainFragment.newInstance(SmPref.SM_COURSE)
        val scheduleFragment = MainFragment.newInstance(SmPref.SM_SCHEDULE)
        pagerAdapter = PagerAdapter(supportFragmentManager).apply {
            add(studentFragment, "所有学生")
            add(scheduleFragment, "课表")
            add(courseFragment, "所有课程")
        }

        vp_main.adapter = pagerAdapter

        tl_main.apply {
            setupWithViewPager(vp_main)
            tabGravity = TabLayout.GRAVITY_FILL
            setSelectedTabIndicatorColor(Color.parseColor("#D81B60"))
        }

        iv_search.setOnClickListener { it.context.startActivity<SearchActivity>() }
        iv_refresh.setOnClickListener { refresh() }
    }

    fun refresh() {
        val currentFragemnt = pagerAdapter.getCurrentFragment() as MainFragment

        when (currentFragemnt.typeOfFragment) {
            SmPref.SM_SCHEDULE -> currentFragemnt.refreshDataForSchedules()
            SmPref.SM_COURSE -> currentFragemnt.refreshDataForCourses()
            SmPref.SM_STUDENT -> currentFragemnt.refreshDataForStudents()
        }
    }
}