package me.texy.treeview;

import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import me.texy.treeview.treeview.base.CheckableNodeViewBinder;
import me.texy.treeview.treeview.TreeNode;

/**
 * Created by zxy on 17/4/23.
 */

public class FirstLevelNodeViewBinder extends CheckableNodeViewBinder {

    public FirstLevelNodeViewBinder(View itemView) {
        super(itemView);
    }

    @Override
    public int getCheckableViewId() {
        return R.id.checkBox;
    }

    @Override
    public int getLayoutId(int level) {
        return R.layout.item_first_level;
    }

    @Override
    public void bindView(View view, final TreeNode treeNode) {
        TextView textView = (TextView) view.findViewById(R.id.node_name_view);
        ImageView imageView = (ImageView) view.findViewById(R.id.arrow_img);

        textView.setText(treeNode.getValue().toString());
        imageView.setRotation(treeNode.isExpanded() ? 90 : 0);
    }

    @Override
    public void onNodeToggled(View item, TreeNode treeNode, boolean expand) {
        ImageView imageView = (ImageView) item.findViewById(R.id.arrow_img);
        if (expand) {
            imageView.animate().rotation(90).setDuration(200).start();
        } else {
            imageView.animate().rotation(0).setDuration(200).start();
        }
    }
}