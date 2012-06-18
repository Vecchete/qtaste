/*
    Copyright 2007-2009 QSpin - www.qspin.be

    This file is part of QTaste framework.

    QTaste is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    QTaste is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with QTaste. If not, see <http://www.gnu.org/licenses/>.
*/

/*

*/
package com.qspin.qtaste.ui;

import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.qspin.qtaste.config.TestBedConfiguration;
import com.qspin.qtaste.kernel.testapi.ComponentFactory;
import com.qspin.qtaste.kernel.testapi.ComponentsLoader;
import com.qspin.qtaste.kernel.testapi.MultipleInstancesComponentFactory;
import com.qspin.qtaste.kernel.testapi.SingletonComponentFactory;
import com.qspin.qtaste.kernel.testapi.TestAPI;
import com.qspin.qtaste.kernel.testapi.TestAPIImpl;
import com.qspin.qtaste.ui.testcasebuilder.TestDesignPanels;


@SuppressWarnings("serial")
public class TestAPIDocsTree extends JTree implements DragSourceListener, DragGestureListener {

    private DragSource dragSourceObject;
    private DropTarget dropTargetObject;
    private StringSelection transferable;
    TestAPIPanel mLinkedpanel;
    
    public TestAPIDocsTree(TestAPIPanel linkedPanel) {
        super();
        final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("API verbs");
        buildTree(rootNode, this);

        final DefaultTreeModel tm = new DefaultTreeModel(rootNode);
        setModel(tm);
        linkedPanel.setTestCaseTree(this);
        mLinkedpanel= linkedPanel;
        TreeSelectionModel selModel = this.getSelectionModel();
        selModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        TestBedConfiguration.registerConfigurationChangeHandler(new TestBedConfiguration.ConfigurationChangeHandler() {

            public void onConfigurationChange() {
                TestAPIDocsTree.this.buildTree(rootNode, TestAPIDocsTree.this);
                tm.reload();
            }
        });

    }

    public void initDragDropOperations()
    {
        if (mLinkedpanel instanceof TestDesignPanels)
        {
            dropTargetObject = new DropTarget();
            dragSourceObject = new DragSource();
            TestDesignPanels designPanel = (TestDesignPanels)mLinkedpanel;
            dragSourceObject.createDefaultDragGestureRecognizer(this, 
                    DnDConstants.ACTION_COPY, this);
            try
            {
                dropTargetObject.setComponent(designPanel.getDesignPanel().getSourceTextArea());
                dropTargetObject.addDropTargetListener(designPanel.getDesignPanel());
            }
            catch (java.util.TooManyListenersException e)
            {
                // to be completed but what ???
            }
                    
        }
    }

    
    private void buildTree(final DefaultMutableTreeNode rootNode, TestAPIDocsTree tree) {
        Thread t = new Thread() {
            public void run() {
                TestAPI testAPI = TestAPIImpl.getInstance();
                rootNode.removeAllChildren();
                ComponentsLoader.getInstance(); // don't remove, it is to be sure that components are registered
                Collection<String> hashComponents = testAPI.getRegisteredComponents();
                TreeSet<String> sortedComponents = new TreeSet<String>(hashComponents);
                TestBedConfiguration testbedConfig = TestBedConfiguration.getInstance();
                for (String componentName: sortedComponents) {
                    boolean componentPresentInTestbed = true;
                    ComponentFactory componentFactory = testAPI.getComponentFactory(componentName);
                    if (componentFactory instanceof SingletonComponentFactory) {
                        componentPresentInTestbed = !testbedConfig.configurationsAt("singleton_components." + componentName).isEmpty();
                    } else if (componentFactory instanceof MultipleInstancesComponentFactory) {
                        componentPresentInTestbed = !testbedConfig.configurationsAt("multiple_instances_components." + componentName).isEmpty();
                    }
                    if (componentPresentInTestbed) {
                        DefaultMutableTreeNode node = new DefaultMutableTreeNode(componentName, true);
                        rootNode.add(node);
                        // get all methods from this component
                        List<String> methods = new ArrayList<String>(testAPI.getRegisteredVerbs(componentName));
                        Collections.sort(methods);
                        for (String methodName: methods) {
                            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(methodName, true);
                            node.add(childNode);
                        }
                    }
                }
            }
        };
        t.start();
        while (t.isAlive()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }

        TreePath tp = new TreePath(rootNode.getPath());
        tree.expandPath(tp);
    }

    public class TestAPITreeListener implements TreeWillExpandListener, TreeSelectionListener {

        protected TestAPIDocsTree mTree;

        public TestAPITreeListener (TestAPIDocsTree tree) {
            this.mTree = tree;
        }

        public void treeWillCollapse(TreeExpansionEvent event) {
        }

        public void treeWillExpand(TreeExpansionEvent event) {
            TreePath path = event.getPath();
            if ( path.getParentPath() == null ) {
                DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)path.getLastPathComponent();
                buildTree(rootNode, mTree);
            }
        }

        public void valueChanged(TreeSelectionEvent e) {
            {
                //TreePath path = e.getPath();
                //DefaultMutableTreeNode tn = (DefaultMutableTreeNode) path.getLastPathComponent();
                //Object obj = tn.getUserObject();
            }
        }
    }

    public void dragEnter(DragSourceDragEvent dsde) {
           System.out.println("Drag enter");    
    }

    public void dragOver(DragSourceDragEvent dsde) {
           System.out.println("Drag over");    
        
    }

    public void dropActionChanged(DragSourceDragEvent dsde) {
           System.out.println("Drag action changed");    
    }

    public void dragExit(DragSourceEvent dse) {
           System.out.println("Drag exit");    
    }

    public void dragDropEnd(DragSourceDropEvent dsde) {
        
        if (dsde.getDropSuccess())
           System.out.println("Drag Drop end successfully");    
        else
           System.out.println("Drag Drop end failed");    
    }

    public void dragGestureRecognized(DragGestureEvent dge) {
        System.out.println("Drag gesture recognized");
        TreePath path = this.getLeadSelectionPath();
        if (path != null ) {
            if (path.getParentPath()!=null)
            {
                DefaultMutableTreeNode tn = (DefaultMutableTreeNode)path.getLastPathComponent();
                transferable = new StringSelection((String)tn.getUserObject());
                dragSourceObject.startDrag(dge, DragSource.DefaultCopyDrop, transferable, this);
            }
        }
    }
  }

