package edu.wtbu.cs.book.filter;

import edu.wtbu.cs.book.entity.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 管理员权限过滤器
 * 拦截所有管理员后台页面，非管理员用户自动跳转到用户首页
 * 注意：LoginFilter 已处理登录验证，本过滤器只负责权限检查
 */
@WebFilter(filterName = "adminFilter", urlPatterns = {"/admin/*", "/api/admin/*"})
public class AdminFilter implements Filter {

    // 白名单路径（不需要检查管理员权限）
    private static final List<String> WHITELIST = Arrays.asList(
            "/js/", "/css/", "/images/", "/fonts/"
    );

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 初始化操作（如有需要）
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();

        // 检查是否为静态资源（白名单）
        if (isWhitelisted(requestURI, contextPath)) {
            chain.doFilter(request, response);
            return;
        }

        // 获取 Session（LoginFilter 已确保用户已登录）
        HttpSession session = req.getSession(false);
        User currentUser = session != null ? (User) session.getAttribute("currentUser") : null;

        // 检查是否为管理员
        if (currentUser == null || !currentUser.isAdmin()) {
            // 非管理员或未登录，重定向到用户首页
            resp.sendRedirect(req.getContextPath() + "/user/index-user.html");
            return;
        }

        // 已登录且为管理员，继续处理请求
        chain.doFilter(request, response);
    }

    /**
     * 检查请求路径是否在白名单中
     */
    private boolean isWhitelisted(String requestURI, String contextPath) {
        String path = requestURI.substring(contextPath.length());

        for (String prefix : WHITELIST) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void destroy() {
        // 销毁操作（如有需要）
    }
}
