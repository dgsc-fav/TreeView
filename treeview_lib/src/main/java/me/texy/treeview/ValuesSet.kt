package me.texy.treeview

/**
 * A wrapper class that contains an original [value] and has a data for every parent categories
 */
interface ValuesSet<V, C> {
    /**
     * Returns a data that define a parent category
     */
    fun getValueForCategory(category: Category<C>): C?

    /**
     * An original value
     */
    val value: V
}