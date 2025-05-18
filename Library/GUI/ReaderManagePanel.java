package Library.GUI;

import Library.Access.ReaderAccess;
import Library.libDB.Books;
import Library.libDB.Reader;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;

public class ReaderManagePanel extends javax.swing.JPanel {
    private JTextField readerIDField;
    private JTextField fnameField;
    private JTextField lnameField;
    private JTextField addressField;
    private JTextField phoneField;
    private JTextField limField;
    private ReaderAccess ra=new ReaderAccess();
    private JButton addButton;
    private JButton deleteButton;
    private JButton updateButton;
    private JButton clearButton;
    private JTable resTable;

    public ReaderManagePanel() throws SQLException{
        setLayout(new BorderLayout());

        ArrayList<Reader> readers=ra.getAllReaders();
        String[] columnNames = {"ID","FirstName","LastName","地址","电话号码","借阅次数"};
        DefaultTableModel model = new DefaultTableModel(columnNames,0){
            public boolean isCellEditable(int row, int column) {return false;}
        };
        for(Reader reader:readers) {
            Object[] row = {
                    reader.getReaderID(),
                    reader.getFirstName(),
                    reader.getLastName(),
                    reader.getAddress(),
                    reader.getPhoneNumber(),
                    reader.getLimits()
            };
            model.addRow(row);
        }
        resTable = new JTable(model);
        resTable.setAutoCreateRowSorter(true);
        //禁止对表格进行大小，顺序的编辑
        resTable.getTableHeader().setReorderingAllowed(false);
        resTable.getTableHeader().setResizingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(resTable);
        add(scrollPane, BorderLayout.CENTER);

        //对文本框与按钮的编辑
        JPanel jp=new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(5,5,5,5);

        gbc.gridx = 0;gbc.gridy = 0;
        jp.add(new JLabel("ID"), gbc);
        gbc.gridx = 1;gbc.gridy = 0;
        jp.add(readerIDField=new JTextField(15), gbc);

        gbc.gridx=0;gbc.gridy=1;
        jp.add(new JLabel("First Name"), gbc);
        gbc.gridx=1;gbc.gridy=1;
        jp.add(fnameField=new JTextField(15), gbc);

        gbc.gridx=0;gbc.gridy=2;
        jp.add(new JLabel("Last Name"), gbc);
        gbc.gridx=1;gbc.gridy=2;
        jp.add(lnameField=new JTextField(15), gbc);

        gbc.gridx=0;gbc.gridy=3;
        jp.add(new JLabel("地址"), gbc);
        gbc.gridx=1;gbc.gridy=3;
        jp.add(addressField=new JTextField(15), gbc);

        gbc.gridx=0;gbc.gridy=4;
        jp.add(new JLabel("电话号码"), gbc);
        gbc.gridx=1;gbc.gridy=4;
        jp.add(phoneField=new JTextField(15), gbc);

        gbc.gridx=0;gbc.gridy=5;
        jp.add(new JLabel("借阅次数"), gbc);
        gbc.gridx=1;gbc.gridy=5;
        jp.add(limField=new JTextField(15), gbc);

        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx=0;gbc.gridy=6;
        gbc.weightx = 1.0;
        gbc.gridwidth=2;
        JPanel btnjp=new JPanel(new GridLayout(1,4,10,0));
        btnjp.add(addButton=new JButton("增加"));
        btnjp.add(deleteButton=new JButton("删除"));
        btnjp.add(updateButton=new JButton("修改"));
        btnjp.add(clearButton=new JButton("清除"));

        jp.add(btnjp,gbc);
        add(jp, BorderLayout.WEST);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String readerID=readerIDField.getText().trim();
                String firstName=fnameField.getText().trim();
                String lastName=lnameField.getText().trim();
                String address=addressField.getText().trim();
                String phone=phoneField.getText().trim();
                String limit=limField.getText().trim();

                if(readerID.isEmpty()||firstName.isEmpty()||lastName.isEmpty()||address.isEmpty()||phone.isEmpty()) {
                    JOptionPane.showMessageDialog(ReaderManagePanel.this,"不能含有空值!");
                    return;
                }

                int readerIdNum=0;
                int limitNum=-1;
                try{
                    readerIdNum=Integer.parseInt(readerID);
                    limitNum=Integer.parseInt(limit);
                    if(readerIdNum<0||limitNum<=-1) {
                        JOptionPane.showMessageDialog(ReaderManagePanel.this,"借阅次数跟ID必须是自然数!");
                        return;
                    }
                }
                catch(NumberFormatException ex){
                    JOptionPane.showMessageDialog(ReaderManagePanel.this,"借阅次数跟ID必须是整数!");
                    return;
                }

                Reader reader=new Reader(readerIdNum,firstName,lastName,address,phone,limitNum);
                try{
                    boolean addSuccess=ra.addReader(reader);
                    if(addSuccess) {
                       refresshTable();
                       clear();
                    }
                    else{
                        JOptionPane.showMessageDialog(ReaderManagePanel.this,"增添读者数据失败!");
                        return;
                    }
                }
                catch(SQLException ex){
                    JOptionPane.showMessageDialog(ReaderManagePanel.this,"增添读者数据失败，错误信息:"+ex.getMessage());
                    return;
                }
            }
        });

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                String readerID=readerIDField.getText().trim();
                String firstName=fnameField.getText().trim();
                String lastName=lnameField.getText().trim();
                String address=addressField.getText().trim();
                String phone=phoneField.getText().trim();
                String limit=limField.getText().trim();

                if(readerID.isEmpty()){
                    JOptionPane.showMessageDialog(ReaderManagePanel.this,"读者id不能为空!");
                    return;
                }
                int readerIdNum=0;
                try{
                    readerIdNum=Integer.parseInt(readerID);
                    if(readerIdNum<0){
                        JOptionPane.showMessageDialog(ReaderManagePanel.this,"读者id必须为正整数!");
                        return;
                    }
                }
                catch(NumberFormatException ex){
                    JOptionPane.showMessageDialog(ReaderManagePanel.this,"读者id必须为整数!");
                    return;
                }
                int limitNum=-1;
                if(!limit.isEmpty()){
                    try{
                        limitNum=Integer.parseInt(limit);
                        if(limitNum<=-1){
                            JOptionPane.showMessageDialog(ReaderManagePanel.this,"借阅次数必须为自然数!");
                            return;
                        }
                    }
                    catch(NumberFormatException ex){
                        JOptionPane.showMessageDialog(ReaderManagePanel.this,"借阅次数必须为整数!");
                        return;
                    }
                }

                //获取原读者数据
                Reader oriReader;
                try{
                    oriReader=ra.searchReader(readerIdNum);
                }
                catch(SQLException ex){
                    JOptionPane.showMessageDialog(ReaderManagePanel.this,"获取原读者数据失败!");
                    return;
                }
                String oriFirstName=oriReader.getFirstName();
                String oriLastName=oriReader.getLastName();
                String oriAddress=oriReader.getAddress();
                String oriPhone=oriReader.getPhoneNumber();
                int oriLimit=oriReader.getLimits();

                try{
                    firstName=firstName.isEmpty()?oriFirstName:firstName;
                    lastName=lastName.isEmpty()?oriLastName:lastName;
                    address=address.isEmpty()?oriAddress:address;
                    phone=phone.isEmpty()?oriPhone:phone;
                    limitNum=limitNum==-1?oriLimit:limitNum;
                    boolean updateSuccess=ra.updateReader(new Reader(readerIdNum,firstName,lastName,address,phone,limitNum));
                    if(!updateSuccess) {
                        JOptionPane.showMessageDialog(ReaderManagePanel.this,"更新失败!");
                        return;
                    }
                    else{
                        refresshTable();
                        clear();
                    }
                }catch(SQLException ex){
                    throw new RuntimeException(ex);
                }

            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                String readerID=readerIDField.getText().trim();
                if(readerID.isEmpty()){
                    JOptionPane.showMessageDialog(ReaderManagePanel.this,"读者id不能为空!");
                    return;
                }
                int readerIdNum=0;
                try{
                    readerIdNum=Integer.parseInt(readerID);
                    if(readerIdNum<0){
                        JOptionPane.showMessageDialog(ReaderManagePanel.this,"读者id必须为正整数!");
                        return;
                    }
                }
                catch(RuntimeException ex){
                    JOptionPane.showMessageDialog(ReaderManagePanel.this,"读者id必须为整数!");
                    return;
                }
                Reader reader;
                try{
                    reader=ra.searchReader(readerIdNum);
                }catch(SQLException ex){
                    JOptionPane.showMessageDialog(ReaderManagePanel.this,"获取要删除的读者数据失败!");
                    return;
                }

                try{
                    boolean deleteSuccess=ra.deleteReader(reader);
                    if(!deleteSuccess) {
                        JOptionPane.showMessageDialog(ReaderManagePanel.this,"删除失败");
                        return;
                    }
                    else{
                        refresshTable();
                        clear();
                    }
                }catch(SQLException ex){
                    JOptionPane.showMessageDialog(ReaderManagePanel.this,"删除过程出现错误!");
                    throw new RuntimeException(ex);
                }
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                clear();
            }
        });
    }


    public void refresshTable() throws SQLException{
        ArrayList<Reader> readers=ra.getAllReaders();
        String[] columnNames = {"ID","FirstName","LastName","地址","电话号码","借阅次数"};
        DefaultTableModel model = new DefaultTableModel(columnNames,0){
            public boolean isCellEditable(int row, int column) {return false;}
        };
        for(Reader reader:readers) {
            Object[] row = {
                    reader.getReaderID(),
                    reader.getFirstName(),
                    reader.getLastName(),
                    reader.getAddress(),
                    reader.getPhoneNumber(),
                    reader.getLimits()
            };
            model.addRow(row);
        }
        resTable.setModel(model);
        resTable.setAutoCreateRowSorter(true);
    }

    public void clear(){
        readerIDField.setText("");
        fnameField.setText("");
        lnameField.setText("");
        addressField.setText("");
        phoneField.setText("");
        limField.setText("");
    }


}
