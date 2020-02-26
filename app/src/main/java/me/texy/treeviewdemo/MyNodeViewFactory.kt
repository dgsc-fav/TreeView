package me.texy.treeviewdemo

import android.view.View
import me.texy.treeview.base.BaseNodeViewBinder
import me.texy.treeview.base.BaseNodeViewFactory

/**
 * Created by zxy on 17/4/23.
 */
class MyNodeViewFactory : BaseNodeViewFactory<String, String>() {

    override fun getNodeViewBinder(view: View, level: Int): BaseNodeViewBinder<String, String>? {
        return when (level) {
            0 -> FirstLevelNodeViewBinder(view)
            1 -> SecondLevelNodeViewBinder(view)
            2 -> ThirdLevelNodeViewBinder(view)
            else -> null
        }
    }

    override fun getNodeLayoutId(level: Int): Int {
        return when (level) {
            0 -> R.layout.item_first_level
            1 -> R.layout.item_second_level
            2 -> R.layout.item_third_level
            else -> R.layout.item_first_level
        }
    }
}