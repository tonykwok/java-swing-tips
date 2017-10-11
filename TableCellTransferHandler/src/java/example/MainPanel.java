package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.IOException;
import javax.activation.*;
import javax.swing.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private static final DataFlavor FLAVOR = new ActivationDataFlavor(JTable.class, DataFlavor.javaJVMLocalObjectMimeType, "JTable");
    private final String[] columnNames = {"String", "Icon", "Boolean"};
    private final Object[][] data = {
        {"aaa", new ColorIcon(Color.RED),    true},
        {"bbb", new ColorIcon(Color.GREEN),  false},
        {"ccc", new ColorIcon(Color.BLUE),   true},
        {"ddd", new ColorIcon(Color.ORANGE), true},
        {"eee", new ColorIcon(Color.PINK),   false},
        {"fff", new ColorIcon(Color.CYAN),   true},
    };
    public MainPanel() {
        super(new BorderLayout());
        JTable table = new JTable(new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                switch (column) {
                  case 0:
                    return String.class;
                  case 1:
                    return Icon.class;
                  case 2:
                    return Boolean.class;
                  default:
                    return super.getColumnClass(column);
                }
            }
        });
        table.setCellSelectionEnabled(true);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setTransferHandler(new CellIconTransferHandler(FLAVOR));
        table.setDragEnabled(true);
        table.setFillsViewportHeight(true);
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
        table.setRowSorter(sorter);

        //Disable row Cut, Copy, Paste
        ActionMap map = table.getActionMap();
        Action dummy = new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { /* Dummy action */ }
        };
        map.put(TransferHandler.getCutAction().getValue(Action.NAME),   dummy);
        map.put(TransferHandler.getCopyAction().getValue(Action.NAME),  dummy);
        map.put(TransferHandler.getPasteAction().getValue(Action.NAME), dummy);

        DefaultListModel<Icon> model = new DefaultListModel<>();
        JList<Icon> list = new JList<>(model);
        list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        list.setVisibleRowCount(0);
        list.setFixedCellWidth(16);
        list.setFixedCellHeight(16);
        list.setCellRenderer(new IconListCellRenderer<>());
        list.setTransferHandler(new TableCellTransferHandler(FLAVOR));

        JButton clearButton = new JButton("clear");
        clearButton.addActionListener(e -> {
            model.clear();
            sorter.setRowFilter(null);
        });

        JButton filterButton = new JButton("filter");
        filterButton.addActionListener(e -> {
            sorter.setRowFilter(new RowFilter<TableModel, Integer>() {
                @Override public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                    Object o = entry.getModel().getValueAt(entry.getIdentifier(), 1);
                    System.out.println(model.contains(o));
                    return model.isEmpty() || model.contains(o);
                }
            });
        });

        Box box = Box.createHorizontalBox();
        box.add(clearButton);
        box.add(filterButton);

        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        p.add(new JScrollPane(list));
        p.add(box, BorderLayout.EAST);

        add(p, BorderLayout.SOUTH);
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
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

class CellIconTransferHandler extends TransferHandler {
    private final DataFlavor localObjectFlavor;
    protected CellIconTransferHandler(DataFlavor flavor) {
        super();
        localObjectFlavor = flavor;
    }
    @Override protected Transferable createTransferable(JComponent c) {
        JTable table = (JTable) c;
        int idx = table.getSelectedColumn();
        if (Icon.class.isAssignableFrom(table.getColumnClass(idx))) {
            return new DataHandler(table, localObjectFlavor.getMimeType());
        }
        return null;
    }
    @Override public boolean canImport(TransferHandler.TransferSupport info) {
        return false;
    }
    @Override public int getSourceActions(JComponent c) {
        return TransferHandler.COPY;
    }
}

class TableCellTransferHandler extends TransferHandler {
    private final DataFlavor localObjectFlavor;
    protected TableCellTransferHandler(DataFlavor flavor) {
        super();
        localObjectFlavor = flavor;
    }
    @Override public boolean canImport(TransferHandler.TransferSupport info) {
        Component c = info.getComponent();
        if (c instanceof JList) {
            return info.isDrop() && info.isDataFlavorSupported(localObjectFlavor);
        }
        return false;
    }
    @Override public int getSourceActions(JComponent c) {
        return TransferHandler.COPY;
    }
    @SuppressWarnings("unchecked")
    @Override public boolean importData(TransferHandler.TransferSupport info) {
        if (!canImport(info)) {
            return false;
        }
        JList l = (JList) info.getComponent();
        try {
            Object o = info.getTransferable().getTransferData(localObjectFlavor);
            if (o instanceof JTable) {
                JTable t = (JTable) o;
                Object obj = t.getValueAt(t.getSelectedRow(), t.getSelectedColumn());
                ((DefaultListModel) l.getModel()).addElement(obj);
            }
            return true;
        } catch (UnsupportedFlavorException | IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}

class IconListCellRenderer<E extends Icon> implements ListCellRenderer<E> {
    private final JLabel l = new JLabel();
    @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
        l.setIcon(value);
        return l;
    }
}

class ColorIcon implements Icon {
    private final Color color;
    protected ColorIcon(Color color) {
        this.color = color;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.setPaint(color);
        g2.fillRect(1, 1, getIconWidth() - 2, getIconHeight() - 2);
        g2.dispose();
    }
    @Override public int getIconWidth() {
        return 12;
    }
    @Override public int getIconHeight() {
        return 12;
    }
}
