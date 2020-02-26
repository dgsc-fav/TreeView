package me.texy.treeview

interface CategoriesHolder<C> {
    val categoriesByPriority: Array<Category<C>>
}