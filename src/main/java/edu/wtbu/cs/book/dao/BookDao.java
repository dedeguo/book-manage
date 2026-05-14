package edu.wtbu.cs.book.dao;

import edu.wtbu.cs.book.entity.Book;
import edu.wtbu.cs.book.util.JDBCUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 图书数据访问对象
 */
public class BookDao {

    /**
     * 查询所有图书
     */
    public List<Book> findAll() {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT * FROM books ORDER BY created_at DESC";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 根据 ID 查询图书
     */
    public Book findById(Integer id) {
        String sql = "SELECT * FROM books WHERE id = ?";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据分类查询图书
     */
    public List<Book> findByCategory(String category) {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE category = ? ORDER BY created_at DESC";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category);
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
     * 搜索图书（书名、作者、ISBN）
     */
    public List<Book> search(String keyword) {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? OR isbn LIKE ? ORDER BY created_at DESC";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            pstmt.setString(3, pattern);
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
     * 新增图书
     */
    public int insert(Book book) {
        String sql = "INSERT INTO books (title, author, publisher, isbn, description, cover_url, category, total_copies, available_copies) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setString(3, book.getPublisher());
            pstmt.setString(4, book.getIsbn());
            pstmt.setString(5, book.getDescription());
            pstmt.setString(6, book.getCoverUrl());
            pstmt.setString(7, book.getCategory());
            pstmt.setInt(8, book.getTotalCopies());
            pstmt.setInt(9, book.getAvailableCopies());
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
     * 更新图书信息
     */
    public int update(Book book) {
        String sql = "UPDATE books SET title = ?, author = ?, publisher = ?, isbn = ?, description = ?, cover_url = ?, category = ? WHERE id = ?";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setString(3, book.getPublisher());
            pstmt.setString(4, book.getIsbn());
            pstmt.setString(5, book.getDescription());
            pstmt.setString(6, book.getCoverUrl());
            pstmt.setString(7, book.getCategory());
            pstmt.setInt(8, book.getId());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 更新图书库存
     */
    public int updateStock(Integer id, Integer availableCopies) {
        String sql = "UPDATE books SET available_copies = ? WHERE id = ?";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, availableCopies);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 减少可借复本（借阅时调用）
     */
    public int decreaseAvailableCopies(Integer id) {
        String sql = "UPDATE books SET available_copies = available_copies - 1 WHERE id = ? AND available_copies > 0";
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
     * 增加可借复本（归还时调用）
     */
    public int increaseAvailableCopies(Integer id) {
        String sql = "UPDATE books SET available_copies = available_copies + 1 WHERE id = ?";
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
     * 删除图书
     */
    public int delete(Integer id) {
        String sql = "DELETE FROM books WHERE id = ?";
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
     * 查询所有图书分类（去重）
     */
    public List<String> findAllCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM books WHERE category IS NOT NULL ORDER BY category";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    /**
     * 统计图书总数
     */
    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM books";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 统计可借复本总数
     */
    public int countAvailable() {
        String sql = "SELECT SUM(available_copies) FROM books";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 映射结果集到 Book 对象
     */
    private Book mapRow(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getInt("id"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setPublisher(rs.getString("publisher"));
        book.setIsbn(rs.getString("isbn"));
        book.setDescription(rs.getString("description"));
        book.setCoverUrl(rs.getString("cover_url"));
        book.setCategory(rs.getString("category"));
        book.setTotalCopies(rs.getInt("total_copies"));
        book.setAvailableCopies(rs.getInt("available_copies"));
        book.setCreatedAt(rs.getTimestamp("created_at") != null ?
                         rs.getTimestamp("created_at").toLocalDateTime() : null);
        return book;
    }
}