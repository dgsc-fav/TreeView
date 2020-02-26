/*
 * Copyright 2016 - 2017 ShineM (Xinyuan)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under.
 */

package me.texy.treeview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import me.texy.treeview.base.BaseNodeViewBinder;
import me.texy.treeview.base.BaseNodeViewFactory;
import me.texy.treeview.base.CheckableNodeViewBinder;
import me.texy.treeview.helper.TreeHelper;

/**
 * Created by xinyuanzhong on 2017/4/21.
 */

public class TreeViewAdapter<V, C> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;

    private TreeNode<V, C> root;

    private List<TreeNode<V, C>> expandedNodeList;

    private BaseNodeViewFactory<V, C> baseNodeViewFactory;

    private TreeView treeView;

    TreeViewAdapter(Context context, TreeNode<V, C> root,
                    @NonNull BaseNodeViewFactory<V, C> baseNodeViewFactory) {
        this.context = context;
        this.root = root;
        this.baseNodeViewFactory = baseNodeViewFactory;

        this.expandedNodeList = new ArrayList<>();

        buildExpandedNodeList();
    }

    private void buildExpandedNodeList() {
        expandedNodeList.clear();

        for (TreeNode<V, C> child : root.getChildren()) {
            insertNode(expandedNodeList, child);
        }
    }

    boolean addEmptyNode = false;

    private void insertNode(List<TreeNode<V, C>> nodeList, TreeNode<V, C> treeNode) {

        if (addEmptyNode) {
            nodeList.add(treeNode);
        }

        if (treeNode.isGroup() && !treeNode.hasChild()) {
            return;
        }

        if (!addEmptyNode) {
            nodeList.add(treeNode);
        }

        if (treeNode.isExpanded()) {
            for (TreeNode<V, C> child : treeNode.getChildren()) {
                insertNode(nodeList, child);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        // return expandedNodeList.get(position).getLevel(); // this old code row used to always return the level
        TreeNode<V, C> treeNode = expandedNodeList.get(position);
        int viewType = this.baseNodeViewFactory.getViewType(treeNode); // default implementation returns the three node level but it can be overridden
        return viewType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int level) {
        View view = LayoutInflater.from(context).inflate(baseNodeViewFactory.getNodeLayoutId(level), parent, false);

        BaseNodeViewBinder nodeViewBinder = baseNodeViewFactory.getNodeViewBinder(view, level);
        nodeViewBinder.setTreeView(treeView);
        return nodeViewBinder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        final View nodeView = holder.itemView;
        final TreeNode<V, C> treeNode = expandedNodeList.get(position);
        final BaseNodeViewBinder<V, C> viewBinder = (BaseNodeViewBinder) holder;

        if (viewBinder.getToggleTriggerViewId() != 0) {
            View triggerToggleView = nodeView.findViewById(viewBinder.getToggleTriggerViewId());

            if (triggerToggleView != null) {
                triggerToggleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onNodeToggled(treeNode);
                        viewBinder.onNodeToggled(treeNode, treeNode.isExpanded());
                    }
                });
            }
        } else if (treeNode.isItemClickEnable()) {
            nodeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onNodeToggled(treeNode);
                    viewBinder.onNodeToggled(treeNode, treeNode.isExpanded());
                }
            });
        }

        if (viewBinder instanceof CheckableNodeViewBinder) {
            setupCheckableItem(nodeView, treeNode, (CheckableNodeViewBinder) viewBinder);
        }

        viewBinder.bindView(treeNode);
    }

    private void setupCheckableItem(View nodeView,
                                    final TreeNode<V, C> treeNode,
                                    final CheckableNodeViewBinder viewBinder) {
        final View view = nodeView.findViewById(viewBinder.getCheckableViewId());

        if (view instanceof Checkable) {
            final Checkable checkableView = (Checkable) view;
            checkableView.setChecked(treeNode.isSelected());

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean checked = checkableView.isChecked();
                    selectNode(checked, treeNode);
                    viewBinder.onNodeSelectedChanged(treeNode, checked);
                }
            });
        } else {
            throw new ClassCastException("The getCheckableViewId() " +
                    "must return a CheckBox's id");
        }
    }

    void selectNode(boolean checked, TreeNode<V, C> treeNode) {
        treeNode.setSelected(checked);

        selectChildren(treeNode, checked);
        selectParentIfNeed(treeNode, checked);
    }

    private void selectChildren(TreeNode<V, C> treeNode, boolean checked) {
        List<TreeNode<V, C>> impactedChildren = TreeHelper.selectNodeAndChild(treeNode, checked);
        int index = expandedNodeList.indexOf(treeNode);
        if (index != -1 && impactedChildren.size() > 0) {
            notifyItemRangeChanged(index, impactedChildren.size() + 1);
        }
    }

    private void selectParentIfNeed(TreeNode<V, C> treeNode, boolean checked) {
        List<TreeNode<V, C>> impactedParents = TreeHelper.selectParentIfNeedWhenNodeSelected(treeNode, checked);
        if (impactedParents.size() > 0) {
            for (TreeNode parent : impactedParents) {
                int position = expandedNodeList.indexOf(parent);
                if (position != -1) notifyItemChanged(position);
            }
        }
    }

    private void onNodeToggled(TreeNode<V, C> treeNode) {
        treeNode.setExpanded(!treeNode.isExpanded());

        if (treeNode.isExpanded()) {
            expandNode(treeNode);
        } else {
            collapseNode(treeNode);
        }
    }

    @Override
    public int getItemCount() {
        return expandedNodeList == null ? 0 : expandedNodeList.size();
    }

    /**
     * Refresh all,this operation is only used for refreshing list when a large of nodes have
     * changed value or structure because it take much calculation.
     */
    void refreshView() {
        buildExpandedNodeList();
        notifyDataSetChanged();
    }

    // Insert a node list after index.
    private void insertNodesAtIndex(int index, List<TreeNode<V, C>> additionNodes) {
        if (index < 0 || index > expandedNodeList.size() - 1 || additionNodes == null) {
            return;
        }
        expandedNodeList.addAll(index + 1, additionNodes);
        notifyItemRangeInserted(index + 1, additionNodes.size());
    }

    //Remove a node list after index.
    private void removeNodesAtIndex(int index, List<TreeNode<V, C>> removedNodes) {
        if (index < 0 || index > expandedNodeList.size() - 1 || removedNodes == null) {
            return;
        }
        expandedNodeList.removeAll(removedNodes);
        notifyItemRangeRemoved(index + 1, removedNodes.size());
    }

    /**
     * Expand node. This operation will keep the structure of children(not expand children)
     */
    void expandNode(TreeNode<V, C> treeNode) {
        if (treeNode == null) {
            return;
        }
        List<TreeNode<V, C>> additionNodes = TreeHelper.expandNode(treeNode, false);
        int index = expandedNodeList.indexOf(treeNode);

        insertNodesAtIndex(index, additionNodes);
    }


    /**
     * Collapse node. This operation will keep the structure of children(not collapse children)
     */
    void collapseNode(TreeNode<V, C> treeNode) {
        if (treeNode == null) {
            return;
        }
        List<TreeNode<V, C>> removedNodes = TreeHelper.collapseNode(treeNode, false);
        int index = expandedNodeList.indexOf(treeNode);

        removeNodesAtIndex(index, removedNodes);
    }

    /**
     * Delete a node from list.This operation will also delete its children.
     */
    void deleteNode(TreeNode<V, C> node) {
        if (node == null || node.getParent() == null) {
            return;
        }
        List<TreeNode<V, C>> allNodes = TreeHelper.getAllNodes(root);
        if (allNodes.indexOf(node) != -1) {
            node.getParent().removeChild(node);
        }

        //remove children form list before delete
        collapseNode(node);

        int index = expandedNodeList.indexOf(node);
        if (index != -1) {
            expandedNodeList.remove(node);
        }
        notifyItemRemoved(index);
    }

    void setTreeView(TreeView treeView) {
        this.treeView = treeView;
    }
}
