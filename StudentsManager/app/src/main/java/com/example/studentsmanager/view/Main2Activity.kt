package com.example.studentsmanager.view

import android.annotation.TargetApi
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity
import android.view.Window
import com.example.studentsmanager.R
import com.example.studentsmanager.commons.AutoReceiver
import com.example.studentsmanager.commons.experimental.extensions.QuietCoroutineExceptionHandler
import com.example.studentsmanager.commons.experimental.extensions.awaitAndHandle
import com.example.studentsmanager.model.Schedule
import com.example.studentsmanager.model.Service
import com.example.studentsmanager.model.SmPref
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.collections.forEachWithIndex
import org.jetbrains.anko.startActivity
import org.litepal.LitePal
import java.text.SimpleDateFormat
import java.util.*


class Main2Activity : AppCompatActivity() {
    lateinit var pagerAdapter: PagerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_main)
        window.statusBarColor = Color.parseColor("#D81B60")
        LitePal.initialize(this)
        val courseFragment = MainFragment.newInstance(SmPref.SM_COURSE)
        val pScheduleFragment = PersonFragment.newInstance(SmPref.SM_SCHEDULE)
        val pCourseFragment = PersonFragment.newInstance(SmPref.SM_COURSE)
        pagerAdapter = PagerAdapter(supportFragmentManager).apply {
            add(pScheduleFragment, "课表")
            add(pCourseFragment, "已选课程")
            add(courseFragment, "所有课程")
        }

        vp_main.apply {
            adapter = pagerAdapter
        }

        tl_main.apply {
            setupWithViewPager(vp_main)
            tabGravity = TabLayout.GRAVITY_FILL
            setSelectedTabIndicatorColor(Color.parseColor("#D81B60"))
        }

        iv_refresh.setOnClickListener { refresh() }
        iv_search.apply {
            setImageResource(R.drawable.sm_person)
            setOnClickListener {
                it.context.startActivity<StudentActivity>(SmPref.SM_SID to SmPref.PERSON_ID)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var channelId = "chat"
            var channelName = "聊天消息"
            var importance = NotificationManager.IMPORTANCE_HIGH
            createNotificationChannel(channelId, channelName, importance)
            channelId = "subscribe"
            channelName = "订阅消息"
            importance = NotificationManager.IMPORTANCE_DEFAULT
            createNotificationChannel(channelId, channelName, importance)
        }
        refreshAnnotation()

    }


    fun refresh() {
        if (pagerAdapter.getCurrentFragment() is MainFragment) {
            val currentFragemnt = pagerAdapter.getCurrentFragment() as MainFragment
            when (currentFragemnt.typeOfFragment) {
                SmPref.SM_SCHEDULE -> currentFragemnt.refreshDataForSchedules()
                SmPref.SM_COURSE -> currentFragemnt.refreshDataForCourses()
                SmPref.SM_STUDENT -> currentFragemnt.refreshDataForStudents()
            }
        } else {
            val currentFragemnt = pagerAdapter.getCurrentFragment() as PersonFragment
            when (currentFragemnt.typeOfFragment) {
                SmPref.SM_SCHEDULE -> currentFragemnt.refreshDataForSchedules()
                SmPref.SM_COURSE -> currentFragemnt.refreshDataForCourses()
            }
        }


    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String, importance: Int) {
        val channel = NotificationChannel(channelId, channelName, importance)
        val notificationManager = getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun cancelPeIntent() {
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        SmPref.pendingIntentList.forEach {
            am.cancel(it)
        }
    }

    private fun refreshAnnotation() {
        launch(UI + QuietCoroutineExceptionHandler) {
            val list = Service.getStudentInfo(SmPref.PERSON_ID).awaitAndHandle {
                it.printStackTrace()
                Toasty.error(this@Main2Activity, "通知设置失败").show()
            }?.data

            val schedules = list?.schedules ?: return@launch
            cancelPeIntent()
            val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            var count = 0
            schedules.forEachWithIndex { i: Int, s: Schedule ->
                val today = SimpleDateFormat("HH:mm").format(Date()).split(":")
                if (s.score in 0..23 && s.select_year in 0..59 && s.score >= today[0].toInt()) {
                    val intent = Intent(this@Main2Activity, AutoReceiver::class.java)
                    intent.apply {
                        action = "VIDEO_TIMER"
                        putExtra(SmPref.SM_TITLE, "课程编号为${s.course_id}的课快要要上课了")
                        putExtra(SmPref.SM_TEXT, "${s.score}点${s.select_year}分上课")
                    }

                    val sender = PendingIntent.getBroadcast(this@Main2Activity, i, intent, FLAG_CANCEL_CURRENT)
                    SmPref.pendingIntentList.add(sender)
                    val triggerAtMillis = Date().time
                    val nHour = s.score - today[0].toInt()
                    val nMin = s.select_year - today[1].toInt()
                    val disTime = if ((60 * nHour + nMin - 2) > 0) (60 * nHour + nMin - 1) else (60 * nHour + nMin)
                    am.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis + disTime * 1000, sender
                    )
                    count++
                }
            }

            Toasty.success(this@Main2Activity, "成功设置${count}个通知").show()
        }
    }

}
