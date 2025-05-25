package Library.Access;

import java.sql.*;

//用于管理数据库连接的工具类
public class DBini{
    private static final String url="jdbc:mysql://localhost:3306/library?useSSL=false&serverTimezone=UTC";
    private static final String user="root";
    private static final String password="123456";

    static{
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public static Connection getConnection() throws SQLException{
        return DriverManager.getConnection(url,user,password);
    }
}
