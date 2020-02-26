package me.texy.treeviewdemo

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import me.texy.treeview.TreeNode
import me.texy.treeview.base.CheckableNodeViewBinder

/**
 * Created by zxy on 17/4/23.
 */
class SecondLevelNodeViewBinder(itemView: View) : CheckableNodeViewBinder<String, String>(itemView) {
    var textView: TextView
    var imageView: ImageView
    override val checkableViewId: Int
        get() = R.id.checkBox

    override fun bindView(treeNode: TreeNode<String, String>) {
        var value: Any = "no value"
        val valueSet = treeNode.valuesSet
        value = if (valueSet != null) {
            valueSet.value!!
        } else {
            treeNode.category.toString() + " " + treeNode.categoryValue
        }
        textView.text = value.toString()
        imageView.setRotation(if (treeNode.isExpanded) 90f else 0f)
        imageView.visibility = if (treeNode.hasChild()) View.VISIBLE else View.INVISIBLE
    }

    override fun onNodeToggled(treeNode: TreeNode<String, String>, expand: Boolean) {
        if (expand) {
            imageView.animate().rotation(90f).setDuration(200).start()
        } else {
            imageView.animate().rotation(0f).setDuration(200).start()
        }
    }

    init {
        textView = itemView.findViewById<View>(R.id.node_name_view) as TextView
        imageView = itemView.findViewById<View>(R.id.arrow_img) as ImageView
    }
}