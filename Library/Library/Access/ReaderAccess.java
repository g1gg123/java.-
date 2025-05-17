package Library.Access;

import Library.libDB.Reader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

//用于对读者数据进行操作
public class ReaderAccess {
    //根据reader主码（readerId）进行查询
    public Reader searchReader(int readerId) throws SQLException{
        Reader res = new Reader();
        String sql="select * from Reader where ReaderID=?";
        try(Connection conn=DBini.getConnection();PreparedStatement ps=conn.prepareStatement(sql.toString())){
            ps.setInt(1, readerId);
            ResultSet rs=ps.executeQuery();
            while(rs.next()){
                Reader reader=new Reader();
                reader.setReaderID(rs.getInt("ReaderID"));
                reader.setAddress(rs.getString("Address"));
                reader.setFirstName(rs.getString("FirstName"));
                reader.setLastName(rs.getString("LastName"));
                reader.setLimits(rs.getInt("Limits"));
                reader.setPhoneNumber(rs.getString("PhoneNumber"));
                res=reader;
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        return res;
    }

    //获取所有的读者
    public ArrayList<Reader> getAllReaders() throws SQLException{
        String sql="select * from Reader";
        ArrayList<Reader> res=new ArrayList<>();
        try(Connection conn=DBini.getConnection();PreparedStatement ps=conn.prepareStatement(sql.toString())){
            ResultSet rs=ps.executeQuery();
            while(rs.next()){
                Reader reader=new Reader();
                reader.setReaderID(rs.getInt("ReaderID"));
                reader.setAddress(rs.getString("Address"));
                reader.setFirstName(rs.getString("FirstName"));
                reader.setLastName(rs.getString("LastName"));
                reader.setLimits(rs.getInt("Limits"));
                reader.setPhoneNumber(rs.getString("PhoneNumber"));
                res.add(reader);
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        return res;
    }

    //增添读者数据
    public boolean addReader(Reader reader) throws SQLException{
        String sql="insert into reader(ReaderID,FirstName,LastName,Address,PhoneNumber,Limits)values(?,?,?,?,?,?)";
        try(Connection conn=DBini.getConnection();PreparedStatement ps=conn.prepareStatement(sql.toString())){
            ps.setInt(1, reader.getReaderID());
            ps.setString(2, reader.getFirstName());
            ps.setString(3, reader.getLastName());
            ps.setString(4, reader.getAddress());
            ps.setString(5, reader.getPhoneNumber());
            ps.setInt(6, reader.getLimits());
            int affected=ps.executeUpdate();
            return affected>0;
        }
        catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    //修改读者数据
    public boolean updateReader(Reader reader) throws SQLException{
        String sql="update reader set FirstName=?,LastName=?,Address=?,PhoneNumber=?,Limits=? where ReaderID=?";
        try(Connection conn=DBini.getConnection();PreparedStatement ps=conn.prepareStatement(sql.toString())){
            ps.setString(1, reader.getFirstName());
            ps.setString(2, reader.getLastName());
            ps.setString(3, reader.getAddress());
            ps.setString(4, reader.getPhoneNumber());
            ps.setInt(5, reader.getLimits());
            ps.setInt(6, reader.getReaderID());
            int affected=ps.executeUpdate();
            return affected>0;
        }
        catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    //删除读者数据
    public boolean deleteReader(Reader reader) throws SQLException{
        int readerID=reader.getReaderID();
        //删除前检测是否有未归还记录
        String checkSql="select * from record where ReaderID=? and ReturnDate is null";
        try(Connection conn=DBini.getConnection();PreparedStatement ps=conn.prepareStatement(checkSql.toString())){
            ps.setInt(1, readerID);
            ResultSet rs=ps.executeQuery();
            if(rs.next()){return false;}
        }
        catch(SQLException e){
            e.printStackTrace();
            return false;
        }
        String sql="delete from reader where ReaderID=?";
        try(Connection conn=DBini.getConnection();PreparedStatement ps=conn.prepareStatement(sql.toString())){
            ps.setInt(1, readerID);
            int affected=ps.executeUpdate();
            return affected>0;
        }
        catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }


}
