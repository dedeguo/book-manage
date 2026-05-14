package edu.wtbu.cs.book.service;

import edu.wtbu.cs.book.dao.BorrowRecordDao;
import edu.wtbu.cs.book.entity.Book;
import edu.wtbu.cs.book.entity.BorrowRecord;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 借阅服务层
 */
public class BorrowService {

    private final BorrowRecordDao borrowRecordDao = new BorrowRecordDao();
    private final BookService bookService = new BookService();

    /**
     * 借阅图书
     * @param userId 用户 ID
     * @param bookId 图书 ID
     * @return 0=失败，1=成功，2=已达借阅上限，3=有超期未还，4=图书不可借
     */
    public int borrow(Integer userId, Integer bookId) {
        // 检查图书是否存在且可借
        Book book = bookService.getBookById(bookId);
        if (book == null || !book.isAvailable()) {
            return 4; // 图书不可借
        }

        // 检查是否有超期未还记录
        if (borrowRecordDao.hasOverdueRecord(userId)) {
            return 3; // 有超期未还
        }

        // 检查借阅数量上限（5 本）
        int borrowingCount = borrowRecordDao.countByUserIdAndStatus(userId, "borrowing");
        if (borrowingCount >= 5) {
            return 2; // 已达借阅上限
        }

        // 创建借阅记录
        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setBorrowDate(LocalDateTime.now());
        record.setDueDate(LocalDateTime.now().plusDays(30)); // 30 天借阅期
        record.setStatus("borrowing");

        // 事务处理
        int recordId = borrowRecordDao.insert(record);
        if (recordId > 0) {
            if (bookService.borrowBook(bookId)) {
                return 1; // 成功
            } else {
                // 回滚借阅记录
                // 实际应使用事务，这里简化处理
                return 0;
            }
        }
        return 0; // 失败
    }

    /**
     * 归还图书
     * @param userId 用户 ID
     * @param bookId 图书 ID
     * @return 0=失败，1=成功
     */
    public int returnBook(Integer userId, Integer bookId) {
        // 查找借阅记录
        List<BorrowRecord> records = borrowRecordDao.findByUserIdAndStatus(userId, "borrowing");
        for (BorrowRecord record : records) {
            if (record.getBookId().equals(bookId)) {
                // 更新借阅记录
                int result = borrowRecordDao.returnBook(record.getId());
                if (result > 0) {
                    // 增加图书库存
                    bookService.returnBook(bookId);
                    return 1;
                }
            }
        }
        return 0;
    }

    /**
     * 获取用户的借阅记录
     */
    public List<BorrowRecord> getUserBorrowRecords(Integer userId) {
        return borrowRecordDao.findByUserId(userId);
    }

    /**
     * 获取用户当前借阅记录
     */
    public List<BorrowRecord> getUserBorrowingRecords(Integer userId) {
        return borrowRecordDao.findByUserIdAndStatus(userId, "borrowing");
    }

    /**
     * 获取用户历史借阅记录（已归还）
     */
    public List<BorrowRecord> getUserReturnedRecords(Integer userId) {
        return borrowRecordDao.findByUserIdAndStatus(userId, "returned");
    }

    /**
     * 获取所有借阅记录
     */
    public List<BorrowRecord> getAllRecords() {
        return borrowRecordDao.findAll();
    }

    /**
     * 搜索借阅记录
     */
    public List<BorrowRecord> searchRecords(String username, String name, String bookTitle, String status) {
        return borrowRecordDao.search(username, name, bookTitle, status);
    }

    /**
     * 获取超期借阅记录
     */
    public List<BorrowRecord> getOverdueRecords() {
        return borrowRecordDao.findOverdue();
    }

    /**
     * 管理员代为归还
     */
    public int adminReturn(Integer recordId) {
        BorrowRecord record = borrowRecordDao.findById(recordId);
        if (record == null || !record.isBorrowing()) {
            return 0;
        }

        int result = borrowRecordDao.returnBook(recordId);
        if (result > 0) {
            bookService.returnBook(record.getBookId());
            return 1;
        }
        return 0;
    }

    /**
     * 检查用户是否可以借阅
     */
    public boolean canBorrow(Integer userId) {
        // 检查是否有超期未还
        if (borrowRecordDao.hasOverdueRecord(userId)) {
            return false;
        }
        // 检查借阅数量
        int count = borrowRecordDao.countByUserIdAndStatus(userId, "borrowing");
        return count < 5;
    }

    /**
     * 获取用户当前借阅数量
     */
    public int getBorrowingCount(Integer userId) {
        return borrowRecordDao.countByUserIdAndStatus(userId, "borrowing");
    }
}