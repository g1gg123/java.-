package Library.GUI;

import Library.Access.RecordAccess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

public class ReturnPanel extends javax.swing.JPanel{
    private JTextField isbnField;
    private JTextField readerIDField;
    private JButton returnButton;
    private RecordAccess ra;

    public ReturnPanel(){
        setLayout(new GridBagLayout());
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.insets=new Insets(10,10,10,10);
        gbc.anchor=GridBagConstraints.WEST;

        gbc.gridx=0;
        gbc.gridy=0;
        add(new JLabel("ISBN:"),gbc);
        isbnField=new JTextField(15);
        gbc.gridx=1;
        gbc.gridy=0;
        add(isbnField,gbc);

        gbc.gridx=0;
        gbc.gridy=1;
        add(new JLabel("读者id:"),gbc);
        readerIDField=new JTextField(15);
        gbc.gridx=1;
        gbc.gridy=1;
        add(readerIDField,gbc);

        gbc.gridx=1;
        gbc.gridy=2;
        returnButton=new JButton("归还");
        add(returnButton,gbc);

        returnButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                try{
                    excuteReturn();
                }catch(SQLException ex){
                    JOptionPane.showMessageDialog(ReturnPanel.this,"归还失败!");
                }
            }
        });
    }

    public void excuteReturn() throws SQLException{
        ra=new RecordAccess();
        String isbn=isbnField.getText().trim();
        String readerText=readerIDField.getText().trim();
        if(isbn.isEmpty()||readerText.isEmpty()){
            JOptionPane.showMessageDialog(ReturnPanel.this,"ISBN与读者都不能为空!");
            return;
        }
        int readerID;
        try{
            readerID=Integer.parseInt(readerText);
        }catch(NumberFormatException e){
            JOptionPane.showMessageDialog(ReturnPanel.this,"id必须为数字");
            return;
        }
        String res=ra.returnBook(isbn,readerID);
        JOptionPane.showMessageDialog(ReturnPanel.this,res);
    }
}
