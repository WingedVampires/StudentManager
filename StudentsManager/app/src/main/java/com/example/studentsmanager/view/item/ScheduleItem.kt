package com.example.studentsmanager.view.item

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.studentsmanager.R
import com.example.studentsmanager.commons.ui.rec.Item
import com.example.studentsmanager.commons.ui.rec.ItemController
import com.example.studentsmanager.model.Schedule
import com.example.studentsmanager.model.SmPref
import com.example.studentsmanager.view.ScheduleActivity
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.startActivity

class ScheduleItem(val schedule: Schedule, val block: (ScheduleItem) -> Unit) : Item {
    override val controller: ItemController
        get() = Controller

    override fun areContentsTheSame(newItem: Item): Boolean = (schedule == (newItem as? ScheduleItem)?.schedule)

    override fun areItemsTheSame(newItem: Item): Boolean = (schedule == (newItem as? ScheduleItem)?.schedule)


    companion object Controller : ItemController {
        override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
            val view = parent.context.layoutInflater.inflate(R.layout.item_schedule, parent, false)

            return ViewHolder(view, view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: Item) {
            holder as ViewHolder
            item as ScheduleItem
            val schedule = item.schedule

            holder.apply {
                cid.text = schedule.course_id
                sid.text = schedule.student_id
                score.text = if (SmPref.isPerson) "${schedule.score}点" else "${schedule.score}分"
                selectYear.text = if (SmPref.isPerson) "${schedule.select_year}分" else "${schedule.select_year}年"

                delete.setOnClickListener {
                    item.block(item)
                }
            }

            holder.rootView.setOnClickListener {
                it.context.startActivity<ScheduleActivity>(
                    SmPref.SM_CID to schedule.course_id,
                    SmPref.SM_SID to schedule.student_id
                )
            }
        }

        class ViewHolder(itemView: View, val rootView: View) : RecyclerView.ViewHolder(itemView) {
            val cid: TextView = itemView.findViewById(R.id.tv_cid)
            val sid: TextView = itemView.findViewById(R.id.tv_sid)
            val score: TextView = itemView.findViewById(R.id.tv_sscore)
            val selectYear: TextView = itemView.findViewById(R.id.tv_year)
            val delete: ImageView = itemView.findViewById(R.id.iv_schedule_delete)
        }
    }
}

public fun MutableList<Item>.scheduleItem(schedule: Schedule, block: (ScheduleItem) -> Unit) = add(
    ScheduleItem(
        schedule,
        block
    )
)