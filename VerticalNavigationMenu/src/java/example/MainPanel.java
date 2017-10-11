package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout(2, 2));
        Icon emptyIcon = new EmptyIcon();
        UIManager.put("Tree.openIcon",      emptyIcon);
        UIManager.put("Tree.closedIcon",    emptyIcon);
        UIManager.put("Tree.leafIcon",      emptyIcon);
        UIManager.put("Tree.expandedIcon",  emptyIcon);
        UIManager.put("Tree.collapsedIcon", emptyIcon);
        UIManager.put("Tree.leftChildIndent", 10);
        UIManager.put("Tree.rightChildIndent", 0);
        UIManager.put("Tree.paintLines", false);

        TreeModel model = makeModel();
        CardLayout cardLayout = new CardLayout();
        JPanel p = new JPanel(cardLayout);
        // http://ateraimemo.com/Swing/TraverseAllNodes.html
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        Enumeration en = root.postorderEnumeration();
        while (en.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
            String title = Objects.toString(node.getUserObject());
            p.add(new JLabel(title), title);
        }

        JTree tree = new RowSelectionTree();
        tree.setModel(model);
        tree.setRowHeight(32);
        tree.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        // http://ateraimemo.com/Swing/ExpandAllNodes.html
        int row = 0;
        while (row < tree.getRowCount()) {
            tree.expandRow(row++);
        }
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(e -> {
            // http://ateraimemo.com/Swing/CardLayoutTabbedPane.html
            Object o = e.getNewLeadSelectionPath().getLastPathComponent(); // tree.getLastSelectedPathComponent();
            if (o instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
                String title = Objects.toString(node.getUserObject());
                cardLayout.show(p, title);
            }
        });

        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        sp.setLeftComponent(new JScrollPane(tree));
        sp.setRightComponent(new JScrollPane(p));
        sp.setResizeWeight(.5);
        add(sp);
        setPreferredSize(new Dimension(320, 240));
    }
    private static DefaultTreeModel makeModel() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        DefaultMutableTreeNode c1 = new DefaultMutableTreeNode("1. Introduction");

        DefaultMutableTreeNode c2 = new DefaultMutableTreeNode("2. Chapter");
        c2.add(new DefaultMutableTreeNode("2.1. Section"));
        c2.add(new DefaultMutableTreeNode("2.2. Section"));
        c2.add(new DefaultMutableTreeNode("2.3. Section"));

        DefaultMutableTreeNode c3 = new DefaultMutableTreeNode("3. Chapter");
        c3.add(new DefaultMutableTreeNode("3.1. Section"));
        c3.add(new DefaultMutableTreeNode("3.2. Section"));
        c3.add(new DefaultMutableTreeNode("3.3. Section"));
        c3.add(new DefaultMutableTreeNode("3.4. Section"));

        root.add(c1);
        root.add(c2);
        root.add(c3);
        return new DefaultTreeModel(root);
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

// http://ateraimemo.com/Swing/TreeRowSelection.html
class RowSelectionTree extends JTree {
    public static final Color SELC = new Color(100, 150, 200);
    private transient TreeWillExpandListener listener;
    @Override protected void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(SELC);
        Arrays.stream(getSelectionRows()).forEach(i -> {
            Rectangle r = getRowBounds(i);
            g2.fillRect(0, r.y, getWidth(), r.height);
        });
        super.paintComponent(g);
        if (hasFocus()) {
            Optional.ofNullable(getLeadSelectionPath()).ifPresent(path -> {
                Rectangle r = getRowBounds(getRowForPath(path));
                g2.setPaint(SELC.darker());
                g2.drawRect(0, r.y, getWidth() - 1, r.height - 1);
            });
        }
        g2.dispose();
    }
    @Override public void updateUI() {
        setCellRenderer(null);
        removeTreeWillExpandListener(listener);
        super.updateUI();
        setUI(new BasicTreeUI() {
            @Override public Rectangle getPathBounds(JTree tree, TreePath path) {
                if (Objects.nonNull(tree) && Objects.nonNull(treeState)) {
                    return getPathBounds(path, tree.getInsets(), new Rectangle());
                }
                return null;
            }
            private Rectangle getPathBounds(TreePath path, Insets insets, Rectangle bounds) {
                Rectangle rect = treeState.getBounds(path, bounds);
                if (Objects.nonNull(rect)) {
                    rect.width = tree.getWidth();
                    rect.y += insets.top;
                }
                return rect;
            }
        });
        UIManager.put("Tree.repaintWholeRow", Boolean.TRUE);
        setCellRenderer(new Handler());
        setOpaque(false);
        setRootVisible(false);
        // http://ateraimemo.com/Swing/TreeNodeCollapseVeto.html
        listener = new TreeWillExpandListener() {
            @Override public void treeWillExpand(TreeExpansionEvent e) throws ExpandVetoException {
                 //throw new ExpandVetoException(e, "Tree expansion cancelled");
             }
            @Override public void treeWillCollapse(TreeExpansionEvent e) throws ExpandVetoException {
                 throw new ExpandVetoException(e, "Tree collapse cancelled");
             }
        };
        addTreeWillExpandListener(listener);
    }
    private static class Handler extends DefaultTreeCellRenderer {
        @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            JLabel l = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            l.setBackground(selected ? SELC : tree.getBackground());
            l.setOpaque(true);
            return l;
        }
    }
}

class EmptyIcon implements Icon {
    @Override public void paintIcon(Component c, Graphics g, int x, int y) { /* Empty icon */ }
    @Override public int getIconWidth() {
        return 0;
    }
    @Override public int getIconHeight() {
        return 0;
    }
}
