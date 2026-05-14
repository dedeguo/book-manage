package edu.wtbu.cs.book.controller;

import edu.wtbu.cs.book.entity.BorrowRecord;
import edu.wtbu.cs.book.entity.User;
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
 * 管理员借阅 Servlet - 处理借阅记录管理、手动归还
 */
@WebServlet(name = "adminBorrowServlet", value = "/api/admin/borrows")
public class AdminBorrowServlet extends HttpServlet {

    private final BorrowService borrowService = new BorrowService();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 设置编码
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        // 检查管理员登录状态
        HttpSession session = req.getSession(false);
        User currentUser = session != null ? (User) session.getAttribute("currentUser") : null;
        if (currentUser == null || !currentUser.isAdmin()) {
            resp.getWriter().write(JsonUtils.forbidden("需要管理员权限"));
            return;
        }

        String action = req.getParameter("action");
        String method = req.getMethod();

        if ("GET".equalsIgnoreCase(method)) {
            if ("list".equals(action)) {
                listRecords(req, resp);
            } else if ("search".equals(action)) {
                searchRecords(req, resp);
            } else if ("overdue".equals(action)) {
                getOverdueRecords(req, resp);
            } else {
                listRecords(req, resp);
            }
        } else if ("POST".equalsIgnoreCase(method)) {
            if ("return".equals(action)) {
                adminReturn(req, resp);
            } else {
                resp.getWriter().write(JsonUtils.error("未知的操作"));
            }
        } else {
            resp.getWriter().write(JsonUtils.error("不支持的请求方法"));
        }
    }

    /**
     * 获取借阅记录列表
     */
    private void listRecords(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<BorrowRecord> records = borrowService.getAllRecords();
            resp.getWriter().write(JsonUtils.success(formatRecords(records)));
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write(JsonUtils.serverError("获取借阅记录失败：" + e.getMessage()));
        }
    }

    /**
     * 搜索借阅记录
     */
    private void searchRecords(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String username = req.getParameter("username");
            String name = req.getParameter("name");
            String book = req.getParameter("book");
            String status = req.getParameter("status");

            List<BorrowRecord> records = borrowService.searchRecords(username, name, book, status);
            resp.getWriter().write(JsonUtils.success(formatRecords(records)));
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write(JsonUtils.serverError("搜索借阅记录失败：" + e.getMessage()));
        }
    }

    /**
     * 获取超期借阅记录
     */
    private void getOverdueRecords(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<BorrowRecord> records = borrowService.getOverdueRecords();
            resp.getWriter().write(JsonUtils.success(formatRecords(records)));
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write(JsonUtils.serverError("获取超期记录失败：" + e.getMessage()));
        }
    }

    /**
     * 管理员代为归还
     */
    private void adminReturn(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Map<String, String> params = readJsonBody(req);
            String recordIdStr = getParam(params, "recordId");

            if (recordIdStr == null || recordIdStr.isEmpty()) {
                resp.getWriter().write(JsonUtils.error("缺少借阅记录 ID"));
                return;
            }

            Integer recordId = Integer.parseInt(recordIdStr);
            int result = borrowService.adminReturn(recordId);

            if (result > 0) {
                resp.getWriter().write(JsonUtils.success("归还成功", null));
            } else {
                resp.getWriter().write(JsonUtils.error("归还失败，记录不存在或已归还"));
            }
        } catch (NumberFormatException e) {
            resp.getWriter().write(JsonUtils.error("无效的记录 ID"));
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write(JsonUtils.serverError("归还失败：" + e.getMessage()));
        }
    }

    /**
     * 从 Map 中获取参数
     */
    private String getParam(Map<String, String> params, String key) {
        String value = params.get(key);
        return value != null && !value.isEmpty() ? value : null;
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

        // 用户信息
        if (record.getUser() != null) {
            map.put("userId", record.getUser().getId());
            map.put("username", record.getUser().getUsername());
            map.put("userName", record.getUser().getName());
        }

        // 图书信息
        if (record.getBook() != null) {
            map.put("bookId", record.getBook().getId());
            map.put("bookTitle", record.getBook().getTitle());
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