package com.example.studentsmanager.view

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.Window
import com.example.studentsmanager.R
import com.example.studentsmanager.commons.experimental.extensions.QuietCoroutineExceptionHandler
import com.example.studentsmanager.commons.experimental.extensions.awaitAndHandle
import com.example.studentsmanager.model.Service
import com.example.studentsmanager.model.SmPref
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.startActivity

class SearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_search)

        tb_search.apply {
            title = " "
            setSupportActionBar(this)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            setNavigationOnClickListener { onBackPressed() }
        }
        bn_check.setOnClickListener {
            val sIdName = et_st.text.toString()
            val cIdName = et_cr.text.toString()
            val cId = et_cl.text.toString()
            if (sIdName.isNotBlank() && cIdName.isNotBlank()) {
                it.context.startActivity<ScheduleActivity>(SmPref.SM_CID to cIdName, SmPref.SM_SID to sIdName)
            } else if (sIdName.isNotBlank()) {
                it.context.startActivity<StudentActivity>(SmPref.SM_SID to sIdName)
            } else if (cId.isNotBlank() && cIdName.isNotBlank()) {
                launch(UI + QuietCoroutineExceptionHandler) {
                    Service.countClass(cId, cIdName).awaitAndHandle {
                        it.printStackTrace()
                        Toasty.error(this@SearchActivity, "查询失败").show()
                    }?.let {
                        if (it.error_code == -1) {

                            val text = if (it.data != null && it.data?.isNotEmpty()) it.data.first().number else 0
                            val dialog = AlertDialog.Builder(this@SearchActivity)
                                .setTitle("班级平均成绩")
                                .setMessage(text.toString())
                                .setCancelable(true)
                                .create()
                            dialog.show()
                        }
                    }
                }
            } else if (cIdName.isNotBlank()) {
                it.context.startActivity<CourseActivity>(SmPref.SM_CID to cIdName)
            } else {
                Toasty.warning(this@SearchActivity, "请输入查询内容").show()
            }
        }
    }
}
