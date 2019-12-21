package com.example.studentsmanager.view.item

import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import com.example.studentsmanager.R
import com.example.studentsmanager.commons.ui.rec.Item
import com.example.studentsmanager.commons.ui.rec.ItemController
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.sdk25.coroutines.textChangedListener

class EditItem(val title: String, val content: String, val onlyNumber: Boolean = false, val setNull: Boolean = false) :
    Item {
    var value: String = ""
    override val controller: ItemController
        get() = Controller

    override fun areContentsTheSame(newItem: Item): Boolean =
        (title == (newItem as? DetailItem)?.title && content == (newItem as? DetailItem)?.content)

    override fun areItemsTheSame(newItem: Item): Boolean =
        (title == (newItem as? DetailItem)?.title && content == (newItem as? DetailItem)?.content)

    companion object Controller : ItemController {
        override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
            val view = parent.context.layoutInflater.inflate(R.layout.item_edit, parent, false)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: Item) {
            item as EditItem
            holder as ViewHolder
            holder.apply {
                title.text = item.title
                conntent.setText(item.content)
                item.value = item.content
                if (item.onlyNumber) {
                    conntent.inputType = EditorInfo.TYPE_CLASS_NUMBER
                }
                conntent.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

                    override fun afterTextChanged(s: Editable?) = Unit

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        if (conntent.text.isNotBlank()) {
                            item.value = conntent.text.toString()
                        }

                        if (item.setNull) {
                            item.value = ""
                        }
                    }
                })
            }
        }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val title: TextView = itemView.findViewById(R.id.tv_edit_title)
            val conntent: EditText = itemView.findViewById(R.id.tv_edit_content)
        }
    }
}