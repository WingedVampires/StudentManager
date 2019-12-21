package com.example.studentsmanager.view.item

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.studentsmanager.R
import com.example.studentsmanager.commons.ui.rec.Item
import com.example.studentsmanager.commons.ui.rec.ItemController
import org.jetbrains.anko.layoutInflater

class DetailItem(val title: String, val content: String) : Item {
    override val controller: ItemController
        get() = Controller

    override fun areContentsTheSame(newItem: Item): Boolean =
        (title == (newItem as? DetailItem)?.title && content == (newItem as? DetailItem)?.content)

    override fun areItemsTheSame(newItem: Item): Boolean =
        (title == (newItem as? DetailItem)?.title && content == (newItem as? DetailItem)?.content)

    companion object Controller : ItemController {
        override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
            val view = parent.context.layoutInflater.inflate(R.layout.item_detail, parent, false)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: Item) {
            item as DetailItem
            holder as ViewHolder
            holder.apply {
                title.text = item.title
                conntent.text = item.content
            }
        }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val title: TextView = itemView.findViewById(R.id.tv_detail_title)
            val conntent: TextView = itemView.findViewById(R.id.tv_detail_content)
        }
    }
}

public fun MutableList<Item>.detailItem(title: String, content: String) = add(DetailItem(title, content))