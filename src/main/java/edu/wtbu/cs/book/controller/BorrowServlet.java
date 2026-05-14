package edu.wtbu.cs.book.controller;

import edu.wtbu.cs.book.entity.Book;
import edu.wtbu.cs.book.entity.BorrowRecord;
import edu.wtbu.cs.book.entity.User;
import edu.wtbu.cs.book.service.BookService;
import edu.wtbu.cs.book.service.BorrowService;
import edu.wtbu.cs.book.util.JsonUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 借阅 Servlet - 处理借阅、归还、我的借阅
 */
@WebServlet(name = "borrowServlet", value = "/api/borrow")
public class BorrowServlet extends HttpServlet {

    private final BorrowService borrowService = new BorrowService();
    private final BookService bookService = new BookService();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 设置编码
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        // 检查登录状态
        HttpSession session = req.getSession(false);
        User currentUser = session != null ? (User) session.getAttribute("currentUser") : null;
        if (currentUser == null) {
            resp.getWriter().write(JsonUtils.unauthorized("请先登录"));
            return;
        }

        String action = req.getParameter("action");
        String method = req.getMethod();

        if ("GET".equalsIgnoreCase(method)) {
            if ("my".equals(action)) {
                getMyBorrowRecords(req, resp, currentUser);
            } else if ("canBorrow".equals(action)) {
                checkCanBorrow(req, resp, currentUser);
            } else {
                resp.getWriter().write(JsonUtils.error("未知的操作"));
            }
        } else if ("POST".equalsIgnoreCase(method)) {
            if ("borrow".equals(action)) {
                borrowBook(req, resp, currentUser);
            } else if ("return".equals(action)) {
                returnBook(req, resp, currentUser);
            } else {
                resp.getWriter().write(JsonUtils.error("未知的操作"));
            }
        } else {
            resp.getWriter().write(JsonUtils.error("不支持的请求方法"));
        }
    }

    /**
     * 获取我的借阅记录
     */
    private void getMyBorrowRecords(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        try {
            String status = req.getParameter("status");
            List<BorrowRecord> records;

            if (status == null || status.isEmpty()) {
                records = borrowService.getUserBorrowRecords(user.getId());
            } else if ("borrowing".equals(status)) {
                records = borrowService.getUserBorrowingRecords(user.getId());
            } else if ("returned".equals(status)) {
                records = borrowService.getUserReturnedRecords(user.getId());
            } else {
                resp.getWriter().write(JsonUtils.error("无效的状态参数"));
                return;
            }

            resp.getWriter().write(JsonUtils.success(formatRecords(records)));
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write(JsonUtils.serverError("获取借阅记录失败：" + e.getMessage()));
        }
    }

    /**
     * 检查是否可以借阅
     */
    private void checkCanBorrow(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        try {
            boolean canBorrow = borrowService.canBorrow(user.getId());
            int borrowingCount = borrowService.getBorrowingCount(user.getId());

            Map<String, Object> result = new HashMap<>();
            result.put("canBorrow", canBorrow);
            result.put("borrowingCount", borrowingCount);
            result.put("maxLimit", 5);

            if (!canBorrow) {
                if (borrowingCount >= 5) {
                    result.put("reason", "已达借阅上限（5 本）");
                } else {
                    result.put("reason", "有超期未还图书记录");
                }
            }

            resp.getWriter().write(JsonUtils.success(result));
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write(JsonUtils.serverError("检查借阅资格失败：" + e.getMessage()));
        }
    }

    /**
     * 借阅图书
     */
    private void borrowBook(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
        try {
            // 读取 JSON 请求体
            Map<String, String> params = readJsonBody(req);
            String bookIdStr = params.get("bookId");

            if (bookIdStr == null || bookIdStr.isEmpty()) {
                resp.getWriter().write(JsonUtils.error("缺少图书 ID"));
                return;
            }

            Integer bookId = Integer.parseInt(bookIdStr);

            // 调用服务层
            int result = borrowService.borrow(currentUser.getId(), bookId);

            switch (result) {
                case 1:
                    resp.getWriter().write(JsonUtils.success("借阅成功", null));
                    break;
                case 2:
                    resp.getWriter().write(JsonUtils.error("已达借阅上限（5 本）"));
                    break;
                case 3:
                    resp.getWriter().write(JsonUtils.error("有超期未还图书记录，请先归还"));
                    break;
                case 4:
                    resp.getWriter().write(JsonUtils.error("该图书暂无可借复本"));
                    break;
                default:
                    resp.getWriter().write(JsonUtils.error("借阅失败，请稍后重试"));
            }
        } catch (NumberFormatException e) {
            resp.getWriter().write(JsonUtils.error("无效的图书 ID"));
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write(JsonUtils.serverError("借阅失败：" + e.getMessage()));
        }
    }

    /**
     * 归还图书
     */
    private void returnBook(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
        try {
            // 读取 JSON 请求体
            Map<String, String> params = readJsonBody(req);
            String bookIdStr = params.get("bookId");

            if (bookIdStr == null || bookIdStr.isEmpty()) {
                resp.getWriter().write(JsonUtils.error("缺少图书 ID"));
                return;
            }

            Integer bookId = Integer.parseInt(bookIdStr);

            int result = borrowService.returnBook(currentUser.getId(), bookId);
            if (result > 0) {
                resp.getWriter().write(JsonUtils.success("归还成功", null));
            } else {
                resp.getWriter().write(JsonUtils.error("归还失败，未找到该借阅记录"));
            }
        } catch (NumberFormatException e) {
            resp.getWriter().write(JsonUtils.error("无效的图书 ID"));
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write(JsonUtils.serverError("归还失败：" + e.getMessage()));
        }
    }

    /**
     * 格式化借阅记录列表
     */
    private List<Map<String, Object>> formatRecords(List<BorrowRecord> records) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (BorrowRecord record : records) {
            list.add(formatRecord(record));
        }
        return list;
    }

    /**
     * 格式化借阅记录
     */
    private Map<String, Object> formatRecord(BorrowRecord record) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", record.getId());
        map.put("bookId", record.getBookId());

        // 图书信息
        if (record.getBook() != null) {
            map.put("bookTitle", record.getBook().getTitle());
        } else {
            Book book = bookService.getBookById(record.getBookId());
            if (book != null) {
                map.put("bookTitle", book.getTitle());
            }
        }

        map.put("borrowDate", record.getBorrowDate() != null ? record.getBorrowDate().toString() : null);
        map.put("dueDate", record.getDueDate() != null ? record.getDueDate().toString() : null);
        map.put("returnDate", record.getReturnDate() != null ? record.getReturnDate().toString() : null);
        map.put("status", record.getStatus());
        map.put("isOverdue", record.isOverdue());
        map.put("overdueDays", record.getOverdueDays());
        return map;
    }

    /**
     * 读取 JSON 请求体
     */
    private Map<String, String> readJsonBody(HttpServletRequest req) throws IOException {
        Map<String, String> params = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = req.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        String json = sb.toString();

        // 简单解析 JSON
        if (json != null && !json.isEmpty()) {
            json = json.trim();
            if (json.startsWith("{") && json.endsWith("}")) {
                json = json.substring(1, json.length() - 1);
                String[] pairs = json.split(",");
                for (String pair : pairs) {
                    String[] keyValue = pair.split(":");
                    if (keyValue.length == 2) {
                        String key = keyValue[0].trim().replace("\"", "");
                        String value = keyValue[1].trim().replace("\"", "");
                        params.put(key, value);
                    }
                }
            }
        }
        return params;
    }
}