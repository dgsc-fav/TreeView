package me.texy.treeview

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TreeNodeTest {
    class ValuesSetImpl(val categoryTimeValue: String?, val categorySeverityValue: String?, override val value: String = "value") : ValuesSet<String, String> {

        override fun getValueForCategory(category: Category<String>): String? {
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

        override fun toString(): String {
            return "CategoryTime"
        }

        override fun asKey(): Any {
            return toString()
        }

        override val pinned: Boolean = true
    }

    class CategorySeverity(override val level: Int = 1) : Category<String> {

        override fun toString(): String {
            return "CategorySeverity"
        }

        override fun asKey(): Any {
            return toString()
        }

        override val pinned: Boolean = false
    }

    class CategoryValues(override val level: Int = 2) : Category<String> {
        override fun toString(): String {
            return "CategoryValues"
        }

        override fun asKey(): Any {
            return toString()
        }

        override val pinned: Boolean = false
    }

    @Test
    fun testAddToTree() {

        val categoriesHolder = object : CategoriesHolder<String> {
            override val categoriesByPriority: Array<Category<String>> = arrayOf(CategoryTime(), CategorySeverity(), CategoryValues())
        }

        val itemNullHigh = ValuesSetImpl(null, "high", "item_null_high")
        val item0High = ValuesSetImpl("123", "high", "item_123_high")
        val item0Low = ValuesSetImpl("123", "low", "item_123_low")
        val item1High = ValuesSetImpl("456", "high", "item_456_high")
        val item1Low = ValuesSetImpl("456", "low", "item_456_low")

        val item2Null = ValuesSetImpl("456", null, "item_456_null")

        val root: TreeNode<String, String> = TreeNode.root()

        assertNull(root.add(itemNullHigh, categoriesHolder))

        assertNotNull(root.add(item0High, categoriesHolder))
        assertNotNull(root.add(item0Low, categoriesHolder))
        assertNotNull(root.add(item1High, categoriesHolder))
        assertNotNull(root.add(item1Low, categoriesHolder))
        assertNotNull(root.add(item1Low, categoriesHolder))
        assertNotNull(root.add(item2Null, categoriesHolder))
    }
}