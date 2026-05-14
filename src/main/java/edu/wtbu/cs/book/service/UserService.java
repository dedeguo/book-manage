package edu.wtbu.cs.book.service;

import edu.wtbu.cs.book.dao.UserDao;
import edu.wtbu.cs.book.entity.User;

import java.util.List;

/**
 * 用户服务层
 */
public class UserService {

    private final UserDao userDao = new UserDao();

    /**
     * 用户登录
     */
    public User login(String username, String password) {
        User user = userDao.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    /**
     * 用户注册
     */
    public boolean register(String username, String name, String password, String email) {
        // 检查用户名是否已存在
        User existingUser = userDao.findByUsername(username);
        if (existingUser != null) {
            return false;
        }

        User user = new User();
        user.setUsername(username);
        user.setName(name);
        user.setPassword(password); // 实际应加密
        user.setEmail(email);
        user.setRole("user");
        user.setUserType(determineUserType(username));

        int result = userDao.insert(user);
        return result > 0;
    }

    /**
     * 根据 ID 获取用户
     */
    public User getUserById(Integer id) {
        return userDao.findById(id);
    }

    /**
     * 根据用户名获取用户
     */
    public User getUserByUsername(String username) {
        return userDao.findByUsername(username);
    }

    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    /**
     * 修改密码
     */
    public boolean changePassword(Integer userId, String oldPassword, String newPassword) {
        User user = userDao.findById(userId);
        if (user == null || !user.getPassword().equals(oldPassword)) {
            return false;
        }
        return userDao.updatePassword(userId, newPassword) > 0;
    }

    /**
     * 修改邮箱
     */
    public boolean changeEmail(Integer userId, String newEmail) {
        return userDao.updateEmail(userId, newEmail) > 0;
    }

    /**
     * 更新用户信息
     */
    public boolean updateUser(User user) {
        return userDao.update(user) > 0;
    }

    /**
     * 删除用户
     */
    public boolean deleteUser(Integer userId) {
        User user = userDao.findById(userId);
        if (user == null) {
            return false;
        }
        // 管理员账号不能删除
        if ("admin".equals(user.getRole())) {
            return false;
        }
        return userDao.delete(userId) > 0;
    }

    /**
     * 搜索用户
     */
    public List<User> searchUsers(String keyword) {
        return userDao.search(keyword);
    }

    /**
     * 根据角色查询用户
     */
    public List<User> getUsersByRole(String role) {
        return userDao.findByRole(role);
    }

    /**
     * 判断用户类型（学生/教师）
     */
    private String determineUserType(String username) {
        if (username == null || username.isEmpty()) {
            return "student";
        }

        // 学号格式：8-12 位纯数字
        if (username.matches("^\\d{8,12}$")) {
            return "student";
        }

        // 工号格式：T 开头 或 2-6 位数字
        if (username.startsWith("T") || username.matches("^\\d{2,6}$")) {
            return "teacher";
        }

        // 默认学生
        return "student";
    }

    /**
     * 初始化默认管理员
     */
    public void initDefaultAdmin() {
        User admin = userDao.findByUsername("admin");
        if (admin == null) {
            User newAdmin = new User();
            newAdmin.setUsername("admin");
            newAdmin.setName("系统管理员");
            newAdmin.setPassword("e10adc3949ba59abbe56e057f20f883e"); // MD5("123456")
            newAdmin.setEmail("admin@library.com");
            newAdmin.setRole("admin");
            newAdmin.setUserType("admin");
            userDao.insert(newAdmin);
        }
    }
}