package com.example.studentsmanager.view

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Button
import com.example.studentsmanager.R
import com.example.studentsmanager.model.SmPref
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.startActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_login)
        window.statusBarColor = Color.parseColor("#D81B60")
        val button: Button = findViewById(R.id.bt_login)
        val buttonPerson: Button = findViewById(R.id.bt_login_person)

        buttonPerson.setOnClickListener {
            val s = et_login.text.toString()
            SmPref.PERSON_ID = s
            SmPref.isPerson = true
            it.context.startActivity<Main2Activity>(SmPref.SM_SID to s)
        }
        button.setOnClickListener {
            SmPref.isPerson = false
            it.context.startActivity<MainActivity>()
        }
    }
}
