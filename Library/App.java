package Library;

import Library.Access.BookAccess;
import Library.Access.ReaderAccess;
import Library.Access.RecordAccess;
import Library.GUI.*;

import java.sql.SQLException;

public class App{
    public static void main(String[] args) throws SQLException{
        MainFrame mainFrame=new MainFrame();
        mainFrame.setVisible(true);
    }

}
