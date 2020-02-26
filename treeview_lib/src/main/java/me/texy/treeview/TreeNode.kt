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

import me.texy.treeview.helper.TreeHelper
import java.util.ArrayList
import java.util.LinkedList

class TreeNode<V, C>(
        // a parent item for this item. may be null if it is a root item
        var parent: TreeNode<V, C>? = null,
        // a leaf value. is null for a group
        var valuesSet: ValuesSet<V, C>? = null
) {
    var category: Category<C>? = null
    var categoryValue: C? = null

    private var childCategory: Category<C>? = null
    // list of children. may be null if no children presented for a category (? this might not been happened) or for an item
    private var children: MutableList<TreeNode<V, C>>? = null

    val level: Int
        get() = category?.level ?: -1

    var index = 0
    var isExpanded = false
    var isSelected = false

    var isItemClickEnable = true

    fun addChild(treeNode: TreeNode<V, C>?) {
        if (treeNode == null) {
            return
        }
        children!!.add(treeNode)
        treeNode.index = getChildren().size
        treeNode.parent = this
    }

    fun removeChild(treeNode: TreeNode<V, C>?) {
        if (treeNode == null || getChildren().size < 1) {
            return
        }
        if (getChildren().indexOf(treeNode) != -1) {
            getChildren().remove(treeNode)
        }
    }

    val isLastChild: Boolean
        get() {
            if (parent == null) {
                return false
            }
            val children: List<TreeNode<V, C>> = parent!!.getChildren()
            return children.isNotEmpty() && children.indexOf(this) == children.size - 1
        }

    val isRoot: Boolean
        get() = parent == null

    fun getChildren(): MutableList<TreeNode<V, C>> {
        return children ?: LinkedList()
    }

    val selectedChildren: List<TreeNode<V, C>>
        get() {
            val selectedChildren: MutableList<TreeNode<V, C>> = ArrayList()
            for (child in getChildren()) {
                if (child.isSelected) {
                    selectedChildren.add(child)
                }
            }
            return selectedChildren
        }

    fun setChildren(children: List<TreeNode<V, C>?>?) {
        if (children == null) {
            return
        }
        this.children = LinkedList()
        for (child in children) {
            addChild(child)
        }
    }

    /**
     * Updating the list of children while maintaining the tree structure
     */
    fun updateChildren(children: MutableList<TreeNode<V, C>>?) {
        val expands: MutableList<Boolean> = LinkedList()
        val allNodesPre = TreeHelper.getAllNodes(this)
        for (node in allNodesPre) {
            expands.add(node.isExpanded)
        }
        this.children = children
        val allNodes = TreeHelper.getAllNodes(this)
        if (allNodes.size == expands.size) {
            for (i in allNodes.indices) {
                allNodes[i].isExpanded = expands[i]
            }
        }
    }

    var isGroup: Boolean = false

    fun hasChild(): Boolean {
        return !children.isNullOrEmpty()
    }

    val id: String
        get() = "$level,$index"

    companion object {
        @JvmStatic
        fun <V, C> root(): TreeNode<V, C> {
            return TreeNode<V, C>()
        }
    }

    override fun toString(): String {
        return if (valuesSet != null) {
            valuesSet.toString()
        } else {
            "\n${getIndent()}GI{$category($categoryValue), children=$children}"
        }
    }

    private fun getIndent(): String {
        val sb = StringBuilder()
        repeat(category?.level ?: 0) {
            sb.append(" ")
        }
        return sb.toString()
    }

    fun add(valuesSet: ValuesSet<V, C>, categoriesHolder: CategoriesHolder<C>): TreeNode<V, C>? {

        // as mentioned above, we use the filesystem behaviour - firstly make all the folders next add a file to the last folder
        // from highest priority category to the lowest

        if (!isRoot) throw IllegalAccessException("This is not a root")

        var currNode: TreeNode<V, C> = this

        for (idx in categoriesHolder.categoriesByPriority.indices) {

            val currentCategory = categoriesHolder.categoriesByPriority[idx]
            val nextCategory = categoriesHolder.categoriesByPriority.getOrNull(idx + 1)

            // get child from parentGroup.children with specified value
            val node = getOrCreateNode(currNode, currentCategory, nextCategory, valuesSet)

            if (node != null) {
                // next search will from returned node
                currNode = node

                if (idx == categoriesHolder.categoriesByPriority.size - 1) {
                    return insertValueSet(valuesSet, currNode)
                }
            } else {
                // this item does not contains a data for the highest category
                return if (currNode.isRoot) {
                    println("Skip $valuesSet item as an item which has no root parent")
                    null
                } else {
                    insertValueSet(valuesSet, currNode)
                }
            }
        }

        // Nothing was added
        return null
    }

    /**
     * Returns a node which must contains an item or null if the category is not specified in the item
     */
    private fun getOrCreateNode(node: TreeNode<V, C>, forCategory: Category<C>, nextCategory: Category<C>?, item: ValuesSet<V, C>): TreeNode<V, C>? {

        //
        if (node.childCategory == null) {
            node.childCategory = forCategory
        }
        if (node.children == null) {
            node.children = LinkedList()
        }

        val valueForCategory = item.getValueForCategory(forCategory)

        if (valueForCategory == null) {
            // the item has no data for the category
            return null
        } else {
            // here we got a value from the item for the current category
            // let look for it by climbing down

            // get child with category and its value
            var childNodeByCategoryValue = node.getChildNodeByCategoryValue(forCategory, valueForCategory)

            if (childNodeByCategoryValue == null) {
                // node not found. do make it
                childNodeByCategoryValue = TreeNode<V, C>().apply {
                    parent = node
                    category = node.childCategory!!
                    categoryValue = valueForCategory
//                    childCategory = nextCategory
//                    children = LinkedList()
                    isGroup = true

                    if (forCategory.pinned) {
                        isExpanded = true
                        isItemClickEnable = false
                    }
                }

                // add to the tree
                node.children!!.add(childNodeByCategoryValue)
            }

            return childNodeByCategoryValue
        }
    }

    /**
     * Only get from current level. Does not create anything
     */
    private fun getChildNodeByCategoryValue(category: Category<C>, valueForCategory: C): TreeNode<V, C>? {

        if (this.children == null) {
            // no initialized yet. no children
            return null
        }

        if (this.childCategory!!.level == category.level) {
            // we in the category we are looking for
            return children!!.firstOrNull { it.categoryValue == valueForCategory }
        } else {
            // this is error state
            throw IllegalAccessException("wrong level: ${this.childCategory?.level} != ${category.level}")
        }
    }

    /**
     * Insert a new child node
     */
    private fun insertValueSet(valuesSet: ValuesSet<V, C>, parent: TreeNode<V, C>): TreeNode<V, C> {
//        if (parent.children == null) {
//            parent.children = LinkedList()
//        }

        val leaf = TreeNode<V, C>().apply {
            this.category = parent.childCategory!!
            this.parent = parent
            this.valuesSet = valuesSet
        }

        parent.children!!.add(leaf)
        return leaf
    }

    fun toMap(): Any? {
        val map = mutableMapOf<Any, Any>()

        val keyCategory = category?.asKey()?.toString()
        val keyCategoryValue = categoryValue?.toString()

        val childrenList = children?.map { it.toMap() }

        val key = keyCategoryValue

        if (key != null) {
            keyCategory?.let { map["type"] = it }

            if (childrenList != null) {
                map[key] = childrenList
            } else if (valuesSet != null) {
                map[key] = valuesSet!!
            }
        } else {
            if (childrenList != null) {
                return childrenList
            } else if (valuesSet != null) {
                return valuesSet!!
            }
        }

        return map
    }

}