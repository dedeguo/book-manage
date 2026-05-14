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
 * 认证 Servlet - 处理登录、注册、登出
 */
@WebServlet(name = "authServlet", value = "/api/auth")
public class AuthServlet extends HttpServlet {

    private final UserService userService = new UserService();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 设置编码
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        String action = req.getParameter("action");
        String method = req.getMethod();

        if ("POST".equalsIgnoreCase(method)) {
            if ("login".equals(action)) {
                login(req, resp);
            } else if ("register".equals(action)) {
                register(req, resp);
            } else if ("logout".equals(action)) {
                logout(req, resp);
            } else if ("validate".equals(action)) {
                validateSession(req, resp);
            } else {
                resp.getWriter().write(JsonUtils.error("未知的操作"));
            }
        } else if ("GET".equalsIgnoreCase(method)) {
            if ("validate".equals(action)) {
                validateSession(req, resp);
            } else {
                resp.getWriter().write(JsonUtils.error("不支持的请求方法"));
            }
        } else {
            resp.getWriter().write(JsonUtils.error("不支持的请求方法"));
        }
    }

    /**
     * 用户登录
     */
    private void login(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // 读取 JSON 请求体
            Map<String, String> params = readJsonBody(req);
            String username = params.get("username");
            String password = params.get("password");

            // 参数校验
            if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
                resp.getWriter().write(JsonUtils.error("用户名和密码不能为空"));
                return;
            }

            // 调用服务层（密码加密后验证）
            User user = userService.login(username, MD5Utils.md5(password));
            if (user != null) {
                // 登录成功，存入 Session
                HttpSession session = req.getSession();
                session.setAttribute("currentUser", user);
                session.setMaxInactiveInterval(30 * 60); // 30 分钟超时

                // 返回用户信息（不包含密码）
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("name", user.getName());
                userInfo.put("role", user.getRole());
                userInfo.put("userType", user.getUserType());
                userInfo.put("email", user.getEmail());

                resp.getWriter().write(JsonUtils.success("登录成功", userInfo));
            } else {
                resp.getWriter().write(JsonUtils.error("用户名或密码错误"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write(JsonUtils.serverError("登录失败：" + e.getMessage()));
        }
    }

    /**
     * 用户注册
     */
    private void register(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // 读取 JSON 请求体
            Map<String, String> params = readJsonBody(req);
            String username = params.get("username");
            String name = params.get("name");
            String password = params.get("password");
            String email = params.get("email");

            // 参数校验
            if (username == null || username.isEmpty() || name == null || name.isEmpty()
                    || password == null || password.isEmpty() || email == null || email.isEmpty()) {
                resp.getWriter().write(JsonUtils.error("所有字段均为必填"));
                return;
            }

            // 检查用户名是否已存在
            User existingUser = userService.getUserByUsername(username);
            if (existingUser != null) {
                resp.getWriter().write(JsonUtils.error("该用户名已被注册"));
                return;
            }

            // 调用服务层注册（密码加密存储）
            boolean success = userService.register(username, name, MD5Utils.md5(password), email);
            if (success) {
                resp.getWriter().write(JsonUtils.success("注册成功", null));
            } else {
                resp.getWriter().write(JsonUtils.error("注册失败"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write(JsonUtils.serverError("注册失败：" + e.getMessage()));
        }
    }

    /**
     * 退出登录
     */
    private void logout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.removeAttribute("currentUser");
            session.invalidate();
        }
        resp.getWriter().write(JsonUtils.success("退出成功", null));
    }

    /**
     * 验证 Session 是否有效
     */
    private void validateSession(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        User currentUser = session != null ? (User) session.getAttribute("currentUser") : null;

        if (currentUser != null) {
            // Session 有效，返回用户信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", currentUser.getId());
            userInfo.put("username", currentUser.getUsername());
            userInfo.put("name", currentUser.getName());
            userInfo.put("role", currentUser.getRole());
            userInfo.put("userType", currentUser.getUserType());
            resp.getWriter().write(JsonUtils.success("Session 有效", userInfo));
        } else {
            // Session 无效
            resp.getWriter().write(JsonUtils.error("Session 已过期"));
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

        // 简单解析 JSON（生产环境建议使用 Jackson/Gson）
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