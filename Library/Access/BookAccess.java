package Library.Access;

import Library.libDB.Books;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;


//用于对Books进行增删改查操作
public class BookAccess {
    /*
     * 根据给定的条件查询对应的书
     * 返回符合条件的书籍列表
     * 大小写不敏感
     * */
    public ArrayList<Books> searchBooks(String isbn,String title,String author,String publisher,String editionNum,String publishDate,String type) throws SQLException{
        ArrayList<Books> books=new ArrayList<>();
        StringBuilder sql=new StringBuilder("select * from Books where 1=1");
        ArrayList<Object> params=new ArrayList<>();//存储需要替代问号占位符的数据

        if(!isbn.isEmpty()&&!isbn.trim().isEmpty()){
            sql.append(" and ISBN=?");
            params.add(isbn);
        }
        if(!title.isEmpty()&&!title.trim().isEmpty()){
            sql.append(" and title like ?");
            params.add("%"+title.trim()+"%");//用于模糊匹配
        }
        if(!author.isEmpty()&&!author.trim().isEmpty()){
            sql.append(" and authors like ?");
            params.add("%"+author.trim()+"%");
        }
        if(!publisher.isEmpty()&&!publisher.trim().isEmpty()){
            sql.append(" and publisher like ?");
            params.add("%"+publisher.trim()+"%");
        }
        if(!editionNum.isEmpty()&&!editionNum.trim().isEmpty()){
            try{
                int edition=Integer.parseInt(editionNum);
                sql.append(" and edition = ?");
                params.add(edition);
            }catch(Exception e){/*版次错误则忽略该条件*/}
        }
        if(!publishDate.isEmpty()&&!publishDate.trim().isEmpty()){
            sql.append(" and publicationdate like ?");
            params.add("%"+publishDate.trim()+"%");
        }
        if(!type.isEmpty()&&!type.trim().isEmpty()){
            sql.append(" and type like ?");
            params.add("%"+type.trim()+"%");
        }

        try(Connection conn=DBini.getConnection();PreparedStatement ps=conn.prepareStatement(sql.toString())){
            for(int i=0;i<params.size();i++){
                ps.setObject(i+1,params.get(i));
            }
            ResultSet rs=ps.executeQuery();
            while(rs.next()){
                Books book=new Books();
                book.setISBN(rs.getString("ISBN"));
                book.setTitle(rs.getString("Title"));
                book.setAuthors(rs.getString("Authors"));
                book.setPublisher(rs.getString("Publisher"));
                book.setEditionNumber(rs.getInt("EditionNumber"));
                Date date=rs.getDate("PublicationDate");
                book.setPublicationDate(date!=null?date.toString():null);
                book.setType(rs.getString("Type"));
                books.add(book);
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return books;
    }

    /*
     * 获取所有书籍
     * */
    public ArrayList<Books> getAllbooks() throws SQLException{
        ArrayList<Books> books=new ArrayList<>();
        String sql="select * from books";
        try(Connection conn=DBini.getConnection();PreparedStatement ps=conn.prepareStatement(sql)){
            ResultSet rs=ps.executeQuery();
            while(rs.next()){
                Books book=new Books();
                book.setISBN(rs.getString("ISBN"));
                book.setTitle(rs.getString("Title"));
                book.setAuthors(rs.getString("Authors"));
                book.setPublisher(rs.getString("Publisher"));
                book.setEditionNumber(rs.getInt("EditionNumber"));
                Date date=rs.getDate("PublicationDate");
                book.setPublicationDate(date!=null?date.toString():null);
                book.setType(rs.getString("Type"));
                books.add(book);
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        return books;
    }

    /*
    *根据主码（ISBN）查询书籍
    * */
    public Books getBooksByISBN(String ISBN) throws SQLException{
        Books book=new Books();
        StringBuilder sql=new StringBuilder("select * from Books where ISBN=?");
        try(Connection conn=DBini.getConnection();PreparedStatement ps=conn.prepareStatement(sql.toString())){
            ps.setObject(1,ISBN);
            ResultSet rs=ps.executeQuery();
            while(rs.next()){
                book.setISBN(rs.getString("ISBN"));
                book.setTitle(rs.getString("Title"));
                book.setAuthors(rs.getString("Authors"));
                book.setPublisher(rs.getString("Publisher"));
                book.setEditionNumber(rs.getInt("EditionNumber"));
                Date date=rs.getDate("PublicationDate");
                book.setPublicationDate(date!=null?date.toString():null);
                book.setType(rs.getString("Type"));
            }
        }
        catch(SQLException e){
            System.out.println("查询失败:"+e.getMessage());
        }
        return book;
    }

    /*
    * 插入书籍，成功返回true
    * */
    public boolean addBook(Books book) throws SQLException{
        String sql="insert into Books(ISBN,Title,Authors,Publisher,EditionNumber,PublicationDate,Type) values(?,?,?,?,?,?,?)";
        try(Connection conn=DBini.getConnection();PreparedStatement ps=conn.prepareStatement(sql)){
            ps.setString(1,book.getISBN());
            ps.setString(2,book.getTitle());
            ps.setString(3,book.getAuthors());
            ps.setString(4,book.getPublisher());
            ps.setInt(5,book.getEditionNumber());
            ps.setString(6,book.getPublicationDate());
            ps.setString(7,book.getType());
            //根据影响的数量判断是否插入成功
            int affected=ps.executeUpdate();
            return affected>0;
        }
        catch(SQLException e){
            System.out.println("插入失败: "+e.getMessage());
            return false;
        }
    }

    /*
    * 更新书籍，成功返回true
    * */
    public boolean updateBook(Books book) throws SQLException{
        String sql="update Books set Title=?,Authors=?,Publisher=?,EditionNumber=?,PublicationDate=?,Type=? where ISBN=?";
        try(Connection conn=DBini.getConnection();PreparedStatement ps=conn.prepareStatement(sql)){
            ps.setString(1,book.getTitle());
            ps.setString(2,book.getAuthors());
            ps.setString(3,book.getPublisher());
            ps.setInt(4,book.getEditionNumber());
            ps.setString(5,book.getPublicationDate());
            ps.setString(6,book.getType());
            ps.setString(7,book.getISBN());
            int affected=ps.executeUpdate();
            return affected>0;
        }
        catch(SQLException e){
            System.out.println("更新失败: "+e.getMessage());
            return false;
        }
    }

    /*
    * 删除书籍
    * */
    public boolean deleteBook(Books book) throws SQLException{
        //检查书籍是否有归还
        String checkSql="select * from Record where ISBN=? and ReturnDate is NULL";
        try(Connection conn=DBini.getConnection();PreparedStatement ps=conn.prepareStatement(checkSql)){
            ps.setString(1,book.getISBN());
            ResultSet rs=ps.executeQuery();
            if(rs.next()){return false;}
        }
        catch(SQLException e){
            System.out.println("检查失败: "+e.getMessage());
            return false;
        }
        String sql="delete from Books where ISBN=?";
        try(Connection conn=DBini.getConnection();PreparedStatement ps=conn.prepareStatement(sql)){
            ps.setString(1,book.getISBN());
            int affected=ps.executeUpdate();
            return affected>0;
        }
        catch(SQLException e){
            System.out.println("删除失败: "+e.getMessage());
            return false;
        }
    }
}
