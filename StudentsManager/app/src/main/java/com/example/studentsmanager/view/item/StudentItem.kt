package com.example.studentsmanager.view.item

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.studentsmanager.R
import com.example.studentsmanager.view.StudentActivity
import com.example.studentsmanager.commons.ui.rec.Item
import com.example.studentsmanager.commons.ui.rec.ItemController
import com.example.studentsmanager.model.SmPref
import com.example.studentsmanager.model.Student
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.startActivity

class StudentItem(val student: Student, val block: (StudentItem, View) -> Unit) : Item {

    override val controller: ItemController
        get() = Controller

    override fun areContentsTheSame(newItem: Item): Boolean = (student == (newItem as? StudentItem)?.student)

    override fun areItemsTheSame(newItem: Item): Boolean = (student == (newItem as? StudentItem)?.student)


    companion object Controller : ItemController {
        override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
            val view = parent.context.layoutInflater.inflate(R.layout.item_student, parent, false)

            return ViewHolder(view, view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: Item) {
            item as StudentItem
            holder as ViewHolder
            val student = item.student
            holder.apply {
                name.text = student.student_name
                sex.text = student.sex
                classId.text = student.class_id
                id.text = student.student_id
                year.text = "${student.year}年"
                age.text = "${student.age}年级"

                delete.setOnClickListener {
                    item.block(item, it)
                }
            }

            holder.rootView.setOnClickListener {
                it.context.startActivity<StudentActivity>(SmPref.SM_SID to student.student_id)
            }
        }

        class ViewHolder(itemView: View, val rootView: View) : RecyclerView.ViewHolder(itemView) {
            val name: TextView = itemView.findViewById(R.id.tv_name)
            val id: TextView = itemView.findViewById(R.id.tv_id)
            val age: TextView = itemView.findViewById(R.id.tv_age)
            val year: TextView = itemView.findViewById(R.id.tv_year)
            val sex: TextView = itemView.findViewById(R.id.tv_sex)
            val classId: TextView = itemView.findViewById(R.id.tv_class)
            val delete: ImageView = itemView.findViewById(R.id.iv_delete)
        }
    }
}

public fun MutableList<Item>.studentItem(student: Student, block: (StudentItem, View) -> Unit) = add(
    StudentItem(student, block)
)