package edu.wtbu.cs.book.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 借阅记录实体类
 * 对应数据库表：borrow_records
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRecord {

    /**
     * 主键 ID
     */
    private Integer id;

    /**
     * 用户 ID
     */
    private Integer userId;

    /**
     * 图书 ID
     */
    private Integer bookId;

    /**
     * 借阅日期
     */
    private LocalDateTime borrowDate;

    /**
     * 应还日期
     */
    private LocalDateTime dueDate;

    /**
     * 实际归还日期
     */
    private LocalDateTime returnDate;

    /**
     * 状态（borrowing/returned）
     */
    private String status;

    /**
     * 关联的用户信息（非数据库字段，用于展示）
     */
    private User user;

    /**
     * 关联的图书信息（非数据库字段，用于展示）
     */
    private Book book;

    /**
     * 判断是否借阅中
     */
    public boolean isBorrowing() {
        return "borrowing".equals(this.status);
    }

    /**
     * 判断是否已归还
     */
    public boolean isReturned() {
        return "returned".equals(this.status);
    }

    /**
     * 判断是否超期
     */
    public boolean isOverdue() {
        if (!isBorrowing()) {
            return false;
        }
        return LocalDateTime.now().isAfter(this.dueDate);
    }

    /**
     * 计算超期天数
     */
    public long getOverdueDays() {
        if (!isOverdue()) {
            return 0;
        }
        return java.time.Duration.between(this.dueDate, LocalDateTime.now()).toDays();
    }
}