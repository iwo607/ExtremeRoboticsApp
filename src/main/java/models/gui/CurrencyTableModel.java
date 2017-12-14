package models.gui;

import models.Currency;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Iwo Skwierawski on 13.12.17.
 * Custom created table model to fill currency table
 */
public class CurrencyTableModel implements TableModel
{
    private final String[] columnNames = {"Currency", "Price"};
    private List<Currency> data = new ArrayList<>();

    public CurrencyTableModel(List<Currency> data)
    {
        this.data = data;
    }

    @Override
    public int getRowCount()
    {
        return data.size();
    }

    @Override
    public int getColumnCount()
    {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int columnIndex)
    {
        return columnNames[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex)
    {
        return Currency.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        if(columnIndex == 0)
            return data.get(rowIndex).getName();
        if(columnIndex == 1)
            return data.get(rowIndex).getCurrentPrice().getPrice();
        return null;
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
