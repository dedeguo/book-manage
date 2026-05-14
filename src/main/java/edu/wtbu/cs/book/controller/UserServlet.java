package edu.wtbu.cs.book.controller;

import edu.wtbu.cs.book.entity.User;
import edu.wtbu.cs.book.service.UserService;
import edu.wtbu.cs.book.util.JsonUtils;
import edu.wtbu.cs.book.util.MD5Utils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户 Servlet - 处理个人信息、修改密码、修改邮箱
 */
@WebServlet(name = "userServlet", value = "/api/user")
public class UserServlet extends HttpServlet {

    private final UserService userService = new UserService();

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
            if ("info".equals(action)) {
                getInfo(req, resp, currentUser);
            } else {
                resp.getWriter().write(JsonUtils.error("未知的操作"));
            }
        } else if ("PUT".equalsIgnoreCase(method) || "POST".equalsIgnoreCase(method)) {
            if ("password".equals(action)) {
                changePassword(req, resp, currentUser);
            } else if ("email".equals(action)) {
                changeEmail(req, resp, currentUser);
            } else {
                resp.getWriter().write(JsonUtils.error("未知的操作"));
            }
        } else {
            resp.getWriter().write(JsonUtils.error("不支持的请求方法"));
        }
    }

    /**
     * 获取个人信息
     */
    private void getInfo(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("name", user.getName());
        userInfo.put("email", user.getEmail());
        userInfo.put("role", user.getRole());
        userInfo.put("userType", user.getUserType());
        userInfo.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        resp.getWriter().write(JsonUtils.success(userInfo));
    }

    /**
     * 修改密码
     */
    private void changePassword(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
        try {
            // 读取 JSON 请求体
            Map<String, String> params = readJsonBody(req);
            String oldPassword = params.get("oldPassword");
            String newPassword = params.get("newPassword");

            // 参数校验
            if (oldPassword == null || oldPassword.isEmpty() || newPassword == null || newPassword.isEmpty()) {
                resp.getWriter().write(JsonUtils.error("所有字段均为必填"));
                return;
            }

            if (newPassword.length() < 6) {
                resp.getWriter().write(JsonUtils.error("新密码长度不能少于 6 位"));
                return;
            }

            // 验证旧密码并更新
            boolean success = userService.changePassword(currentUser.getId(),
                    MD5Utils.md5(oldPassword), MD5Utils.md5(newPassword));
            if (success) {
                resp.getWriter().write(JsonUtils.success("密码修改成功", null));
            } else {
                resp.getWriter().write(JsonUtils.error("原密码错误"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write(JsonUtils.serverError("修改密码失败：" + e.getMessage()));
        }
    }

    /**
     * 修改邮箱
     */
    private void changeEmail(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
        try {
            // 读取 JSON 请求体
            Map<String, String> params = readJsonBody(req);
            String newEmail = params.get("newEmail");

            // 参数校验
            if (newEmail == null || newEmail.isEmpty()) {
                resp.getWriter().write(JsonUtils.error("新邮箱不能为空"));
                return;
            }

            // 邮箱格式校验
            if (!newEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                resp.getWriter().write(JsonUtils.error("邮箱格式不正确"));
                return;
            }

            boolean success = userService.changeEmail(currentUser.getId(), newEmail);
            if (success) {
                // 更新 Session 中的用户信息
                currentUser.setEmail(newEmail);
                resp.getWriter().write(JsonUtils.success("邮箱修改成功", null));
            } else {
                resp.getWriter().write(JsonUtils.error("邮箱修改失败"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write(JsonUtils.serverError("修改邮箱失败：" + e.getMessage()));
        }
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