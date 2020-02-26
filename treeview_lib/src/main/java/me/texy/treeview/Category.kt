package me.texy.treeview

/**
 * Category is a level in a three
 */
interface Category<V> : Comparable<Category<V>> {
    val level: Int

    val pinned: Boolean

    override fun compareTo(other: Category<V>): Int {
        return level.compareTo(other.level)
    }

    fun asKey(): Any
}