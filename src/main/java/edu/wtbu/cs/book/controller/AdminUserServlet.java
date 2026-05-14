package edu.wtbu.cs.book.controller;

import edu.wtbu.cs.book.entity.User;
import edu.wtbu.cs.book.service.BorrowService;
import edu.wtbu.cs.book.service.UserService;
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
 * 管理员用户 Servlet - 处理用户管理（列表、删除）
 */
@WebServlet(name = "adminUserServlet", value = "/api/admin/users")
public class AdminUserServlet extends HttpServlet {

    private final UserService userService = new UserService();
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
                listUsers(req, resp);
            } else if ("search".equals(action)) {
                searchUsers(req, resp);
            } else {
                listUsers(req, resp);
            }
        } else if ("POST".equalsIgnoreCase(method)) {
            if ("delete".equals(action)) {
                deleteUser(req, resp);
            } else {
                resp.getWriter().write(JsonUtils.error("未知的操作"));
            }
        } else {
            resp.getWriter().write(JsonUtils.error("不支持的请求方法"));
        }
    }

    /**
     * 获取用户列表
     */
    private void listUsers(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<User> users = userService.getAllUsers();
            resp.getWriter().write(JsonUtils.success(formatUsers(users)));
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write(JsonUtils.serverError("获取用户列表失败：" + e.getMessage()));
        }
    }

    /**
     * 搜索用户
     */
    private void searchUsers(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String username = req.getParameter("username");
            String name = req.getParameter("name");

            List<User> users;
            if (username != null && !username.isEmpty()) {
                users = userService.searchUsers(username);
            } else if (name != null && !name.isEmpty()) {
                users = userService.searchUsers(name);
            } else {
                users = userService.getAllUsers();
            }

            resp.getWriter().write(JsonUtils.success(formatUsers(users)));
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write(JsonUtils.serverError("搜索用户失败：" + e.getMessage()));
        }
    }

    /**
     * 删除用户
     */
    private void deleteUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Map<String, String> params = readJsonBody(req);
            String idStr = getParam(params, "id");

            if (idStr == null || idStr.isEmpty()) {
                resp.getWriter().write(JsonUtils.error("缺少用户 ID"));
                return;
            }

            Integer id = Integer.parseInt(idStr);
            User user = userService.getUserById(id);

            if (user == null) {
                resp.getWriter().write(JsonUtils.notFound("用户不存在"));
                return;
            }

            // 管理员账号不能删除
            if ("admin".equals(user.getRole())) {
                resp.getWriter().write(JsonUtils.error("管理员账号不能删除"));
                return;
            }

            // 检查是否有借阅在身的用户
            if (borrowService.getBorrowingCount(id) > 0) {
                resp.getWriter().write(JsonUtils.error("该用户有借阅中的图书，无法删除"));
                return;
            }

            boolean success = userService.deleteUser(id);
            if (success) {
                resp.getWriter().write(JsonUtils.success("用户删除成功", null));
            } else {
                resp.getWriter().write(JsonUtils.error("用户删除失败"));
            }
        } catch (NumberFormatException e) {
            resp.getWriter().write(JsonUtils.error("无效的用户 ID"));
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write(JsonUtils.serverError("删除用户失败：" + e.getMessage()));
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
     * 格式化用户列表
     */
    private List<Map<String, Object>> formatUsers(List<User> users) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (User user : users) {
            list.add(formatUser(user));
        }
        return list;
    }

    /**
     * 格式化用户对象
     */
    private Map<String, Object> formatUser(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("name", user.getName());
        map.put("email", user.getEmail());
        map.put("role", user.getRole());
        map.put("userType", user.getUserType());
        map.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        
        // 添加借阅状态信息（管理员账号除外）
        if (!"admin".equals(user.getRole())) {
            int borrowingCount = borrowService.getBorrowingCount(user.getId());
            map.put("hasBorrowing", borrowingCount > 0);
            map.put("borrowingCount", borrowingCount);
        } else {
            map.put("hasBorrowing", false);
            map.put("borrowingCount", 0);
        }
        
        // 不返回密码
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