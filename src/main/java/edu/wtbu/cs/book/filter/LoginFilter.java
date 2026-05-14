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
 * 登录验证过滤器
 * 拦截所有需要登录才能访问的页面，未登录用户自动跳转到登录页
 * 白名单：登录页、登录 Servlet、静态资源、注册页
 */
@WebFilter(filterName = "loginFilter", urlPatterns = {"/*"})
public class LoginFilter implements Filter {

    // 白名单路径（不需要登录即可访问）
    private static final List<String> WHITELIST = Arrays.asList(
            "/user/login.html",
            "/user/register.html",
            "/api/auth?action=login",
            "/api/auth?action=register",
            "/api/auth?action=logout",
            "/js/",
            "/css/",
            "/images/",
            "/fonts/"
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

        // 检查是否在白名单中
        if (isWhitelisted(requestURI, contextPath)) {
            chain.doFilter(request, response);
            return;
        }

        // 获取 Session
        HttpSession session = req.getSession(false);

        // 检查用户是否已登录
        User currentUser = session != null ? (User) session.getAttribute("currentUser") : null;

        if (currentUser == null) {
            // 未登录，重定向到登录页
            resp.sendRedirect(req.getContextPath() + "/user/login.html");
            return;
        }

        // 已登录，继续处理请求
        chain.doFilter(request, response);
    }

    /**
     * 检查请求路径是否在白名单中
     */
    private boolean isWhitelisted(String requestURI, String contextPath) {
        // 去掉上下文路径
        String path = requestURI.substring(contextPath.length());

        // 精确匹配
        for (String whitelistPath : WHITELIST) {
            if (whitelistPath.startsWith("/api/")) {
                // API 路径需要包含 action 参数
                if (path.equals(whitelistPath.split("\\?")[0])) {
                    return true;
                }
            } else {
                // HTML 页面精确匹配
                if (path.equals(whitelistPath)) {
                    return true;
                }
            }
        }

        // 前缀匹配（静态资源）
        for (String prefix : Arrays.asList("/js/", "/css/", "/images/", "/fonts/")) {
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
