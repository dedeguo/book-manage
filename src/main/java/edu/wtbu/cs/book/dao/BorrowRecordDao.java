package edu.wtbu.cs.book.dao;

import edu.wtbu.cs.book.entity.Book;
import edu.wtbu.cs.book.entity.User;
import edu.wtbu.cs.book.entity.BorrowRecord;
import edu.wtbu.cs.book.util.JDBCUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 借阅记录数据访问对象
 *
 * 数据库连接是通过
 * try-with-resources 语句自动释放的。工作原理：
 *   - 当 try 块执行完毕（正常结束或抛出异常），Java
 *   会自动调用 Connection、PreparedStatement、ResultSet
 *   对象的 close() 方法
 *   - 这是 Java 7+ 引入的语法，实现了 AutoCloseable
 *   接口的资源会自动关闭
 * DAO 层没有显式调用 close() 方法，而是依赖
 *   Java 的 try-with-resources 机制自动释放数据库连接，
 *   代码简洁且安全，不会造成连接泄漏。
 */
public class BorrowRecordDao {

    /**
     * 查询用户的借阅记录
     */
    public List<BorrowRecord> findByUserId(Integer userId) {
        List<BorrowRecord> list = new ArrayList<>();
        String sql = "SELECT * FROM borrow_records WHERE user_id = ? ORDER BY borrow_date DESC";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 查询用户的借阅记录（带状态筛选）
     */
    public List<BorrowRecord> findByUserIdAndStatus(Integer userId, String status) {
        List<BorrowRecord> list = new ArrayList<>();
        String sql = "SELECT * FROM borrow_records WHERE user_id = ? AND status = ? ORDER BY borrow_date DESC";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 查询所有借阅记录
     */
    public List<BorrowRecord> findAll() {
        List<BorrowRecord> list = new ArrayList<>();
        String sql = "SELECT br.*, u.username, u.name as user_name, b.title as book_title " +
                     "FROM borrow_records br " +
                     "JOIN users u ON br.user_id = u.id " +
                     "JOIN books b ON br.book_id = b.id " +
                     "ORDER BY br.borrow_date DESC";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowWithInfo(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 根据 ID 查询借阅记录
     */
    public BorrowRecord findById(Integer id) {
        String sql = "SELECT br.*, u.username, u.name as user_name, b.title as book_title " +
                     "FROM borrow_records br " +
                     "JOIN users u ON br.user_id = u.id " +
                     "JOIN books b ON br.book_id = b.id " +
                     "WHERE br.id = ?";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowWithInfo(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 搜索借阅记录
     */
    public List<BorrowRecord> search(String username, String name, String bookTitle, String status) {
        List<BorrowRecord> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT br.*, u.username, u.name as user_name, b.title as book_title " +
            "FROM borrow_records br " +
            "JOIN users u ON br.user_id = u.id " +
            "JOIN books b ON br.book_id = b.id " +
            "WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (username != null && !username.isEmpty()) {
            sql.append(" AND u.username LIKE ?");
            params.add("%" + username + "%");
        }
        if (name != null && !name.isEmpty()) {
            sql.append(" AND u.name LIKE ?");
            params.add("%" + name + "%");
        }
        if (bookTitle != null && !bookTitle.isEmpty()) {
            sql.append(" AND b.title LIKE ?");
            params.add("%" + bookTitle + "%");
        }
        if (status != null && !status.isEmpty()) {
            sql.append(" AND br.status = ?");
            params.add(status);
        }

        sql.append(" ORDER BY br.borrow_date DESC");

        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowWithInfo(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 查询超期借阅记录
     */
    public List<BorrowRecord> findOverdue() {
        List<BorrowRecord> list = new ArrayList<>();
        String sql = "SELECT br.*, u.username, u.name as user_name, b.title as book_title " +
                     "FROM borrow_records br " +
                     "JOIN users u ON br.user_id = u.id " +
                     "JOIN books b ON br.book_id = b.id " +
                     "WHERE br.status = 'borrowing' AND br.due_date < NOW() " +
                     "ORDER BY br.due_date ASC";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowWithInfo(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 新增借阅记录
     */
    public int insert(BorrowRecord record) {
        String sql = "INSERT INTO borrow_records (user_id, book_id, borrow_date, due_date, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, record.getUserId());
            pstmt.setInt(2, record.getBookId());
            pstmt.setTimestamp(3, record.getBorrowDate() != null ?
                               Timestamp.valueOf(record.getBorrowDate()) : Timestamp.valueOf(java.time.LocalDateTime.now()));
            pstmt.setTimestamp(4, record.getDueDate() != null ?
                               Timestamp.valueOf(record.getDueDate()) : Timestamp.valueOf(java.time.LocalDateTime.now().plusDays(30)));
            pstmt.setString(5, record.getStatus() != null ? record.getStatus() : "borrowing");
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
            return affected;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 更新借阅记录（归还时用）
     */
    public int update(BorrowRecord record) {
        String sql = "UPDATE borrow_records SET status = ?, return_date = ? WHERE id = ?";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, record.getStatus());
            pstmt.setTimestamp(2, record.getReturnDate() != null ?
                               Timestamp.valueOf(record.getReturnDate()) : Timestamp.valueOf(java.time.LocalDateTime.now()));
            pstmt.setInt(3, record.getId());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 归还图书（更新状态和归还时间）
     */
    public int returnBook(Integer id) {
        String sql = "UPDATE borrow_records SET status = 'returned', return_date = NOW() WHERE id = ?";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 统计用户当前借阅数量
     */
    public int countByUserIdAndStatus(Integer userId, String status) {
        String sql = "SELECT COUNT(*) FROM borrow_records WHERE user_id = ? AND status = ?";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 检查用户是否有超期未还记录
     */
    public boolean hasOverdueRecord(Integer userId) {
        String sql = "SELECT COUNT(*) FROM borrow_records WHERE user_id = ? AND status = 'borrowing' AND due_date < NOW()";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 检查图书是否有借阅记录
     */
    public boolean hasBorrowRecord(Integer bookId) {
        String sql = "SELECT COUNT(*) FROM borrow_records WHERE book_id = ?";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 检查用户是否有借阅中的记录
     */
    public boolean hasBorrowingRecord(Integer userId) {
        String sql = "SELECT COUNT(*) FROM borrow_records WHERE user_id = ? AND status = 'borrowing'";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 映射结果集到 BorrowRecord 对象（带关联信息）
     */
    private BorrowRecord mapRowWithInfo(ResultSet rs) throws SQLException {
        BorrowRecord record = mapRow(rs);

        // 设置关联的用户信息
        User user = new User();
        user.setId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setName(rs.getString("user_name"));
        record.setUser(user);

        // 设置关联的图书信息
        Book book = new Book();
        book.setId(rs.getInt("book_id"));
        book.setTitle(rs.getString("book_title"));
        record.setBook(book);

        return record;
    }

    /**
     * 映射结果集到 BorrowRecord 对象
     */
    private BorrowRecord mapRow(ResultSet rs) throws SQLException {
        BorrowRecord record = new BorrowRecord();
        record.setId(rs.getInt("id"));
        record.setUserId(rs.getInt("user_id"));
        record.setBookId(rs.getInt("book_id"));
        record.setBorrowDate(rs.getTimestamp("borrow_date") != null ?
                            rs.getTimestamp("borrow_date").toLocalDateTime() : null);
        record.setDueDate(rs.getTimestamp("due_date") != null ?
                         rs.getTimestamp("due_date").toLocalDateTime() : null);
        record.setReturnDate(rs.getTimestamp("return_date") != null ?
                            rs.getTimestamp("return_date").toLocalDateTime() : null);
        record.setStatus(rs.getString("status"));
        return record;
    }
}