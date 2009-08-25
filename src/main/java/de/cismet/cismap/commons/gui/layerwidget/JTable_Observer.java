/*
 * JTable_Observer.java
 * Copyright (C) 2005 by:
 *
 *----------------------------
 * cismet GmbH
 * Goebenstrasse 40
 * 66117 Saarbruecken
 * http://www.cismet.de
 *----------------------------
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *----------------------------
 * Author:
 * thorsten.hell@cismet.de
 *----------------------------
 *
 * Created on 24. November 2005, 17:41
 *
 */

package de.cismet.cismap.commons.gui.layerwidget;

/**
 *
 * @author thorsten.hell@cismet.de
 */
import java.awt.Component;
 
import javax.swing.*;
import javax.swing.table.*;
 
 
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;
 
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
 
/*
 * Created on Apr 18, 2005
 * JTable_Observer.java
 */
 
/**
 * @author Mayank Joshi 
 */
public class JTable_Observer extends JFrame implements Observer{
    
    JTable table;
    MyDefaultTableModel dtm ;
    public static void main(String[] args) {
        new JTable_Observer();
    }
    
    private JTable_Observer(){
        super("JTable using Observer");
        createUI();
        new ObservableChild().addObserver(this);
       
    }
    
    private void createUI(){
        dtm = new MyDefaultTableModel(new Object[] {"Column 0", "Progress "}, 0);
        CustomProgressBar progressBar = new CustomProgressBar();
        CustomProgressBar progressBar2 = new CustomProgressBar();
        CustomProgressBar progressBar3 = new CustomProgressBar();
        progressBar.setBorderPainted(true);
        progressBar.setIndeterminate(true);
        progressBar2.setBorderPainted(true);
        progressBar2.setIndeterminate(true);
        progressBar3.setBorderPainted(true);
        progressBar3.setIndeterminate(true);
        dtm.addRow(new Object[]{"Progress Bar",progressBar});
        dtm.addRow(new Object[]{"Progress Bar",progressBar2});
        dtm.addRow(new Object[]{"Progress Bar",progressBar3});
        table= new JTable(dtm);
        table.getColumnModel().getColumn(1).setCellRenderer(new CustomProgressBar());
        JScrollPane jsp = new JScrollPane(table);        
        getContentPane().add(jsp);
        pack();
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    public void update(Observable o, Object arg){        
        ((DefaultTableModel)table.getModel()).fireTableCellUpdated(0,1);
        ((DefaultTableModel)table.getModel()).fireTableCellUpdated(1,1);
        ((DefaultTableModel)table.getModel()).fireTableCellUpdated(2,1);
    }
}
 
class ObservableChild extends  Observable implements ActionListener{
    Timer timer = new Timer(500,this);
    
    public ObservableChild(){
        super();        
        timer.setRepeats(true);
        timer.start();
    }
    
    public void actionPerformed(ActionEvent e) {        
        setChanged();
        notifyObservers();
    }
}
 
class CustomProgressBar extends JProgressBar implements TableCellRenderer {
    
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value instanceof JComponent) {
            return (JComponent)value;
        } else {
            return null;
        }
    }
    
    public boolean  isDisplayable() {        
        return true;
    }
 
    
}
 
class MyDefaultTableModel extends DefaultTableModel{
    
    
    public MyDefaultTableModel (Object [] obj , int i){
        super(obj,i);
        
    }
    public boolean isCellEditable(int row,int column) {
        if(column==1){
            return false;
        }else{
            return true;
        }
    }
}
