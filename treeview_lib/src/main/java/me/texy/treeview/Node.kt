package me.texy.treeview

import java.util.*

/**
 *  A problem:
 * There is a plain list of items. Each of items may belong to some [Category]ies.
 * Each [Category] has a particular priority [Category.level] which not equals to the others priorities.
 * The priorities are a sequence of positive integer values - levels, where 0 is the highest level.
 *  Need:
 * Create a tree of categories which are present in the list.
 * Children of tree's vertex may be as a child category so an item.
 *
 *  Solution:
 * One of suitable approaches: Imagine that we work with a filesystem. Then every category is a folder
 * and items are files in this folders. Some files in a current directory and other in a child folder and etc.
 * So when we add a file (item) through the root folder (root category) we define appropriate folder and create it if it does not exists then add the file(item) into it.
 */

/**
 *  ROOT
 *  GROUP...
 *  GROUP...
 *  ITEM...
 */

//interface Category<V> : Comparable<Category<V>> {
//    val level: Int
//    fun isChild(item: V)
//
//    override fun compareTo(other: Category<V>): Int {
//        return level.compareTo(other.level)
//    }
//
//    fun asKey(): Any
//}
//
//interface ValuesSet<V, C> {
//    fun getValueForCategory(category: Category<C>): C?
//}
//
//interface CategoriesHolder<C> {
//    val categoriesByPriority: Array<Category<C>>
//}

open class Node<V, C>(
        // a parent item for this item. may be null if it is a root item
        var parent: Node<V, C>? = null,
        // a leaf value. is null for a group
        var valuesSet: ValuesSet<V, C>? = null
) {

    companion object {
        fun <V, C> getRoot(): Node<V, C> = Node()
    }

    var category: Category<C>? = null
    var categoryValue: C? = null

    var childCategory: Category<C>? = null
    // list of children. may be null if no children presented for a category (? this might not been happened) or for an item
    var children: MutableList<Node<V, C>>? = null

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



    private fun insertValueSet(valuesSet: ValuesSet<V, C>, parent: Node<V, C>): Node<V, C> {
        if (parent.children == null) {
            parent.children = LinkedList()
        }

        val leaf = Node<V, C>().apply {
            this.parent = parent
            this.valuesSet = valuesSet
        }

        parent.children!!.add(leaf)
        return leaf
    }

    fun add(valuesSet: ValuesSet<V, C>, categoriesHolder: CategoriesHolder<C>): Node<V, C>? {

        println("add item=$valuesSet")

        // as mentioned above, we use the filesystem behaviour - firstly make all the folders next add a file to the last folder
        // from highest priority category to the lowest

        if (this.parent != null) throw IllegalAccessException("This is not a root")

        var currNode: Node<V, C> = this

        for (idx in categoriesHolder.categoriesByPriority.indices) {

            val currentCategory = categoriesHolder.categoriesByPriority[idx]
            val nextCategory = categoriesHolder.categoriesByPriority.getOrNull(idx + 1)

            // get child from parentGroup.children with specified value
            val node = getOrCreateNode(currNode, currentCategory, nextCategory, valuesSet)

            println("getOrCreateGroup result: group=$node")

            if (node != null) {
                // next search will from returned node
                currNode = node

                if (idx == categoriesHolder.categoriesByPriority.size - 1) {
                    return insertValueSet(valuesSet, currNode)
                }
            } else {
                // this item does not contains a data for the highest category
                if (currNode.isRoot()) {
                    println("SKIP THIS ITEM")
                    return null
                } else {
                    return insertValueSet(valuesSet, currNode)
                }
            }
        }

        // Nothing was added
        return null
    }

    private fun isRoot(): Boolean {
        return parent == null
    }

    /**
     * Returns a node which must contains an item or null if the category is not specified in the item
     */
    private fun getOrCreateNode(node: Node<V, C>, forCategory: Category<C>, nextCategory: Category<C>?, item: ValuesSet<V, C>): Node<V, C>? {
        println("getOrCreateGroup($node) item: $item")

        //
        if (node.childCategory == null) {
            node.childCategory = forCategory
        }
        if (node.children == null) {
            node.children = LinkedList()
        }

        val valueForCategory = item.getValueForCategory(forCategory)
        println("valueForCategory($forCategory) is $valueForCategory")

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
                childNodeByCategoryValue = Node<V, C>().apply {
                    parent = node
                    category = node.childCategory
                    categoryValue = valueForCategory
                    childCategory = nextCategory
                    children = LinkedList()
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
    fun getChildNodeByCategoryValue(category: Category<C>, valueForCategory: C): Node<V, C>? {
        println("getGroupByCategory: $valueForCategory")

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

    fun toMap() : Any? {
        val map = mutableMapOf<Any, Any>()

        val keyCategory = category?.asKey()?.toString()
        val keyCategoryValue = categoryValue?.toString()


        val childrenList = children?.map { it.toMap() }

        val key = keyCategoryValue

        if (key == null) {
            if (childrenList != null) {
                return childrenList
            } else if (valuesSet != null) {
                return valuesSet!!
            }
        } else {
             keyCategory?.let { map["type"] = it }

            if (childrenList != null) {
                map[key] = childrenList
            } else if (valuesSet != null) {
                map[key] = valuesSet!!
            }
        }

        return map
    }
}