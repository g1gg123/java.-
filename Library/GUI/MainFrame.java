package Library.GUI;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame{
    public MainFrame(){
        setTitle("图书管理系统");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setSize(1280,720);

        JTabbedPane jtp=new JTabbedPane();
        jtp.add("查询图书",new BookQueryPanel());
        jtp.add("借书",new BorrowPanel());
        jtp.add("还书",new ReturnPanel());
        jtp.add("图书管理",new BookManagePanel());
        jtp.add("读者管理",new ReaderManagePanel());
        jtp.add("读者借阅记录",new RecordQueryPanel());

        add(jtp,BorderLayout.CENTER);
    }
}
