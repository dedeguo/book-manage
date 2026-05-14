package edu.wtbu.cs.book.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库表：users
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * 主键 ID
     */
    private Integer id;

    /**
     * 用户名（学号/工号，唯一）
     */
    private String username;

    /**
     * 姓名
     */
    private String name;

    /**
     * 密码（加密）
     */
    private String password;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 角色（user/admin）
     */
    private String role;

    /**
     * 用户类型（student/teacher/admin）
     */
    private String userType;

    /**
     * 注册时间
     */
    private LocalDateTime createdAt;

    /**
     * 判断是否为管理员
     */
    public boolean isAdmin() {
        return "admin".equals(this.role);
    }

    /**
     * 判断是否为学生
     */
    public boolean isStudent() {
        return "student".equals(this.userType);
    }

    /**
     * 判断是否为教师
     */
    public boolean isTeacher() {
        return "teacher".equals(this.userType);
    }
}