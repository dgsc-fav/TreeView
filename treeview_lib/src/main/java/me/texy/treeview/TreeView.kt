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
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import me.texy.treeview.base.BaseNodeViewFactory
import me.texy.treeview.base.SelectableTreeAction
import me.texy.treeview.helper.TreeHelper.collapseAll
import me.texy.treeview.helper.TreeHelper.collapseLevel
import me.texy.treeview.helper.TreeHelper.expandAll
import me.texy.treeview.helper.TreeHelper.expandLevel
import me.texy.treeview.helper.TreeHelper.getAllNodes
import me.texy.treeview.helper.TreeHelper.getSelectedNodes
import me.texy.treeview.helper.TreeHelper.selectNodeAndChild

/**
 * Created by xinyuanzhong on 2017/4/20.
 */
class TreeView<V, C>(
        private val context: Context,
        private val root: TreeNode<V, C>,
        private val baseNodeViewFactory: BaseNodeViewFactory<V, C>,
        private val showEmptyNode: Boolean = false,
        private val onTreeNodeClickListener: TreeViewAdapter.OnTreeNodeClickListener<V, C>? = null) : SelectableTreeAction<V, C> {

    private var rootView: RecyclerView? = null
    private var adapter: TreeViewAdapter<V, C>? = null

    var isItemSelectable = true

    val view: View
        get() {
            if (rootView == null) {
                rootView = buildRootView()
            }
            return rootView!!
        }

    private fun buildRootView(): RecyclerView {
        val recyclerView = RecyclerView(context)
        /**
         * disable multi touch event to prevent terrible data set error when calculate list.
         */
        recyclerView.isMotionEventSplittingEnabled = false
        (recyclerView.itemAnimator as SimpleItemAnimator?)!!.supportsChangeAnimations = false
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = TreeViewAdapter<V, C>(context, this, root, baseNodeViewFactory, showEmptyNode, onTreeNodeClickListener)
        recyclerView.adapter = adapter
        return recyclerView
    }

    override fun expandAll() {
        expandAll(root)
        refreshTreeView()
    }

    fun refreshTreeView() {
        (adapter as? TreeViewAdapter<V, C>)?.refreshView()
    }

    fun updateTreeView() {
        adapter?.notifyDataSetChanged()
    }

    override fun expandNode(treeNode: TreeNode<V, C>?) {
        adapter?.expandNode(treeNode)
    }

    override fun expandLevel(level: Int) {
        expandLevel(root, level)
        refreshTreeView()
    }

    override fun collapseAll() {
        collapseAll(root)
        refreshTreeView()
    }

    override fun collapseNode(treeNode: TreeNode<V, C>?) {
        adapter?.collapseNode(treeNode)
    }

    override fun collapseLevel(level: Int) {
        collapseLevel(root, level)
        refreshTreeView()
    }

    override fun toggleNode(treeNode: TreeNode<V, C>?) {
        if (treeNode != null) {
            if (treeNode.isExpanded) {
                collapseNode(treeNode)
            } else {
                expandNode(treeNode)
            }
        }
    }

    override fun deleteNode(node: TreeNode<V, C>?) {
        adapter?.deleteNode(node)
    }

    override fun addNode(parent: TreeNode<V, C>?, treeNode: TreeNode<V, C>?) {
        if (parent != null && treeNode != null) {
            parent.addChild(treeNode)
            refreshTreeView()
        }
    }

    override val allNodes: List<TreeNode<V, C>>
        get() = getAllNodes(root)

    override fun selectNode(treeNode: TreeNode<V, C>?) {
        if (treeNode != null) {
            adapter?.selectNode(true, treeNode)
        }
    }

    override fun deselectNode(treeNode: TreeNode<V, C>?) {
        if (treeNode != null) {
            adapter?.selectNode(false, treeNode)
        }
    }

    override fun selectAll() {
        selectNodeAndChild(root, true)
        refreshTreeView()
    }

    override fun deselectAll() {
        selectNodeAndChild(root, false)
        refreshTreeView()
    }

    override val selectedNodes: List<TreeNode<V, C>>
        get() = getSelectedNodes(root)

}