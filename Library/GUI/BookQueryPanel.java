package Library.GUI;

import Library.Access.BookAccess;
import Library.libDB.Books;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;

public class BookQueryPanel extends JPanel{
    private JTextField isbnFiled;
    private JTextField titleField;
    private JTextField authorField;
    private JTextField publisherField;
    private JTextField editionField;
    private JTextField publicationDateField;
    private JTextField typeField;
    private BookAccess ba;
    private JTable resTable;


    public BookQueryPanel(){
        ba=new BookAccess();
        setLayout(new BorderLayout());

        //筛选条件用
        JPanel filterPanel=new JPanel();
        filterPanel.setLayout(new GridLayout(4,2,5,5));
        //第一行
        JLabel isbnLabel=new JLabel("ISBN:");
        filterPanel.add(isbnLabel);
        isbnFiled=new JTextField(10);
        filterPanel.add(isbnFiled);

        JLabel titleLabel=new JLabel("书名:");
        filterPanel.add(titleLabel);
        titleField=new JTextField(10);
        filterPanel.add(titleField);

        //第二行
        JLabel authorLabel=new JLabel("作者:");
        filterPanel.add(authorLabel);
        authorField=new JTextField(10);
        filterPanel.add(authorField);

        JLabel editionLabel=new JLabel("编辑次数:");
        filterPanel.add(editionLabel);
        editionField=new JTextField(10);
        filterPanel.add(editionField);

        //第三行
        JLabel publisherLabel=new JLabel("出版社:");
        filterPanel.add(publisherLabel);
        publisherField=new JTextField(10);
        filterPanel.add(publisherField);

        JLabel publicationLabel=new JLabel("出版日期:");
        filterPanel.add(publicationLabel);
        publicationDateField=new JTextField(10);
        filterPanel.add(publicationDateField);

        //第四行
        JLabel typeLabel=new JLabel("类型:");
        filterPanel.add(typeLabel);
        typeField=new JTextField(10);
        filterPanel.add(typeField);

        JLabel jl=new JLabel();
        filterPanel.add(jl);

        add(filterPanel,BorderLayout.NORTH);

        resTable=new JTable();
        resTable.setFillsViewportHeight(true);
        resTable.setAutoCreateRowSorter(true);
        JScrollPane resScrollPane=new JScrollPane(resTable);
        add(resScrollPane,BorderLayout.CENTER);

        JPanel buttonPanel=new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        JButton jb=new JButton("查询");
        buttonPanel.add(jb);
        add(buttonPanel,BorderLayout.SOUTH);

        jb.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                try{
                    research();
                }catch(SQLException ex){
                    System.out.println("书籍查询失败!");
                    throw new RuntimeException(ex);
                }
            }
        });

    }

    private void research() throws SQLException{
        String isbn=isbnFiled.getText().trim();
        String title=titleField.getText().trim();
        String author=authorField.getText().trim();
        String edition=editionField.getText().trim();
        String publisher=publisherField.getText().trim();
        String publicationDate=publicationDateField.getText().trim();
        String type=typeField.getText().trim();

        ArrayList<Books> books=ba.searchBooks(isbn,title,author,publisher,edition,publicationDate,type);
        String[] colunmnName={"ISBN","书名","作者","出版社","编辑次数","出版日期","类型"};

        DefaultTableModel model=new DefaultTableModel(colunmnName,0){
            //设置不可编辑
            @Override
            public boolean isCellEditable(int row,int column){
                return false;
            }
        };

        for(Books b : books){
            Object[] rowData={b.getISBN(),b.getTitle(),b.getAuthors(),b.getPublisher(),b.getEditionNumber(),b.getPublicationDate(),b.getType()};
            model.addRow(rowData);
        }
        resTable.setModel(model);
        resTable.setAutoCreateRowSorter(true);
    }
}
