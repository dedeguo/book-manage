package edu.wtbu.cs.book.util;


import com.alibaba.druid.pool.DruidDataSourceFactory;
import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class JDBCUtils {
    private static DataSource ds;

    // 静态代码块：类加载时初始化连接池
    static {
        try {
            Properties pro = new Properties();
            InputStream is = JDBCUtils.class.getClassLoader().getResourceAsStream("druid.properties");
            pro.load(is);
            ds = DruidDataSourceFactory.createDataSource(pro);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("连接池初始化失败！");
        }
    }

    // 获取连接：从池中拿
    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    // 获取数据源：供后续框架使用
    public static DataSource getDataSource() {
        return ds;
    }

    // 释放资源：归还连接
    public static void close(ResultSet rs, Statement stmt, Connection conn) {
        if (rs != null) try { rs.close(); } catch (SQLException e) {}
        if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
        if (conn != null) try {
            conn.close(); // 注意：此处是“归还”给连接池
        } catch (SQLException e) {}
    }
}
