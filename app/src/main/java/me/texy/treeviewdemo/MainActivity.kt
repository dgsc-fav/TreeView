package me.texy.treeviewdemo

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import me.texy.treeview.TreeNode
import me.texy.treeview.TreeNode.Companion.root
import me.texy.treeview.TreeView
import me.texy.treeviewdemo.FakeDataGenerator.buildTree

/**
 * https://github.com/shineM/TreeView
 */
class MainActivity : AppCompatActivity() {

    protected var toolbar: Toolbar? = null
    private var viewGroup: ViewGroup? = null
    private var root: TreeNode<String, String>? = null
    private var treeView: TreeView<String, String>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.activity_main)
        initView()
        root = root()
        buildTree()
        treeView = TreeView(root, this, MyNodeViewFactory())
        val view = treeView!!.view
        view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        viewGroup!!.addView(view)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.select_all -> treeView!!.selectAll()
            R.id.deselect_all -> treeView!!.deselectAll()
            R.id.expand_all -> treeView!!.expandAll()
            R.id.collapse_all -> treeView!!.collapseAll()
            R.id.expand_level -> treeView!!.expandLevel(1)
            R.id.collapse_level -> treeView!!.collapseLevel(1)
            R.id.show_select_node -> Toast.makeText(application, selectedNodes, Toast.LENGTH_LONG).show()
        }
        return super.onOptionsItemSelected(item)
    }

    private val selectedNodes: String
        private get() {
            val stringBuilder = StringBuilder("You have selected: ")
            val selectedNodes = treeView!!.selectedNodes
            for (i in selectedNodes.indices) {
                if (i < 5) {
                    stringBuilder.append(selectedNodes[i].valuesSet.toString() + ",")
                } else {
                    stringBuilder.append("...and " + (selectedNodes.size - 5) + " more.")
                    break
                }
            }
            return stringBuilder.toString()
        }

    private fun buildTree() {
        buildTree(root!!)
    }

    private fun setLightStatusBar(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var flags = view.systemUiVisibility
            window.statusBarColor = Color.WHITE
            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            view.systemUiVisibility = flags
        }
    }

    private fun initView() {
        toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        viewGroup = findViewById<View>(R.id.container) as RelativeLayout
        setSupportActionBar(toolbar)
        setLightStatusBar(viewGroup!!)
    }
}