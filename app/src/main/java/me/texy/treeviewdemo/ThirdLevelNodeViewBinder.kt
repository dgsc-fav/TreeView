package me.texy.treeviewdemo

import android.view.View
import android.widget.TextView
import me.texy.treeview.TreeNode
import me.texy.treeview.base.CheckableNodeViewBinder

/**
 * Created by zxy on 17/4/23.
 */
class ThirdLevelNodeViewBinder(itemView: View) : CheckableNodeViewBinder<String, String>(itemView) {

    var textView: TextView
    override val checkableViewId: Int
        get() = R.id.checkBox

    override fun bindView(treeNode: TreeNode<String, String>) {
        var value: Any = "no value"
        val valueSet = treeNode.valuesSet
        if (valueSet != null) {
            value = valueSet.value!!
        }
        textView.text = value.toString()
    }

    init {
        textView = itemView.findViewById<View>(R.id.node_name_view) as TextView
    }
}