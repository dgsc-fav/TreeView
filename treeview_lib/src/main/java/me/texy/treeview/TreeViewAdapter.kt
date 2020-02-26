/*
 * Copyright 2016 - 2017 ShineM (Xinyuan)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under.
 */
package me.texy.treeview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Checkable
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import me.texy.treeview.base.BaseNodeViewBinder
import me.texy.treeview.base.BaseNodeViewFactory
import me.texy.treeview.base.CheckableNodeViewBinder
import me.texy.treeview.helper.TreeHelper.collapseNode
import me.texy.treeview.helper.TreeHelper.expandNode
import me.texy.treeview.helper.TreeHelper.getAllNodes
import me.texy.treeview.helper.TreeHelper.selectNodeAndChild
import me.texy.treeview.helper.TreeHelper.selectParentIfNeedWhenNodeSelected
import java.util.ArrayList

/**
 * Created by xinyuanzhong on 2017/4/21.
 */
class TreeViewAdapter<V, C> internal constructor(
        private val context: Context,
        private val treeView: TreeView<V, C>,
        private val root: TreeNode<V, C>,
        private val baseNodeViewFactory: BaseNodeViewFactory<V, C>,
        private val showEmptyNode: Boolean = false,
        private val onTreeNodeClickListener: OnTreeNodeClickListener<V, C>? = null) : RecyclerView.Adapter<ViewHolder>() {

    private val expandedNodeList: MutableList<TreeNode<V, C>> = ArrayList()

    interface OnTreeNodeClickListener<V, C> {
        fun onTreeNodeClick(treeNode: TreeNode<V, C>)
    }

    init {
        buildExpandedNodeList()
    }

    private fun buildExpandedNodeList() {
        expandedNodeList.clear()
        for (child in root.getChildren()) {
            insertNode(expandedNodeList, child)
        }
    }

    private fun insertNode(nodeList: MutableList<TreeNode<V, C>>?, treeNode: TreeNode<V, C>) {
        if (showEmptyNode) {
            nodeList!!.add(treeNode)
        }
        if (treeNode.isGroup && !treeNode.hasChild()) {
            return
        }
        if (!showEmptyNode) {
            nodeList!!.add(treeNode)
        }
        if (treeNode.isExpanded) {
            for (child in treeNode.getChildren()) {
                insertNode(nodeList, child)
            }
        }
    }

    override fun getItemViewType(position: Int): Int { // return expandedNodeList.get(position).getLevel(); // this old code row used to always return the level
        val treeNode = expandedNodeList[position]
        return baseNodeViewFactory.getViewType(treeNode)
    }

    override fun onCreateViewHolder(parent: ViewGroup, level: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(baseNodeViewFactory.getNodeLayoutId(level), parent, false)
        val nodeViewBinder = baseNodeViewFactory.getNodeViewBinder(view, level)
        nodeViewBinder!!.treeView = treeView
        return nodeViewBinder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val nodeView = holder.itemView
        val treeNode = expandedNodeList[position]
        val viewBinder: BaseNodeViewBinder<V, C> = holder as BaseNodeViewBinder<V, C>
        if (viewBinder.toggleTriggerViewId != 0) {
            val triggerToggleView = nodeView.findViewById<View>(viewBinder.toggleTriggerViewId)
            triggerToggleView?.setOnClickListener {
                onNodeToggled(treeNode)
                viewBinder.onNodeToggled(treeNode, treeNode.isExpanded)
            }
        } else if (treeNode.isItemClickEnable) {
            if (treeNode.isGroup) {
                nodeView.setOnClickListener {
                    onNodeToggled(treeNode)
                    viewBinder.onNodeToggled(treeNode, treeNode.isExpanded)
                }
            } else {
                nodeView.setOnClickListener {
                    onTreeNodeClickListener?.onTreeNodeClick(treeNode)
                }
            }
        }
        if (viewBinder is CheckableNodeViewBinder<V, C>) {
            setupCheckableItem(nodeView, treeNode, viewBinder)
        }
        viewBinder.bindView(treeNode)
    }

    private fun setupCheckableItem(nodeView: View,
            treeNode: TreeNode<V, C>,
            viewBinder: CheckableNodeViewBinder<V, C>) {

        val view = nodeView.findViewById<View>(viewBinder.checkableViewId)
        if (view is Checkable) {
            val checkableView = view as Checkable
            checkableView.isChecked = treeNode.isSelected
            view.setOnClickListener {
                val checked = checkableView.isChecked
                selectNode(checked, treeNode)
                viewBinder.onNodeSelectedChanged(treeNode, checked)
            }
        } else {
            throw ClassCastException("The getCheckableViewId() " +
                    "must return a CheckBox's id")
        }
    }

    fun selectNode(checked: Boolean, treeNode: TreeNode<V, C>) {
        treeNode.isSelected = checked
        selectChildren(treeNode, checked)
        selectParentIfNeed(treeNode, checked)
    }

    private fun selectChildren(treeNode: TreeNode<V, C>, checked: Boolean) {
        val impactedChildren = selectNodeAndChild(treeNode, checked)
        val index = expandedNodeList.indexOf(treeNode)
        if (index != -1 && impactedChildren.isNotEmpty()) {
            notifyItemRangeChanged(index, impactedChildren.size + 1)
        }
    }

    private fun selectParentIfNeed(treeNode: TreeNode<V, C>, checked: Boolean) {
        val impactedParents = selectParentIfNeedWhenNodeSelected(treeNode, checked)
        if (impactedParents.isNotEmpty()) {
            for (parent in impactedParents) {
                val position = expandedNodeList.indexOf(parent)
                if (position != -1) notifyItemChanged(position)
            }
        }
    }

    private fun onNodeToggled(treeNode: TreeNode<V, C>) {
        treeNode.isExpanded = !treeNode.isExpanded
        if (treeNode.isExpanded) {
            expandNode(treeNode)
        } else {
            collapseNode(treeNode)
        }
    }

    override fun getItemCount(): Int {
        return expandedNodeList.size
    }

    /**
     * Refresh all,this operation is only used for refreshing list when a large of nodes have
     * changed value or structure because it take much calculation.
     */
    fun refreshView() {
        buildExpandedNodeList()
        notifyDataSetChanged()
    }

    // Insert a node list after index.
    private fun insertNodesAtIndex(index: Int, additionNodes: List<TreeNode<V, C>>?) {
        if (index < 0 || index > expandedNodeList.size - 1 || additionNodes == null) {
            return
        }
        expandedNodeList.addAll(index + 1, additionNodes)
        notifyItemRangeInserted(index + 1, additionNodes.size)
    }

    //Remove a node list after index.
    private fun removeNodesAtIndex(index: Int, removedNodes: List<TreeNode<V, C>>?) {
        if (index < 0 || index > expandedNodeList.size - 1 || removedNodes == null) {
            return
        }
        expandedNodeList.removeAll(removedNodes)
        notifyItemRangeRemoved(index + 1, removedNodes.size)
    }

    /**
     * Expand node. This operation will keep the structure of children(not expand children)
     */
    fun expandNode(treeNode: TreeNode<V, C>?) {
        if (treeNode == null) {
            return
        }
        val additionNodes = expandNode(treeNode, false)
        val index = expandedNodeList.indexOf(treeNode)
        insertNodesAtIndex(index, additionNodes)
    }

    /**
     * Collapse node. This operation will keep the structure of children(not collapse children)
     */
    fun collapseNode(treeNode: TreeNode<V, C>?) {
        if (treeNode == null) {
            return
        }
        val removedNodes = collapseNode(treeNode, false)
        val index = expandedNodeList.indexOf(treeNode)
        removeNodesAtIndex(index, removedNodes)
    }

    /**
     * Delete a node from list.This operation will also delete its children.
     */
    fun deleteNode(node: TreeNode<V, C>?) {
        if (node?.parent == null) {
            return
        }
        val allNodes = getAllNodes(root)
        if (allNodes.indexOf(node) != -1) {
            node.parent!!.removeChild(node)
        }
        //remove children form list before delete
        collapseNode(node)
        val index = expandedNodeList.indexOf(node)
        if (index != -1) {
            expandedNodeList.remove(node)
        }
        notifyItemRemoved(index)
    }
}