# 校园图书管理系统 - 项目文档

## 项目结构

```
src/main/java/edu/wtbu/cs/book/
├── controller/          # 控制器层 (7 个 Servlet)
│   ├── AuthServlet.java         # 认证（登录/注册/登出）
│   ├── UserServlet.java         # 用户个人中心
│   ├── BookServlet.java         # 图书浏览与查询
│   ├── BorrowServlet.java       # 借阅相关
│   ├── AdminBookServlet.java    # 管理员 - 图书管理
│   ├── AdminUserServlet.java    # 管理员 - 用户管理
│   └── AdminBorrowServlet.java  # 管理员 - 借阅管理
├── service/             # 业务逻辑层
│   ├── UserService.java
│   ├── BookService.java
│   └── BorrowService.java
├── dao/                 # 数据访问层
│   ├── UserDao.java
│   ├── BookDao.java
│   └── BorrowRecordDao.java
├── entity/              # 实体类
│   ├── User.java
│   ├── Book.java
│   └── BorrowRecord.java
├── filter/              # 过滤器
│   ├── LoginFilter.java         # 登录验证过滤器
│   └── AdminFilter.java         # 管理员权限过滤器
└── util/                # 工具类
    ├── JDBCUtils.java           # JDBC 工具（Druid 连接池）
    ├── JsonUtils.java           # JSON 响应封装
    └── MD5Utils.java            # MD5 加密工具

src/main/resources/
├── druid.properties             # Druid 连接池配置
└── db.sql                       # 数据库建表脚本

src/main/webapp/
├── js/
│   └── common.js          # 公共工具模块（API/Auth/Utils）
├── user/                  # 用户端页面
│   ├── login.html         # 登录页
│   ├── register.html      # 注册页
│   ├── index-user.html    # 用户首页
│   ├── book_detail.html   # 图书详情页
│   ├── my-borrowing.html  # 我的借阅
│   └── profile.html       # 个人中心
├── admin/                 # 管理员端页面
│   ├── admin-index.html   # 管理首页
│   ├── book-manage.html   # 图书管理
│   ├── add-book.html      # 新增图书
│   ├── edit-book.html     # 编辑图书
│   ├── user-manage.html   # 用户管理
│   └── borrow-records.html # 借阅记录
└── WEB-INF/
    └── web.xml            # Web 应用配置
```

---

## API 接口

### 认证模块 `/api/auth`

| 方法 | 参数 | 功能 | 权限 |
|------|------|------|------|
| POST | `action=login` | 用户登录 | 公开 |
| POST | `action=register` | 用户注册 | 公开 |
| POST | `action=logout` | 退出登录 | 已登录 |
| GET/POST | `action=validate` | 验证 Session 有效性 | 已登录 |

### 用户模块 `/api/user`

| 方法 | 参数 | 功能 | 权限 |
|------|------|------|------|
| GET | `action=info` | 获取当前用户信息 | 已登录 |
| PUT | `action=email` | 修改邮箱 | 已登录 |
| PUT | `action=password` | 修改密码 | 已登录 |

### 图书模块 `/api/books`

| 方法 | 参数 | 功能 | 权限 |
|------|------|------|------|
| GET | `action=list` | 获取图书列表 | 已登录 |
| GET | `action=detail&id=` | 获取图书详情 | 已登录 |
| GET | `action=search&keyword=` | 搜索图书 | 已登录 |
| GET | `action=categories` | 获取分类列表 | 已登录 |

### 借阅模块 `/api/borrow`

| 方法 | 参数 | 功能 | 权限 |
|------|------|------|------|
| GET | `action=my&status=` | 我的借阅记录 | 已登录 |
| POST | `action=borrow&bookId=` | 借阅图书 | 已登录 |
| POST | `action=return&bookId=` | 归还图书 | 已登录 |

### 管理员 - 图书 `/api/admin/books`

| 方法 | 参数 | 功能 | 权限 |
|------|------|------|------|
| GET | `action=list` | 获取图书列表 | 管理员 |
| GET | `action=detail&id=` | 获取图书详情 | 管理员 |
| GET | `action=search&keyword=` | 搜索图书 | 管理员 |
| GET | `action=categories` | 获取分类列表 | 管理员 |
| POST | `action=create` | 新增图书 | 管理员 |
| POST | `action=update` | 更新图书 | 管理员 |
| POST | `action=delete` | 删除图书 | 管理员 |

### 管理员 - 用户 `/api/admin/users`

| 方法 | 参数 | 功能 | 权限 |
|------|------|------|------|
| GET | `action=list` | 获取用户列表 | 管理员 |
| POST | `action=delete` | 删除用户 | 管理员 |

### 管理员 - 借阅 `/api/admin/borrows`

| 方法 | 参数 | 功能 | 权限 |
|------|------|------|------|
| GET | `action=list` | 获取借阅记录列表 | 管理员 |
| GET | `action=overdue` | 超期借阅记录 | 管理员 |
| POST | `action=return` | 手动归还图书 | 管理员 |
| POST | `action=search` | 搜索借阅记录 | 管理员 |

---

## 业务规则

### 认证与安全
- ✅ 密码使用 MD5 加密存储
- ✅ Session 机制进行用户认证（30 分钟超时）
- ✅ 过滤器实现登录验证（`LoginFilter`）
- ✅ 管理员权限验证（`AdminFilter`）
- ✅ 过滤器白名单机制（静态资源、登录/注册页）

### 借阅规则
- ✅ 每人最多借阅 **5 本** 图书
- ✅ 借阅期限 **30 天**
- ✅ 有超期未还记录的用户不能借阅
- ✅ 已达借阅上限的用户不能借阅
- ✅ 可借复本为 0 的图书不能借阅

### 管理规则
- ✅ 有借阅记录的图书不能删除
- ✅ 有借阅在身的用户不能删除
- ✅ 管理员账号不能删除
- ✅ 更新图书信息时不修改复本数（避免借阅记录错乱）

---

## 数据库设计

### 用户表 `users`

```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    role VARCHAR(20) DEFAULT 'user',
    user_type VARCHAR(20) DEFAULT 'student',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 图书表 `books`

```sql
CREATE TABLE books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    author VARCHAR(50) NOT NULL,
    publisher VARCHAR(100),
    isbn VARCHAR(20),
    description TEXT,
    cover_url VARCHAR(255),
    category VARCHAR(50),
    total_copies INT DEFAULT 1,
    available_copies INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 借阅记录表 `borrow_records`

```sql
CREATE TABLE borrow_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    borrow_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    due_date TIMESTAMP,
    return_date TIMESTAMP,
    status VARCHAR(20) DEFAULT 'borrowing',
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (book_id) REFERENCES books(id)
);
```

---

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | HTML5 + CSS3 + 原生 JavaScript |
| 后端 | Jakarta EE Servlet 6.0 |
| 数据库 | MySQL 8.0 |
| 连接池 | Druid |
| 认证 | HttpSession + 过滤器 |
| 加密 | MD5 |
| JSON | Jackson |

---

## 核心机制

### 1. 连接释放机制
DAO 层使用 **try-with-resources** 语句自动释放数据库连接：
```java
try (Connection conn = JDBCUtils.getConnection();
     PreparedStatement pstmt = conn.prepareStatement(sql);
     ResultSet rs = pstmt.executeQuery()) {
    // 使用资源
}
// 自动关闭 ResultSet → PreparedStatement → Connection
```

### 2. Session 认证流程
1. 用户登录成功 → 服务器创建 Session → 存入 `currentUser`
2. 前端将用户信息存入 `sessionStorage`
3. 每次请求 → 过滤器检查 Session → 未登录则跳转登录页
4. 退出登录 → 清除 Session + 清除 sessionStorage → 跳转登录页

### 3. 过滤器链
- `LoginFilter`（`/*`）→ 验证所有请求的登录状态
- `AdminFilter`（`/admin/*`, `/api/admin/*`）→ 验证管理员权限

---

## 快速开始

### 1. 数据库配置
```sql
CREATE DATABASE library_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE library_db;
-- 执行 db.sql 建表脚本
-- 执行测试数据插入脚本
```

### 2. 连接池配置
修改 `src/main/resources/druid.properties`：
```properties
url=jdbc:mysql://localhost:3306/library_db?...
username=root
password=your_password
```

### 3. 启动项目
1. 将项目部署到 Tomcat
2. 访问 `http://localhost:8080/项目名称/user/login.html`

### 4. 默认测试账号
| 用户名 | 密码 | 角色 |
|--------|------|------|
| 20220104044 | 123456 | 普通用户 |
| admin | admin123 | 管理员 |

---

## 更新日志

| 日期 | 内容 |
|------|------|
| 2026-04-05 | 修复退出登录功能、修复 Session 过期重定向循环问题 |
| 2026-04-xx | 完成前后端 API 对接、修复个人中心、我的借阅页面问题 |
