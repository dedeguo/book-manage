# 校园图书管理系统 - API 接口文档

> | 项目 | 说明 |
> |------|------|
> | 基础路径 | `/api` |
> | 返回格式 | `application/json; charset=UTF-8` |
> | 字符编码 | `UTF-8` |

---

## 一、通用响应格式

### 成功响应
```json
{
  "code": 200,
  "message": "操作成功",
  "data": { }
}
```

### 错误响应
```json
{
  "code": 400,
  "message": "错误描述",
  "data": null
}
```

### 状态码说明

| 状态码 | 说明 | 场景 |
|:------:|------|------|
| `200` | 请求成功 | 正常返回 |
| `400` | 请求参数错误 | 参数缺失或格式错误 |
| `401` | 未登录/未授权 | Session 过期或未登录 |
| `403` | 禁止访问 | 权限不足（如普通用户访问管理员接口） |
| `404` | 资源不存在 | 图书、用户等不存在 |
| `500` | 服务器内部错误 | 系统异常 |

---

## 二、认证模块 `/api/auth`

### 2.1 用户登录

| 项目 | 说明 |
|------|------|
| **请求 URL** | `/api/auth?action=login` |
| **请求方法** | `POST` |
| **权限** | 公开 |

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|:-----|:-----|:----:|------|
| `username` | String | 是 | 用户名（学号/工号） |
| `password` | String | 是 | 密码（明文传输，后端 MD5 加密） |

**请求示例**
```json
{
  "username": "20220104044",
  "password": "123456"
}
```

**返回参数**

| 参数 | 类型 | 说明 |
|:-----|:-----|------|
| `id` | Long | 用户 ID |
| `username` | String | 用户名 |
| `name` | String | 姓名 |
| `role` | String | 角色（`user` / `admin`） |
| `userType` | String | 用户类型（`student` / `teacher`） |
| `email` | String | 邮箱 |

**返回示例**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "id": 1,
    "username": "20220104044",
    "name": "陈德",
    "role": "user",
    "userType": "student",
    "email": "chende@wbu.edu.cn"
  }
}
```

---

### 2.2 用户注册

| 项目 | 说明 |
|------|------|
| **请求 URL** | `/api/auth?action=register` |
| **请求方法** | `POST` |
| **权限** | 公开 |

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|:-----|:-----|:----:|------|
| `username` | String | 是 | 用户名（学号/工号，必须唯一） |
| `name` | String | 是 | 姓名 |
| `password` | String | 是 | 密码 |
| `email` | String | 是 | 邮箱地址 |

**请求示例**
```json
{
  "username": "20230101001",
  "name": "张三",
  "password": "123456",
  "email": "zhangsan@wbu.edu.cn"
}
```

**返回示例**
```json
{
  "code": 200,
  "message": "注册成功",
  "data": null
}
```

---

### 2.3 退出登录

| 项目 | 说明 |
|------|------|
| **请求 URL** | `/api/auth?action=logout` |
| **请求方法** | `POST` |
| **权限** | 已登录用户 |

**请求参数** 无

**返回示例**
```json
{
  "code": 200,
  "message": "退出成功",
  "data": null
}
```

---

### 2.4 验证 Session

| 项目 | 说明 |
|------|------|
| **请求 URL** | `/api/auth?action=validate` |
| **请求方法** | `GET` 或 `POST` |
| **权限** | 已登录用户 |

**请求参数** 无

**返回参数**

| 参数 | 类型 | 说明 |
|:-----|:-----|------|
| `id` | Long | 用户 ID |
| `username` | String | 用户名 |
| `name` | String | 姓名 |
| `role` | String | 角色 |
| `userType` | String | 用户类型 |

**返回示例**
```json
{
  "code": 200,
  "message": "Session 有效",
  "data": {
    "id": 1,
    "username": "20220104044",
    "name": "陈德",
    "role": "user",
    "userType": "student"
  }
}
```

---

## 三、用户模块 `/api/user`

### 3.1 获取个人信息

| 项目 | 说明 |
|------|------|
| **请求 URL** | `/api/user?action=info` |
| **请求方法** | `GET` |
| **权限** | 已登录用户 |

**请求参数** 无

**返回参数**

| 参数 | 类型 | 说明 |
|:-----|:-----|------|
| `id` | Long | 用户 ID |
| `username` | String | 用户名 |
| `name` | String | 姓名 |
| `email` | String | 邮箱 |
| `role` | String | 角色 |
| `userType` | String | 用户类型 |
| `createdAt` | String | 注册时间 |

**返回示例**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "username": "20220104044",
    "name": "陈德",
    "email": "chende@wbu.edu.cn",
    "role": "user",
    "userType": "student",
    "createdAt": "2026-03-21 14:30:00"
  }
}
```

---

### 3.2 修改密码

| 项目 | 说明 |
|------|------|
| **请求 URL** | `/api/user?action=password` |
| **请求方法** | `POST` 或 `PUT` |
| **权限** | 已登录用户 |

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|:-----|:-----|:----:|------|
| `oldPassword` | String | 是 | 原密码 |
| `newPassword` | String | 是 | 新密码（至少 6 位） |

**请求示例**
```json
{
  "oldPassword": "123456",
  "newPassword": "new123"
}
```

**返回示例**
```json
{
  "code": 200,
  "message": "密码修改成功",
  "data": null
}
```

---

### 3.3 修改邮箱

| 项目 | 说明 |
|------|------|
| **请求 URL** | `/api/user?action=email` |
| **请求方法** | `POST` 或 `PUT` |
| **权限** | 已登录用户 |

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|:-----|:-----|:----:|------|
| `newEmail` | String | 是 | 新邮箱地址 |

**请求示例**
```json
{
  "newEmail": "newemail@wbu.edu.cn"
}
```

**返回示例**
```json
{
  "code": 200,
  "message": "邮箱修改成功",
  "data": null
}
```

---

## 四、图书模块 `/api/books`

### 4.1 获取图书列表

| 项目 | 说明 |
|------|------|
| **请求 URL** | `/api/books?action=list` |
| **请求方法** | `GET` |
| **权限** | 已登录用户 |

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|:-----|:-----|:----:|------|
| `category` | String | 否 | 图书分类筛选 |

**返回参数**

| 参数 | 类型 | 说明 |
|:-----|:-----|------|
| `id` | Long | 图书 ID |
| `title` | String | 书名 |
| `author` | String | 作者 |
| `publisher` | String | 出版社 |
| `isbn` | String | ISBN 编号 |
| `description` | String | 简介 |
| `coverUrl` | String | 封面图片 URL |
| `category` | String | 分类 |
| `totalCopies` | Integer | 总复本数 |
| `availableCopies` | Integer | 可借复本数 |
| `isAvailable` | Boolean | 是否可借 |
| `createdAt` | String | 入库时间 |

**返回示例**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": 1,
      "title": "Java 编程思想",
      "author": "Bruce Eckel",
      "publisher": "机械工业出版社",
      "isbn": "9787111213826",
      "description": "Java 经典教材",
      "coverUrl": "http://example.com/cover.jpg",
      "category": "计算机/互联网",
      "totalCopies": 5,
      "availableCopies": 3,
      "isAvailable": true,
      "createdAt": "2026-03-01 10:00:00"
    }
  ]
}
```

---

### 4.2 获取图书详情

| 项目 | 说明 |
|------|------|
| **请求 URL** | `/api/books?action=detail&id={id}` |
| **请求方法** | `GET` |
| **权限** | 已登录用户 |

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|:-----|:-----|:----:|------|
| `id` | Integer | 是 | 图书 ID |

**返回参数** 同 4.1

**返回示例**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "title": "Java 编程思想",
    "author": "Bruce Eckel",
    "publisher": "机械工业出版社",
    "isbn": "9787111213826",
    "description": "Java 经典教材",
    "coverUrl": "http://example.com/cover.jpg",
    "category": "计算机/互联网",
    "totalCopies": 5,
    "availableCopies": 3,
    "isAvailable": true,
    "createdAt": "2026-03-01 10:00:00"
  }
}
```

---

### 4.3 搜索图书

| 项目 | 说明 |
|------|------|
| **请求 URL** | `/api/books?action=search&keyword={keyword}` |
| **请求方法** | `GET` |
| **权限** | 已登录用户 |

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|:-----|:-----|:----:|------|
| `keyword` | String | 是 | 搜索关键词（书名/作者） |

**返回参数** 图书列表（同 4.1）

**返回示例**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": 1,
      "title": "Java 编程思想",
      "author": "Bruce Eckel",
      "publisher": "机械工业出版社",
      "isbn": "9787111213826",
      "description": "Java 经典教材",
      "coverUrl": "http://example.com/cover.jpg",
      "category": "计算机/互联网",
      "totalCopies": 5,
      "availableCopies": 3,
      "isAvailable": true,
      "createdAt": "2026-03-01 10:00:00"
    }
  ]
}
```

---

### 4.4 获取分类列表

| 项目 | 说明 |
|------|------|
| **请求 URL** | `/api/books?action=categories` |
| **请求方法** | `GET` |
| **权限** | 已登录用户 |

**请求参数** 无

**返回参数** 字符串数组

**返回示例**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    "计算机/互联网",
    "文学/小说",
    "教材/教辅",
    "历史/地理",
    "其他"
  ]
}
```

---

## 五、借阅模块 `/api/borrow`

### 5.1 获取我的借阅记录

| 项目 | 说明 |
|------|------|
| **请求 URL** | `/api/borrow?action=my` |
| **请求方法** | `GET` |
| **权限** | 已登录用户 |

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|:-----|:-----|:----:|------|
| `status` | String | 否 | 借阅状态（`borrowing` / `returned`，不传为全部） |

**返回参数**

| 参数 | 类型 | 说明 |
|:-----|:-----|------|
| `id` | Long | 借阅记录 ID |
| `bookId` | Long | 图书 ID |
| `bookTitle` | String | 书名 |
| `borrowDate` | String | 借阅日期 |
| `dueDate` | String | 应还日期 |
| `returnDate` | String | 归还日期（未还为 null） |
| `status` | String | 状态（`borrowing` / `returned`） |
| `isOverdue` | Boolean | 是否超期 |
| `overdueDays` | Integer | 超期天数 |

**返回示例**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": 1,
      "bookId": 1,
      "bookTitle": "Java 编程思想",
      "borrowDate": "2026-03-25 10:00:00",
      "dueDate": "2026-04-24 10:00:00",
      "returnDate": null,
      "status": "borrowing",
      "isOverdue": false,
      "overdueDays": 0
    }
  ]
}
```

---

### 5.2 检查借阅资格

| 项目 | 说明 |
|------|------|
| **请求 URL** | `/api/borrow?action=canBorrow` |
| **请求方法** | `GET` |
| **权限** | 已登录用户 |

**请求参数** 无

**返回参数**

| 参数 | 类型 | 说明 |
|:-----|:-----|------|
| `canBorrow` | Boolean | 是否可以借阅 |
| `borrowingCount` | Integer | 当前借阅数量 |
| `maxLimit` | Integer | 最大借阅限制（5 本） |
| `reason` | String | 不能借阅的原因 |

**返回示例（可借）**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "canBorrow": true,
    "borrowingCount": 2,
    "maxLimit": 5,
    "reason": null
  }
}
```

**返回示例（不可借）**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "canBorrow": false,
    "borrowingCount": 5,
    "maxLimit": 5,
    "reason": "已达借阅上限（5 本）"
  }
}
```

---

### 5.3 借阅图书

| 项目 | 说明 |
|------|------|
| **请求 URL** | `/api/borrow?action=borrow` |
| **请求方法** | `POST` |
| **权限** | 已登录用户 |

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|:-----|:-----|:----:|------|
| `bookId` | Integer | 是 | 图书 ID |

**请求示例**
```json
{
  "bookId": 1
}
```

**返回示例（成功）**
```json
{
  "code": 200,
  "message": "借阅成功",
  "data": null
}
```

**返回示例（错误）**
```json
{
  "code": 400,
  "message": "已达借阅上限（5 本）",
  "data": null
}
```

**错误码说明**

| 返回信息 | 说明 |
|----------|------|
| `借阅成功` | 成功 |
| `已达借阅上限（5 本）` | 超过最大借阅数 |
| `有超期未还图书记录，请先归还` | 有超期记录 |
| `该图书暂无可借复本` | 图书已全部借出 |

---

### 5.4 归还图书

| 项目 | 说明 |
|------|------|
| **请求 URL** | `/api/borrow?action=return` |
| **请求方法** | `POST` |
| **权限** | 已登录用户 |

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|:-----|:-----|:----:|------|
| `bookId` | Integer | 是 | 图书 ID |

**请求示例**
```json
{
  "bookId": 1
}
```

**返回示例（成功）**
```json
{
  "code": 200,
  "message": "归还成功",
  "data": null
}
```

---

## 六、管理员 - 图书 `/api/admin/books`

### 6.1 获取图书列表

| 项目 | 说明 |
|------|------|
| **请求 URL** | `/api/admin/books?action=list` |
| **请求方法** | `GET` |
| **权限** | 管理员 |

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|:-----|:-----|:----:|------|
| `category` | String | 否 | 分类筛选 |

**返回参数** 同 4.1

---

### 6.2 获取图书详情

| 项目 | 说明 |
|------|------|
| **请求 URL** | `/api/admin/books?action=detail&id={id}` |
| **请求方法** | `GET` |
| **权限** | 管理员 |

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|:-----|:-----|:----:|------|
| `id` | Integer | 是 | 图书 ID |

**返回参数** 同 4.1

---

### 6.3 搜索图书

| 项目 | 说明 |
|------|------|
| **请求 URL** | `/api/admin/books?action=search` |
| **请求方法** | `GET` |
| **权限** | 管理员 |

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|:-----|:-----|:----:|------|
| `keyword` | String | 否 | 搜索关键词 |
| `category` | String | 否 | 分类筛选 |

**返回参数** 图书列表（同 4.1）

---

### 6.4 获取分类列表

| 项目 | 说明 |
|------|------|
| **请求 URL** | `/api/admin/books?action=categories` |
| **请求方法** | `GET` |
| **权限** | 管理员 |

**请求参数** 无

**返回参数** 分类字符串数组

---

### 6.5 新增图书

| 项目 | 说明 |
|------|------|
| **请求 URL** | `/api/admin/books?action=create` |
| **请求方法** | `POST` |
| **权限** | 管理员 |

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|:-----|:-----|:----:|------|
| `title` | String | 是 | 书名 |
| `author` | String | 是 | 作者 |
| `publisher` | String | 是 | 出版社 |
| `isbn` | String | 是 | ISBN 编号 |
| `description` | String | 否 | 简介 |
| `coverUrl` | String | 否 | 封面 URL |
| `category` | String | 是 | 分类 |
| `totalCopies` | Integer | 否 | 复本数（默认 1） |

**请求示例**
```json
{
  "title": "Java 编程思想",
  "author": "Bruce Eckel",
  "publisher": "机械工业出版社",
  "isbn": "9787111213826",
  "description": "Java 经典教材",
  "coverUrl": "https://example.com/cover.jpg",
  "category": "计算机/互联网",
  "totalCopies": 3
}
```

**返回示例**
```json
{
  "code": 200,
  "message": "图书添加成功",
  "data": null
}
```

---

### 6.6 更新图书

| 项目 | 说明 |
|------|------|
| **请求 URL** | `/api/admin/books?action=update` |
| **请求方法** | `POST` |
| **权限** | 管理员 |

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|:-----|:-----|:----:|------|
| `id` | Integer | 是 | 图书 ID |
| `title` | String | 是 | 书名 |
| `author` | String | 是 | 作者 |
| `publisher` | String | 是 | 出版社 |
| `isbn` | String | 是 | ISBN 编号 |
| `description` | String | 否 | 简介 |
| `coverUrl` | String | 否 | 封面 URL |
| `category` | String | 是 | 分类 |

> **注意**: 不能修改 `totalCopies` 和 `availableCopies`，避免借阅记录错乱

**请求示例**
```json
{
  "id": 1,
  "title": "Java 编程思想（第 4 版）",
  "author": "Bruce Eckel",
  "publisher": "机械工业出版社",
  "isbn": "9787111213826",
  "description": "Java 经典教材，第 4 版",
  "coverUrl": "https://example.com/cover.jpg",
  "category": "计算机/互联网"
}
```

**返回示例**
```json
{
  "code": 200,
  "message": "图书更新成功",
  "data": null
}
```

---

### 6.7 删除图书

| 项目 | 说明 |
|------|------|
| **请求 URL** | `/api/admin/books?action=delete` |
| **请求方法** | `POST` |
| **权限** | 管理员 |

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|:-----|:-----|:----:|------|
| `id` | Integer | 是 | 图书 ID |

**请求示例**
```json
{
  "id": 1
}
```

**返回示例（成功）**
```json
{
  "code": 200,
  "message": "图书删除成功",
  "data": null
}
```

**返回示例（失败）**
```json
{
  "code": 400,
  "message": "该图书有借阅记录，无法删除",
  "data": null
}
```

---

## 七、管理员 - 用户 `/api/admin/users`

### 7.1 获取用户列表

| 项目 | 说明 |
|------|------|
| **请求 URL** | `/api/admin/users?action=list` |
| **请求方法** | `GET` |
| **权限** | 管理员 |

**请求参数** 无

**返回参数**

| 参数 | 类型 | 说明 |
|:-----|:-----|------|
| `id` | Long | 用户 ID |
| `username` | String | 用户名（学号/工号） |
| `name` | String | 姓名 |
| `email` | String | 邮箱 |
| `role` | String | 角色（`user` / `admin`） |
| `userType` | String | 用户类型（`student` / `teacher`） |
| `createdAt` | String | 注册时间 |

**返回示例**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": 1,
      "username": "20220104044",
      "name": "陈德",
      "email": "chende@wbu.edu.cn",
      "role": "user",
      "userType": "student",
      "createdAt": "2026-03-21 14:30:00"
    },
    {
      "id": 2,
      "username": "admin",
      "name": "系统管理员",
      "email": "admin@wbu.edu.cn",
      "role": "admin",
      "userType": "teacher",
      "createdAt": "2025-11-20 08:00:00"
    }
  ]
}
```

---

### 7.2 搜索用户

| 项目 | 说明 |
|------|------|
| **请求 URL** | `/api/admin/users?action=search` |
| **请求方法** | `GET` |
| **权限** | 管理员 |

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|:-----|:-----|:----:|------|
| `username` | String | 否 | 学号/工号 |
| `name` | String | 否 | 姓名 |

**返回参数** 用户列表（同 7.1）

---

### 7.3 删除用户

| 项目 | 说明 |
|------|------|
| **请求 URL** | `/api/admin/users?action=delete` |
| **请求方法** | `POST` |
| **权限** | 管理员 |

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|:-----|:-----|:----:|------|
| `id` | Integer | 是 | 用户 ID |

**请求示例**
```json
{
  "id": 1
}
```

**返回示例（成功）**
```json
{
  "code": 200,
  "message": "用户删除成功",
  "data": null
}
```

**返回示例（失败）**
```json
{
  "code": 400,
  "message": "管理员账号不能删除",
  "data": null
}
```

---

## 八、管理员 - 借阅 `/api/admin/borrows`

### 8.1 获取借阅记录列表

| 项目 | 说明 |
|------|------|
| **请求 URL** | `/api/admin/borrows?action=list` |
| **请求方法** | `GET` |
| **权限** | 管理员 |

**请求参数** 无

**返回参数**

| 参数 | 类型 | 说明 |
|:-----|:-----|------|
| `id` | Long | 借阅记录 ID |
| `userId` | Long | 用户 ID |
| `username` | String | 用户名 |
| `userName` | String | 姓名 |
| `bookId` | Long | 图书 ID |
| `bookTitle` | String | 书名 |
| `borrowDate` | String | 借阅日期 |
| `dueDate` | String | 应还日期 |
| `returnDate` | String | 归还日期 |
| `status` | String | 状态 |
| `isOverdue` | Boolean | 是否超期 |
| `overdueDays` | Integer | 超期天数 |

**返回示例**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "username": "20220104044",
      "userName": "陈德",
      "bookId": 1,
      "bookTitle": "Java 编程思想",
      "borrowDate": "2026-03-25 10:00:00",
      "dueDate": "2026-04-24 10:00:00",
      "returnDate": null,
      "status": "borrowing",
      "isOverdue": false,
      "overdueDays": 0
    }
  ]
}
```

---

### 8.2 搜索借阅记录

| 项目 | 说明 |
|------|------|
| **请求 URL** | `/api/admin/borrows?action=search` |
| **请求方法** | `GET` |
| **权限** | 管理员 |

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|:-----|:-----|:----:|------|
| `username` | String | 否 | 学号/工号 |
| `name` | String | 否 | 姓名 |
| `book` | String | 否 | 书名 |
| `status` | String | 否 | 状态（`borrowing` / `returned`） |

**返回参数** 借阅记录列表（同 8.1）

---

### 8.3 获取超期借阅记录

| 项目 | 说明 |
|------|------|
| **请求 URL** | `/api/admin/borrows?action=overdue` |
| **请求方法** | `GET` |
| **权限** | 管理员 |

**请求参数** 无

**返回参数** 超期借阅记录列表（同 8.1）

**返回示例**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": 2,
      "userId": 3,
      "username": "20210101001",
      "userName": "李四",
      "bookId": 2,
      "bookTitle": "数据结构",
      "borrowDate": "2026-02-01 10:00:00",
      "dueDate": "2026-03-02 10:00:00",
      "returnDate": null,
      "status": "borrowing",
      "isOverdue": true,
      "overdueDays": 34
    }
  ]
}
```

---

### 8.4 管理员代为归还

| 项目 | 说明 |
|------|------|
| **请求 URL** | `/api/admin/borrows?action=return` |
| **请求方法** | `POST` |
| **权限** | 管理员 |

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|:-----|:-----|:----:|------|
| `recordId` | Integer | 是 | 借阅记录 ID |

**请求示例**
```json
{
  "recordId": 1
}
```

**返回示例**
```json
{
  "code": 200,
  "message": "归还成功",
  "data": null
}
```

---

## 附录：业务规则

### 借阅限制

| 规则 | 说明 |
|------|------|
| 借阅上限 | 每人最多借阅 **5 本** 图书 |
| 借阅期限 | 借阅期限 **30 天** |
| 超期限制 | 有超期未还记录的用户不能借阅 |
| 复本限制 | 可借复本为 0 的图书不能借阅 |

### 删除限制

| 规则 | 说明 |
|------|------|
| 图书删除 | 有借阅记录的图书不能删除 |
| 用户删除 | 有借阅在身的用户不能删除 |
| 管理员账号 | 管理员账号不能删除 |

### Session 机制

| 项目 | 说明 |
|------|------|
| 超时时间 | **30 分钟** |
| 存储方式 | 登录后前端将用户信息存入 `sessionStorage` |
| 验证方式 | 每次请求通过过滤器验证 Session 有效性 |
