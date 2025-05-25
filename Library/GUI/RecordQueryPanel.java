package Library.GUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import Library.Access.*;
import Library.libDB.Reader;
import Library.libDB.Record;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;

public class RecordQueryPanel extends JPanel{
    private JTextField readerId;
    private JLabel rederLabel;
    private JTable recordTable;
    private RecordAccess recordAccess;
    private ReaderAccess readerAccess;
    private JButton queryButton;

    public RecordQueryPanel(){
        recordAccess=new RecordAccess();
        readerAccess=new ReaderAccess();
        setLayout(new BorderLayout());

        //北部面板，用于显示读者id输入以及查询按钮
        JPanel northPanel=new JPanel(new FlowLayout(FlowLayout.LEFT));
        rederLabel=new JLabel("读者id:");
        northPanel.add(rederLabel);
        readerId=new JTextField(10);
        northPanel.add(readerId);
        queryButton=new JButton("查询");
        northPanel.add(queryButton);
        add(northPanel,BorderLayout.NORTH);

        recordTable=new JTable();
        recordTable.setFillsViewportHeight(true);
        recordTable.setAutoCreateRowSorter(true);
        recordTable.getTableHeader().setReorderingAllowed(false);
        recordTable.getTableHeader().setResizingAllowed(false);
        JScrollPane scrollPane=new JScrollPane(recordTable);
        add(scrollPane,BorderLayout.CENTER);

        queryButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                try{
                    excuteQuery();
                }catch(SQLException ex){
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    public void excuteQuery() throws SQLException{
        String idText=readerId.getText().trim();
        if(idText.isEmpty()){
            JOptionPane.showMessageDialog(RecordQueryPanel.this,"请输入读者ID");
            return;
        }
        int readerID;
        try{
            readerID=Integer.parseInt(idText);
            if(readerID<0){
                JOptionPane.showMessageDialog(RecordQueryPanel.this,"读者id必须为正整数");
                return;
            }
        }catch(NumberFormatException ex){
            JOptionPane.showMessageDialog(RecordQueryPanel.this,"读者ID必须为数字");
            return;
        }
        Reader reader=readerAccess.searchReader(readerID);
        if(reader==null){
            JOptionPane.showMessageDialog(RecordQueryPanel.this,"读者不存在");
            recordTable.setModel(new DefaultTableModel());
            return;
        }
        ArrayList<Library.libDB.Record> records=recordAccess.searchRecords(readerID);
        String[] columnNames={"记录ID","ISBN","书名","借出日期","归还日期"};
        DefaultTableModel model=new DefaultTableModel(columnNames,0){
            @Override
            public boolean isCellEditable(int row,int col){
                return false;
            }
        };
        for(Record r : records){
            Object[] rowData={r.getRecordID(),r.getISBN(),r.getBookTitle(),r.getBorrowingDate(),r.getReturnDate()};
            model.addRow(rowData);
        }
        recordTable.setModel(model);
        recordTable.setAutoCreateRowSorter(true);
    }

}
