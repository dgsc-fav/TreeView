package me.texy.treeviewdemo

import com.google.gson.Gson
import me.texy.treeview.CategoriesHolder
import me.texy.treeview.Category
import me.texy.treeview.TreeNode
import me.texy.treeview.ValuesSet

object FakeDataGenerator {
    class ValuesSetImpl(val categoryTimeValue: String?, val categorySeverityValue: String?, override val value: String = "value") : ValuesSet<String, String> {

        override fun getValueForCategory(category: Category<String>): String? {
            //println("getValueForCategory: $category")

            return when (category) {
                is CategoryTime -> categoryTimeValue
                is CategorySeverity -> categorySeverityValue
                else -> null
            }
        }

        override fun toString(): String {
            return "VS($categoryTimeValue, $categorySeverityValue, $value)"
        }
    }

    class CategoryTime(override val level: Int = 0) : Category<String> {
        override fun isChild(item: String) {
            println("isChild $item?")
        }

        override fun toString(): String {
            return "CategoryTime"
        }

        override fun asKey(): Any {
            return toString()
        }

        override val pinned: Boolean = true
    }

    class CategorySeverity(override val level: Int = 1) : Category<String> {
        override fun isChild(item: String) {
            println("isChild $item?")
        }

        override fun toString(): String {
            return "CategorySeverity"
        }

        override fun asKey(): Any {
            return toString()
        }

        override val pinned: Boolean = false
    }

    fun printDFS(root: TreeNode<String, String>) {

        val categoriesHolder = object : CategoriesHolder<String> {
            override val categoriesByPriority: Array<Category<String>> = arrayOf(CategoryTime(), CategorySeverity())
        }

        val item_null_high = ValuesSetImpl(null, "high", "item_null_high")
        val item_0_high = ValuesSetImpl("123", "high", "item_123_high")
        val item_0_low = ValuesSetImpl("123", "low", "item_123_low")
        val item_1_high = ValuesSetImpl("456", "high", "item_456_high")
        val item_1_low = ValuesSetImpl("456", "low", "item_456_low")

        val item_2_null = ValuesSetImpl("456", null, "item_456_null")

        root.add(item_null_high, categoriesHolder)
        root.add(item_0_high, categoriesHolder)
        root.add(item_0_low, categoriesHolder)
        root.add(item_1_high, categoriesHolder)
        root.add(item_1_low, categoriesHolder)
        root.add(item_1_low, categoriesHolder)
        root.add(item_2_null, categoriesHolder)

        val map2 = root.toMap()
        val json2 = Gson().toJson(map2)
        println(json2)

    }

}