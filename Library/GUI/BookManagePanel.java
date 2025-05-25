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

//用于管理书籍增删改
public class BookManagePanel extends JPanel{
    private JTextField isbnField;
    private JTextField titleField;
    private JTextField authorField;
    private JTextField publisherField;
    private JTextField editionField;
    private JTextField publicationDateField;
    private JTextField typeField;
    private BookAccess ba=new BookAccess();
    private JTable resTable;

    private JButton addButton;
    private JButton deleteButton;
    private JButton updateButton;
    private JButton clearButton;

    public BookManagePanel() throws SQLException{
        setLayout(new BorderLayout());

        //显示书籍数据的面板
        resTable=new JTable();
        JScrollPane resPanel=new JScrollPane(resTable);
        ArrayList<Books> books=ba.getAllbooks();
        String[] colunmnName={"ISBN","书名","作者","出版社","编辑次数","出版日期","类型"};
        DefaultTableModel model=new DefaultTableModel(colunmnName,0){
            public boolean isCellEditable(int row,int column){return false;}
        };
        for(Books b : books){
            Object[] rowData={b.getISBN(),b.getTitle(),b.getAuthors(),b.getPublisher(),b.getEditionNumber(),b.getPublicationDate(),b.getType()};
            model.addRow(rowData);
        }
        resTable.setModel(model);
        resTable.setAutoCreateRowSorter(true);
        //禁止对表格进行大小，顺序的编辑
        resTable.getTableHeader().setReorderingAllowed(false);
        resTable.getTableHeader().setResizingAllowed(false);

        add(resPanel,BorderLayout.CENTER);

        //设置增删改的面板
        JPanel opJPanel=new JPanel(new GridBagLayout());
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.insets=new Insets(5,5,5,5);
        gbc.anchor=GridBagConstraints.NORTH;

        gbc.gridx=0;
        gbc.gridy=0;
        opJPanel.add(new JLabel("ISBN"),gbc);
        gbc.gridx=1;
        gbc.gridy=0;
        opJPanel.add(isbnField=new JTextField(15),gbc);

        gbc.gridx=0;
        gbc.gridy=1;
        opJPanel.add(new JLabel("书名"),gbc);
        gbc.gridx=1;
        gbc.gridy=1;
        opJPanel.add(titleField=new JTextField(15),gbc);

        gbc.gridx=0;
        gbc.gridy=2;
        opJPanel.add(new JLabel("作者:"),gbc);
        gbc.gridx=1;
        gbc.gridy=2;
        opJPanel.add(authorField=new JTextField(15),gbc);

        gbc.gridx=0;
        gbc.gridy=3;
        opJPanel.add(new JLabel("出版社:"),gbc);
        gbc.gridx=1;
        gbc.gridy=3;
        opJPanel.add(publisherField=new JTextField(15),gbc);

        gbc.gridx=0;
        gbc.gridy=4;
        opJPanel.add(new JLabel("编辑次数:"),gbc);
        gbc.gridx=1;
        gbc.gridy=4;
        opJPanel.add(editionField=new JTextField(15),gbc);

        gbc.gridx=0;
        gbc.gridy=5;
        opJPanel.add(new JLabel("出版日期:"),gbc);
        gbc.gridx=1;
        gbc.gridy=5;
        opJPanel.add(publicationDateField=new JTextField(15),gbc);

        gbc.gridx=0;
        gbc.gridy=6;
        opJPanel.add(new JLabel("类型:"),gbc);
        gbc.gridx=1;
        gbc.gridy=6;
        opJPanel.add(typeField=new JTextField(15),gbc);

        gbc.gridx=0;
        gbc.gridy=7;
        gbc.gridwidth=2;
        gbc.weightx=1;
        gbc.fill=GridBagConstraints.HORIZONTAL;
        gbc.anchor=GridBagConstraints.CENTER;
        JPanel btnPanel=new JPanel(new GridLayout(1,4,10,0));
        btnPanel.add(addButton=new JButton("增加"));
        btnPanel.add(deleteButton=new JButton("删除"));
        btnPanel.add(updateButton=new JButton("修改"));
        btnPanel.add(clearButton=new JButton("清除"));
        opJPanel.add(btnPanel,gbc);

        add(opJPanel,BorderLayout.WEST);

        addButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                String isbn=isbnField.getText().trim();
                String title=titleField.getText().trim();
                String author=authorField.getText().trim();
                String publisher=publisherField.getText().trim();
                String edition=editionField.getText().trim();
                String publicationDate=publicationDateField.getText().trim();
                String type=typeField.getText().trim();

                if(isbn.isEmpty()||title.isEmpty()||author.isEmpty()||publisher.isEmpty()||edition.isEmpty()||type.isEmpty()){
                    JOptionPane.showMessageDialog(BookManagePanel.this,"不能含有空值!");
                    return;
                }

                int editionNum;
                try{
                    editionNum=Integer.parseInt(edition);

                }catch(Exception ex){
                    JOptionPane.showMessageDialog(BookManagePanel.this,"编辑次数输入错误!");
                    return;
                }
                if(editionNum<0){
                    JOptionPane.showMessageDialog(BookManagePanel.this,"编辑次数需要大于等于0!");
                    return;
                }

                try{
                    boolean addSuccess=ba.addBook(new Books(isbn,title,author,publisher,editionNum,publicationDate,type));
                    if(!addSuccess){
                        JOptionPane.showMessageDialog(BookManagePanel.this,"添加失败!");
                    }
                    else{
                        refreshTbale();
                        clear();
                    }
                }catch(SQLException ex){
                    JOptionPane.showMessageDialog(BookManagePanel.this,"添加失败!");
                    throw new RuntimeException(ex);
                }

            }
        });

        deleteButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                String isbn=isbnField.getText().trim();
                if(isbn.isEmpty()){
                    JOptionPane.showMessageDialog(BookManagePanel.this,"ISBN不允许为空!");
                    return;
                }
                Books book;
                try{
                    book=ba.getBooksByISBN(isbn);
                }catch(SQLException ex){
                    JOptionPane.showMessageDialog(BookManagePanel.this,"获取要删除的书籍数据失败!");
                    return;
                }

                try{
                    boolean deleteSuccess=ba.deleteBook(book);
                    if(!deleteSuccess){
                        JOptionPane.showMessageDialog(BookManagePanel.this,"删除数据失败!");
                        return;
                    }
                    else{
                        refreshTbale();
                        clear();
                    }
                }catch(SQLException ex){
                    JOptionPane.showMessageDialog(BookManagePanel.this,"删除过程出现错误!");
                    throw new RuntimeException(ex);
                }

            }
        });

        updateButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                String isbn=isbnField.getText().trim();
                String title=titleField.getText().trim();
                String author=authorField.getText().trim();
                String publisher=publisherField.getText().trim();
                String edition=editionField.getText().trim();
                String publicationDate=publicationDateField.getText().trim();
                String type=typeField.getText().trim();

                //获取原数据
                Books oriBook;
                if(isbn.isEmpty()){
                    JOptionPane.showMessageDialog(BookManagePanel.this,"ISBN不能为空!");
                    return;
                }
                try{
                    oriBook=ba.getBooksByISBN(isbn);
                }catch(SQLException ex){
                    JOptionPane.showMessageDialog(BookManagePanel.this,"获取原数据失败!");
                    return;
                }

                int editionNum;
                try{
                    editionNum=Integer.parseInt(edition.isEmpty() ? "0" : edition);
                    if(editionNum<0){
                        JOptionPane.showMessageDialog(BookManagePanel.this,"编辑次数要为正整数!");
                        return;
                    }
                }catch(Exception ex){
                    JOptionPane.showMessageDialog(BookManagePanel.this,"编辑次数要为整数!");
                    return;
                }

                String oriTitle=oriBook.getTitle();
                String oriAuthor=oriBook.getAuthors();
                String oriPublisher=oriBook.getPublisher();
                int oriEditionNum=oriBook.getEditionNumber();
                String oriPublicationDate=oriBook.getPublicationDate();
                String oriType=oriBook.getType();

                if(!edition.isEmpty()){
                    try{
                        editionNum=Integer.parseInt(edition);
                        if(editionNum<0){
                            JOptionPane.showMessageDialog(BookManagePanel.this,"编辑次数要大于等于0!");
                            return;
                        }
                    }catch(Exception ex){
                        JOptionPane.showMessageDialog(BookManagePanel.this,"编辑次数输入错误");
                        return;
                    }
                }
                try{
                    title=title.isEmpty() ? oriTitle : title;
                    author=author.isEmpty() ? oriAuthor : author;
                    publisher=publisher.isEmpty() ? oriPublisher : publisher;
                    editionNum=edition.isEmpty() ? oriEditionNum : editionNum;
                    publicationDate=publicationDate.isEmpty() ? oriPublicationDate : publicationDate;
                    type=type.isEmpty() ? oriType : type;
                    boolean updateSuccess=ba.updateBook(new Books(isbn,title,author,publisher,editionNum,publicationDate,type));
                    if(!updateSuccess){
                        JOptionPane.showMessageDialog(BookManagePanel.this,"更新失败!");
                        return;
                    }
                    else{
                        refreshTbale();
                        clear();
                    }
                }catch(SQLException ex){
                    throw new RuntimeException(ex);
                }
            }
        });

        clearButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                clear();
            }
        });

    }

    //刷新表格数据
    public void refreshTbale() throws SQLException{
        ArrayList<Books> books=ba.getAllbooks();
        String[] columnNames={"ISBN","书名","作者","出版社","版次","出版日期","类型"};
        DefaultTableModel model=new DefaultTableModel(columnNames,0){
            @Override
            public boolean isCellEditable(int row,int column){
                return false;
            }
        };
        for(Books book : books){
            Object[] rowData={book.getISBN(),book.getTitle(),book.getAuthors(),book.getPublisher(),book.getEditionNumber(),book.getPublicationDate(),book.getType()};
            model.addRow(rowData);
        }
        resTable.setModel(model);
        resTable.setAutoCreateRowSorter(true);
    }

    //清除数据
    public void clear(){
        isbnField.setText("");
        titleField.setText("");
        authorField.setText("");
        publisherField.setText("");
        editionField.setText("");
        publicationDateField.setText("");
        typeField.setText("");
    }

}
