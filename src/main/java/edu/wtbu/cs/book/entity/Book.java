package edu.wtbu.cs.book.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 图书实体类
 * 对应数据库表：books
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    /**
     * 主键 ID
     */
    private Integer id;

    /**
     * 书名
     */
    private String title;

    /**
     * 作者
     */
    private String author;

    /**
     * 出版社
     */
    private String publisher;

    /**
     * ISBN
     */
    private String isbn;

    /**
     * 简介
     */
    private String description;

    /**
     * 封面图片 URL
     */
    private String coverUrl;

    /**
     * 图书分类
     */
    private String category;

    /**
     * 总复本数
     */
    private Integer totalCopies;

    /**
     * 可借复本数
     */
    private Integer availableCopies;

    /**
     * 添加时间
     */
    private LocalDateTime createdAt;

    /**
     * 判断是否可借
     */
    public boolean isAvailable() {
        return this.availableCopies != null && this.availableCopies > 0;
    }

    /**
     * 判断是否已全部借出
     */
    public boolean isAllBorrowed() {
        return this.availableCopies != null && this.availableCopies == 0;
    }

    /**
     * 判断是否有借阅记录（用于删除检查）
     */
    public boolean hasBorrowRecord() {
        return this.totalCopies != null && this.availableCopies != null && this.availableCopies < this.totalCopies;
    }
}