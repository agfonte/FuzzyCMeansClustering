package com.uclv.gui;

import com.uclv.clustering.DataPoint;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;


public class TableModelDocs implements TableModel {

    private DataPoint[] point;
    private String[] columnames = new String[]{"Address", "Author", "Title", "Date"};

    public TableModelDocs(DataPoint[] docs) {
        this.point = docs;
    }

    @Override
    public int getRowCount() {
        return point.length;
    }

    @Override
    public int getColumnCount() {
        return columnames.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnames[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        DataPoint dataPoint = point[rowIndex];
        if (dataPoint.getDoc() == null) {
            return "null";
        }
        switch (columnIndex) {
            case 0:
                return dataPoint.getDoc().getField("address").stringValue();
            case 1:
                return dataPoint.getDoc().getField("author").stringValue();
            case 2:
                return dataPoint.getDoc().getField("title").stringValue();
            case 3:
                return dataPoint.getDoc().getField("date").stringValue();
            default:
                return "null";
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

    }

    @Override
    public void addTableModelListener(TableModelListener l) {

    }

    @Override
    public void removeTableModelListener(TableModelListener l) {

    }
}
