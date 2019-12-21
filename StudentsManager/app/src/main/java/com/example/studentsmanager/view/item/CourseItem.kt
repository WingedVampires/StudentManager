package com.example.studentsmanager.view.item

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.studentsmanager.R
import com.example.studentsmanager.commons.ui.rec.Item
import com.example.studentsmanager.commons.ui.rec.ItemController
import com.example.studentsmanager.model.Course
import com.example.studentsmanager.model.SmPref
import com.example.studentsmanager.view.CourseActivity
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.startActivity

class CourseItem(val course: Course, val block: (CourseItem) -> Unit) : Item {
    override val controller: ItemController
        get() = Controller

    override fun areContentsTheSame(newItem: Item): Boolean = (course == (newItem as? CourseItem)?.course)

    override fun areItemsTheSame(newItem: Item): Boolean = (course == (newItem as? CourseItem)?.course)

    companion object Controller : ItemController {
        override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
            val view = parent.context.layoutInflater.inflate(R.layout.item_course, parent, false)

            return ViewHolder(view, view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: Item) {

            holder as ViewHolder
            item as CourseItem
            val course = item.course
            holder.apply {
                cname.text = course.course_name
                cid.text = course.course_id
                teacher.text = course.teacher
                ageSuit.text = "${course.age_suit}年级"
                credit.text = "${course.credit}学分"

                if (course.cancel_year != null) {
                    cancelYear.text = "${course.cancel_year}年"
                } else {
                    cancelYear.visibility = View.GONE
                    cancelYearLabel.visibility = View.GONE
                }
                delete.setOnClickListener {
                    item.block(item)
                }
            }

            holder.rootView.setOnClickListener {
                it.context.startActivity<CourseActivity>(SmPref.SM_CID to course.course_id)
            }
        }

        class ViewHolder(itemView: View, val rootView: View) : RecyclerView.ViewHolder(itemView) {
            val cname: TextView = itemView.findViewById(R.id.tv_cname)
            val cid: TextView = itemView.findViewById(R.id.tv_cid)
            val ageSuit: TextView = itemView.findViewById(R.id.tv_age_suit)
            val cancelYear: TextView = itemView.findViewById(R.id.tv_cancel_year)
            val cancelYearLabel: ImageView = itemView.findViewById(R.id.iv_cancel_year)
            val teacher: TextView = itemView.findViewById(R.id.tv_teacher)
            val credit: TextView = itemView.findViewById(R.id.tv_credit)
            val delete: ImageView = itemView.findViewById(R.id.iv_course_delete)
        }
    }
}

public fun MutableList<Item>.courseItem(course: Course, block: (CourseItem) -> Unit) = add(
    CourseItem(
        course,
        block
    )
)