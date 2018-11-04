package com.uclv.gui;

import com.uclv.clustering.DataPoint;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class TableModelMFs implements TableModel {
    private DataPoint[] point;
    private String[] columnames = new String[]{"Documents", "Degree of cluster "};
    private double[][] membership;

    public TableModelMFs(DataPoint[] p, double[][] m) {
        point = p;
        membership = m;
    }

    @Override
    public int getRowCount() {
        return point.length;
    }

    @Override
    public int getColumnCount() {
        return membership[0].length + 1;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex == 0) {
            return columnames[0];
        } else {
            return columnames[1] + (columnIndex - 1);
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 0 ? String.class : Double.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return point[rowIndex].getDoc().getField("address").stringValue();
        } else {
            return membership[rowIndex][columnIndex - 1];
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
