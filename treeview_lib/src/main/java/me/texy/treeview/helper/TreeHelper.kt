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
package me.texy.treeview.helper

import me.texy.treeview.TreeNode
import java.util.ArrayList

/**
 * Created by xinyuanzhong on 2017/4/27.
 */
object TreeHelper {
    @JvmStatic
    fun expandAll(node: TreeNode<*, *>?) {
        if (node == null) {
            return
        }
        expandNode(node, true)
    }

    /**
     * Expand node and calculate the visible addition nodes.
     *
     * @param treeNode     target node to expand
     * @param includeChild should expand child
     * @return the visible addition nodes
     */
    @JvmStatic
    fun <V, C> expandNode(treeNode: TreeNode<V, C>?, includeChild: Boolean): List<TreeNode<V, C>> {
        val expandChildren: MutableList<TreeNode<V, C>> = ArrayList()
        if (treeNode == null) {
            return expandChildren
        }
        treeNode.isExpanded = true
        if (!treeNode.hasChild()) {
            return expandChildren
        }
        for (child in treeNode.getChildren()) {
            expandChildren.add(child)
            if (includeChild || child.isExpanded) {
                expandChildren.addAll(expandNode(child, includeChild))
            }
        }
        return expandChildren
    }

    /**
     * Expand the same deep(level) nodes.
     *
     * @param root  the tree root
     * @param level the level to expand
     */
    @JvmStatic
    fun <V, C> expandLevel(root: TreeNode<V, C>?, level: Int) {
        if (root == null) {
            return
        }
        for (child in root.getChildren()) {
            if (child.level == level) {
                expandNode(child, false)
            } else {
                expandLevel(child, level)
            }
        }
    }

    @JvmStatic
    fun <V, C> collapseAll(node: TreeNode<V, C>?) {
        if (node == null) {
            return
        }
        for (child in node.getChildren()) {
            performCollapseNode(child, true)
        }
    }

    /**
     * Collapse node and calculate the visible removed nodes.
     *
     * @param node         target node to collapse
     * @param includeChild should collapse child
     * @return the visible addition nodes before remove
     */
    @JvmStatic
    fun <V, C> collapseNode(node: TreeNode<V, C>, includeChild: Boolean): List<TreeNode<V, C>> {
        val treeNodes = performCollapseNode(node, includeChild)
        node.isExpanded = false
        return treeNodes
    }

    private fun <V, C> performCollapseNode(node: TreeNode<V, C>?, includeChild: Boolean): List<TreeNode<V, C>> {
        val collapseChildren: MutableList<TreeNode<V, C>> = ArrayList()
        if (node == null) {
            return collapseChildren
        }
        if (includeChild) {
            node.isExpanded = false
        }
        for (child in node.getChildren()) {
            collapseChildren.add(child)
            if (child.isExpanded) {
                collapseChildren.addAll(performCollapseNode(child, includeChild))
            } else if (includeChild) {
                performCollapseNodeInner(child)
            }
        }
        return collapseChildren
    }

    /**
     * Collapse all children node recursive
     *
     * @param node target node to collapse
     */
    private fun <V, C> performCollapseNodeInner(node: TreeNode<V, C>?) {
        if (node == null) {
            return
        }
        node.isExpanded = false
        for (child in node.getChildren()) {
            performCollapseNodeInner(child)
        }
    }

    @JvmStatic
    fun <V, C> collapseLevel(root: TreeNode<V, C>?, level: Int) {
        if (root == null) {
            return
        }
        for (child in root.getChildren()) {
            if (child.level == level) {
                collapseNode(child, false)
            } else {
                collapseLevel(child, level)
            }
        }
    }

    @JvmStatic
    fun <V, C> getAllNodes(root: TreeNode<V, C>): List<TreeNode<V, C>> {
        val allNodes: MutableList<TreeNode<V, C>> = ArrayList()
        fillNodeList(allNodes, root)
        allNodes.remove(root)
        return allNodes
    }

    private fun <V, C> fillNodeList(treeNodes: MutableList<TreeNode<V, C>>, treeNode: TreeNode<V, C>) {
        treeNodes.add(treeNode)
        if (treeNode.hasChild()) {
            for (child in treeNode.getChildren()) {
                fillNodeList(treeNodes, child)
            }
        }
    }

    /**
     * Select the node and node's children,return the visible nodes
     */
    @JvmStatic
    fun <V, C> selectNodeAndChild(treeNode: TreeNode<V, C>?, select: Boolean): List<TreeNode<V, C>> {
        val expandChildren: MutableList<TreeNode<V, C>> = ArrayList()
        if (treeNode == null) {
            return expandChildren
        }
        treeNode.isSelected = select
        if (!treeNode.hasChild()) {
            return expandChildren
        }
        if (treeNode.isExpanded) {
            for (child in treeNode.getChildren()) {
                expandChildren.add(child)
                if (child.isExpanded) {
                    expandChildren.addAll(selectNodeAndChild(child, select))
                } else {
                    selectNodeInner(child, select)
                }
            }
        } else {
            selectNodeInner(treeNode, select)
        }
        return expandChildren
    }

    private fun <V, C> selectNodeInner(treeNode: TreeNode<V, C>?, select: Boolean) {
        if (treeNode == null) {
            return
        }
        treeNode.isSelected = select
        if (treeNode.hasChild()) {
            for (child in treeNode.getChildren()) {
                selectNodeInner(child, select)
            }
        }
    }

    /**
     * Select parent when all the brothers have been selected, otherwise deselect parent,
     * and check the grand parent recursive.
     */
    @JvmStatic
    fun <V, C> selectParentIfNeedWhenNodeSelected(treeNode: TreeNode<V, C>?, select: Boolean): List<TreeNode<V, C>> {
        val impactedParents: MutableList<TreeNode<V, C>> = ArrayList()
        if (treeNode == null) {
            return impactedParents
        }
        //ensure that the node's level is bigger than 1(first level is 1)
        val parent = treeNode.parent
        if (parent == null || parent.parent == null) {
            return impactedParents
        }
        val brothers: List<TreeNode<V, C>> = parent.getChildren()
        var selectedBrotherCount = 0
        for (brother in brothers) {
            if (brother.isSelected) selectedBrotherCount++
        }
        if (select && selectedBrotherCount == brothers.size) {
            parent.isSelected = true
            impactedParents.add(parent)
            impactedParents.addAll(selectParentIfNeedWhenNodeSelected(parent, true))
        } else if (!select && selectedBrotherCount == brothers.size - 1) { // only the condition that the size of selected's brothers
// is one less than total count can trigger the deselect
            parent.isSelected = false
            impactedParents.add(parent)
            impactedParents.addAll(selectParentIfNeedWhenNodeSelected(parent, false))
        }
        return impactedParents
    }

    /**
     * Get the selected nodes under current node, include itself
     */
    @JvmStatic
    fun <V, C> getSelectedNodes(treeNode: TreeNode<V, C>?): List<TreeNode<V, C>> {
        val selectedNodes: MutableList<TreeNode<V, C>> = ArrayList()
        if (treeNode == null) {
            return selectedNodes
        }
        if (treeNode.isSelected && treeNode.parent != null) selectedNodes.add(treeNode)
        for (child in treeNode.getChildren()) {
            selectedNodes.addAll(getSelectedNodes(child))
        }
        return selectedNodes
    }

    /**
     * Return true when the node has one selected child(recurse all children) at least,
     * otherwise return false
     */
    fun <V, C> hasOneSelectedNodeAtLeast(treeNode: TreeNode<V, C>?): Boolean {
        if (treeNode == null || treeNode.getChildren().size == 0) {
            return false
        }
        val children: List<TreeNode<V, C>> = treeNode.getChildren()
        for (child in children) {
            if (child.isSelected || hasOneSelectedNodeAtLeast(child)) {
                return true
            }
        }
        return false
    }
}