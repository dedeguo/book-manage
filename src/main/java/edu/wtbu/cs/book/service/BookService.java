package edu.wtbu.cs.book.service;

import edu.wtbu.cs.book.dao.BookDao;
import edu.wtbu.cs.book.entity.Book;

import java.util.List;

/**
 * 图书服务层
 */
public class BookService {

    private final BookDao bookDao = new BookDao();

    /**
     * 获取所有图书
     */
    public List<Book> getAllBooks() {
        return bookDao.findAll();
    }

    /**
     * 根据 ID 获取图书
     */
    public Book getBookById(Integer id) {
        return bookDao.findById(id);
    }

    /**
     * 根据分类获取图书
     */
    public List<Book> getBooksByCategory(String category) {
        if (category == null || category.isEmpty() || "全部".equals(category)) {
            return bookDao.findAll();
        }
        return bookDao.findByCategory(category);
    }

    /**
     * 搜索图书
     */
    public List<Book> searchBooks(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return bookDao.findAll();
        }
        return bookDao.search(keyword);
    }

    /**
     * 新增图书
     */
    public boolean addBook(Book book) {
        if (book.getTotalCopies() == null || book.getTotalCopies() <= 0) {
            book.setTotalCopies(1);
        }
        if (book.getAvailableCopies() == null) {
            book.setAvailableCopies(book.getTotalCopies());
        }
        return bookDao.insert(book) > 0;
    }

    /**
     * 更新图书
     */
    public boolean updateBook(Book book) {
        return bookDao.update(book) > 0;
    }

    /**
     * 删除图书
     */
    public boolean deleteBook(Integer id) {
        Book book = bookDao.findById(id);
        if (book == null) {
            return false;
        }
        // 检查是否有借阅记录
        if (book.hasBorrowRecord()) {
            return false;
        }
        return bookDao.delete(id) > 0;
    }

    /**
     * 获取所有分类
     */
    public List<String> getAllCategories() {
        return bookDao.findAllCategories();
    }

    /**
     * 统计图书总数
     */
    public int getTotalCount() {
        return bookDao.countTotal();
    }

    /**
     * 统计可借复本总数
     */
    public int getAvailableCount() {
        return bookDao.countAvailable();
    }

    /**
     * 借阅图书（减少库存）
     */
    public boolean borrowBook(Integer bookId) {
        Book book = bookDao.findById(bookId);
        if (book == null || !book.isAvailable()) {
            return false;
        }
        return bookDao.decreaseAvailableCopies(bookId) > 0;
    }

    /**
     * 归还图书（增加库存）
     */
    public boolean returnBook(Integer bookId) {
        Book book = bookDao.findById(bookId);
        if (book == null) {
            return false;
        }
        return bookDao.increaseAvailableCopies(bookId) > 0;
    }
}