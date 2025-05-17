package Library;

import Library.Access.*;
import Library.libDB.*;

import java.sql.SQLException;
import java.util.ArrayList;

public class Test {
    public static void main(String[] args) throws SQLException{
        BookAccess ba=new BookAccess();
        ReaderAccess ra=new ReaderAccess();
        RecordAccess rr=new RecordAccess();

        String s=rr.borrowBook("978003",1003);
        System.out.println(s);
        String r=rr.returnBook("978003",1003);
        System.out.println(r);
    }
}
