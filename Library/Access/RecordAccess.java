package Library.Access;

import Library.libDB.Reader;
import Library.libDB.Record;
import Library.libDB.Books;

import java.sql.*;
import java.util.ArrayList;


//实现借书与还书的逻辑
public class RecordAccess{
    private ReaderAccess ra=new ReaderAccess();
    private BookAccess ba=new BookAccess();

    /*
     * 借出前判断读者与书籍是否存在
     * 然后判断是否抵达读者借书上限
     * 返回操作结果的信息
     * */
    public String borrowBook(String isbn,int readerId) throws SQLException{
        //检测读者与书籍是否存在
        Books book=ba.getBooksByISBN(isbn);
        if(book==null){return "该书籍不存在,ISBN="+isbn;}
        Reader reader=ra.searchReader(readerId);
        if(reader==null){return "读者不存在,ReaderID="+readerId;}

        //查询书籍是否被借
        String checkSql="select * from record where ISBN=? and ReturnDate is null";
        try(Connection conn=DBini.getConnection();PreparedStatement ps=conn.prepareStatement(checkSql)){
            ps.setString(1,isbn);
            ResultSet rs=ps.executeQuery();
            if(rs.next()){
                return "借书失败，该书籍已借出";
            }
        }catch(SQLException e){
            e.printStackTrace();
            return "查询书籍状态失败";
        }

        //查询数量是否抵达上限
        String sql="select count(*) as cnt from record where ReaderID=? and ReturnDate is null";
        try(Connection conn=DBini.getConnection();PreparedStatement ps=conn.prepareStatement(sql)){
            ps.setInt(1,readerId);
            ResultSet rs=ps.executeQuery();
            if(rs.next()){
                int cnt=rs.getInt("cnt");
                if(cnt>=reader.getLimits()){
                    return "读者借书数量已经抵达上限! 上限:"+reader.getLimits();
                }
            }
        }catch(SQLException e){
            e.printStackTrace();
            return "查询该读者借书数量失败";
        }

        //执行借书操作
        String insertSql="insert into record(ISBN,ReaderID,BorrowingDate) values(?,?,?)";
        try(Connection conn=DBini.getConnection();PreparedStatement ps=conn.prepareStatement(insertSql)){
            ps.setString(1,isbn);
            ps.setInt(2,readerId);
            //获取当前系统的时间作为借书的时间
            Date now=new Date(System.currentTimeMillis());
            ps.setDate(3,now);
            int affected=ps.executeUpdate();
            if(affected>0){return "借书成功";}
            else return "借书失败";
        }catch(SQLException e){
            e.printStackTrace();
            return "借书过程中出现错误";
        }
    }

    /*
     * 归还前判断判断读者与书籍是否存在
     * */
    public String returnBook(String isbn,int readerId) throws SQLException{
        //检测书籍是否存在
        Books book=ba.getBooksByISBN(isbn);
        if(book==null){return "该书籍不存在,ISBN="+isbn;}
        Reader reader=ra.searchReader(readerId);
        if(reader==null){return "读者不存在,ReaderID="+readerId;}

        Record record=null;
        //寻找要归还的书籍
        String returnSql="select * from record where ISBN=? and ReaderID=? and ReturnDate is null";
        try(Connection conn=DBini.getConnection();PreparedStatement ps=conn.prepareStatement(returnSql)){
            ps.setString(1,isbn);
            ps.setInt(2,readerId);
            ResultSet rs=ps.executeQuery();
            if(rs.next()){
                record=new Record();
                record.setRecordID(rs.getInt("RecordID"));
            }
            else return "未找到要归还的书籍";
        }catch(SQLException e){
            e.printStackTrace();
            return "寻找过程中出现错误";
        }

        //重新更新,设置归还数据
        String sql="update record set ReturnDate=? where RecordID=? ";
        try(Connection conn=DBini.getConnection();PreparedStatement ps=conn.prepareStatement(sql)){
            //设置当前系统日期为归还日期
            Date now=new Date(System.currentTimeMillis());
            ps.setDate(1,now);
            ps.setInt(2,record.getRecordID());
            int affected=ps.executeUpdate();
            if(affected>0) return "归还成功";
            else return "归还失败";
        }catch(SQLException e){
            e.printStackTrace();
            return "归还失败"+e.getMessage();
        }
    }

    //根据读者id查询借阅记录
    public ArrayList<Record> searchRecords(int readerId){
        ArrayList<Record> list=new ArrayList<>();
        String sql="select r.RecordID,r.ISBN,r.BorrowingDate,r.ReturnDate,b.Title as bookTitle from record r join books b on r.ISBN=b.ISBN where r.ReaderID=?";

        try(Connection conn=DBini.getConnection();PreparedStatement ps=conn.prepareStatement(sql)){
            ps.setInt(1,readerId);
            ResultSet rs=ps.executeQuery();
            while(rs.next()){
                Record record=new Record();
                record.setRecordID(rs.getInt("RecordID"));
                record.setISBN(rs.getString("ISBN"));
                Date borrowDate=rs.getDate("BorrowingDate");
                record.setBorrowingDate(borrowDate!=null ? borrowDate.toString() : null);
                Date returnDate=rs.getDate("ReturnDate");
                record.setReturnDate(returnDate!=null ? returnDate.toString() : null);
                record.setReaderID(readerId);
                record.setBookTitle(rs.getString("bookTitle"));
                //System.out.println(record.toString());
                list.add(record);
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return list;
    }
}
