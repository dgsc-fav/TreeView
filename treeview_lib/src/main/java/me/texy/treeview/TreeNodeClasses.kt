package me.texy.treeview

interface Category<V> : Comparable<Category<V>> {
    val level: Int

    val pinned: Boolean

    fun isChild(item: V)

    override fun compareTo(other: Category<V>): Int {
        return level.compareTo(other.level)
    }

    fun asKey(): Any
}

interface ValuesSet<V, C> {
    fun getValueForCategory(category: Category<C>): C?

    val value: V
}

interface CategoriesHolder<C> {
    val categoriesByPriority: Array<Category<C>>
}