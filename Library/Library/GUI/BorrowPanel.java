package Library.GUI;

import Library.Access.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

public class BorrowPanel extends javax.swing.JPanel{
    private JTextField isbnField;
    private JTextField readerField;
    private JButton borrowButton;
    private RecordAccess ra;

    public BorrowPanel(){
        setLayout(new GridBagLayout());
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.insets=new Insets(10,10,10,10);
        gbc.anchor=GridBagConstraints.WEST;

        gbc.gridx=0;
        gbc.gridy=0;
        add(new JLabel("ISBN:"),gbc);
        gbc.gridx=1;
        gbc.gridy=0;
        add(isbnField=new JTextField(15),gbc);

        gbc.gridx=0;
        gbc.gridy=1;
        add(new JLabel("读者id:"),gbc);

        gbc.gridx=1;
        gbc.gridy=1;
        add(readerField=new JTextField(15),gbc);

        gbc.gridx=1;
        gbc.gridy=2;
        add(borrowButton=new JButton("借书"),gbc);

        borrowButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                try{
                    excuteBorrow();
                }catch(SQLException ex){
                    JOptionPane.showMessageDialog(BorrowPanel.this,"借出失败!");
                    return;
                }
            }
        });
    }

    public void excuteBorrow() throws SQLException{
        ra=new RecordAccess();
        String isbn=isbnField.getText().trim();
        String readerText=readerField.getText().trim();
        if(isbn.isEmpty()||readerText.isEmpty()){
            JOptionPane.showMessageDialog(BorrowPanel.this,"ISBN与读者都不能为空!");
            return;
        }
        int readerID;
        try{
            readerID=Integer.parseInt(readerText);
        }catch(NumberFormatException e){
            JOptionPane.showMessageDialog(BorrowPanel.this,"id必须为数字");
            return;
        }
        String res=ra.borrowBook(isbn,readerID);
        JOptionPane.showMessageDialog(BorrowPanel.this,res);

    }
}
