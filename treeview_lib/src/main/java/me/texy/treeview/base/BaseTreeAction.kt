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
package me.texy.treeview.base

import me.texy.treeview.TreeNode

/**
 * Created by xinyuanzhong on 2017/4/20.
 */
interface BaseTreeAction<V, C> {
    fun expandAll()
    fun expandNode(treeNode: TreeNode<V, C>?)
    fun expandLevel(level: Int)
    fun collapseAll()
    fun collapseNode(treeNode: TreeNode<V, C>?)
    fun collapseLevel(level: Int)
    fun toggleNode(treeNode: TreeNode<V, C>?)
    fun deleteNode(node: TreeNode<V, C>?)
    fun addNode(parent: TreeNode<V, C>?, treeNode: TreeNode<V, C>?)
    // TODO: 17/4/30
    val allNodes: List<TreeNode<V, C>?>?

// 1.add node at position
// 2.add slide delete or other operations
}