package com.example.studentsmanager.commons

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.BitmapFactory
import android.support.v4.app.NotificationCompat
import com.example.studentsmanager.R
import com.example.studentsmanager.model.SmPref
import com.example.studentsmanager.view.Main2Activity

class AutoReceiver : BroadcastReceiver() {
    private val NOTIFICATION_FLAG = 1

    @SuppressLint("NewApi")
    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == "VIDEO_TIMER") {
            val pendingIntent = PendingIntent.getActivity(
                context, 0,
                Intent(context, Main2Activity::class.java), 0
            )
            val title = intent.getStringExtra(SmPref.SM_TITLE) ?: "有一节课要上了"
            val text = intent.getStringExtra(SmPref.SM_TEXT) ?: "快上了，但不知道是哪个，还有几分钟！"
            val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val notification = NotificationCompat.Builder(context, "chat")
                .setContentTitle(title)
                .setContentText(text)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.sm_main)
                //.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon))
                .setAutoCancel(true)
                .build()
            manager.notify(1, notification)

        }
    }

    fun sendChatMsg(context: Context) {

    }
}