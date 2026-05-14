package edu.wtbu.cs.book.controller;

import edu.wtbu.cs.book.entity.Book;
import edu.wtbu.cs.book.service.BookService;
import edu.wtbu.cs.book.util.JsonUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图书 Servlet - 处理图书浏览、查询、搜索
 */
@WebServlet(name = "bookServlet", value = "/api/books")
public class BookServlet extends HttpServlet {

    private final BookService bookService = new BookService();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 设置编码
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        String action = req.getParameter("action");
        String method = req.getMethod();

        if ("GET".equalsIgnoreCase(method)) {
            if ("list".equals(action)) {
                listBooks(req, resp);
            } else if ("detail".equals(action)) {
                getBookDetail(req, resp);
            } else if ("search".equals(action)) {
                searchBooks(req, resp);
            } else if ("categories".equals(action)) {
                getCategories(req, resp);
            } else {
                // 默认返回图书列表
                listBooks(req, resp);
            }
        } else {
            resp.getWriter().write(JsonUtils.error("不支持的请求方法"));
        }
    }

    /**
     * 获取图书列表（支持分类筛选）
     */
    private void listBooks(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String category = req.getParameter("category");
            List<Book> books = bookService.getBooksByCategory(category);

            // 转换为简化的 JSON 格式
            resp.getWriter().write(JsonUtils.success(formatBooks(books)));
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write(JsonUtils.serverError("获取图书列表失败：" + e.getMessage()));
        }
    }

    /**
     * 获取图书详情
     */
    private void getBookDetail(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String idStr = req.getParameter("id");
            if (idStr == null || idStr.isEmpty()) {
                resp.getWriter().write(JsonUtils.error("缺少图书 ID"));
                return;
            }

            Integer id = Integer.parseInt(idStr);
            Book book = bookService.getBookById(id);

            if (book != null) {
                resp.getWriter().write(JsonUtils.success(formatBook(book)));
            } else {
                resp.getWriter().write(JsonUtils.notFound("图书不存在"));
            }
        } catch (NumberFormatException e) {
            resp.getWriter().write(JsonUtils.error("无效的图书 ID"));
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write(JsonUtils.serverError("获取图书详情失败：" + e.getMessage()));
        }
    }

    /**
     * 搜索图书
     */
    private void searchBooks(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String keyword = req.getParameter("keyword");
            if (keyword == null || keyword.isEmpty()) {
                resp.getWriter().write(JsonUtils.error("搜索关键词不能为空"));
                return;
            }

            List<Book> books = bookService.searchBooks(keyword);
            resp.getWriter().write(JsonUtils.success(formatBooks(books)));
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write(JsonUtils.serverError("搜索图书失败：" + e.getMessage()));
        }
    }

    /**
     * 获取所有分类
     */
    private void getCategories(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<String> categories = bookService.getAllCategories();
            resp.getWriter().write(JsonUtils.success(categories));
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write(JsonUtils.serverError("获取分类失败：" + e.getMessage()));
        }
    }

    /**
     * 格式化图书列表为 JSON 友好格式
     */
    private java.util.List<Map<String, Object>> formatBooks(List<Book> books) {
        java.util.List<Map<String, Object>> list = new java.util.ArrayList<>();
        for (Book book : books) {
            list.add(formatBook(book));
        }
        return list;
    }

    /**
     * 格式化图书对象为 JSON 友好格式
     */
    private Map<String, Object> formatBook(Book book) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", book.getId());
        map.put("title", book.getTitle());
        map.put("author", book.getAuthor());
        map.put("publisher", book.getPublisher());
        map.put("isbn", book.getIsbn());
        map.put("description", book.getDescription());
        map.put("coverUrl", book.getCoverUrl());
        map.put("category", book.getCategory());
        map.put("totalCopies", book.getTotalCopies());
        map.put("availableCopies", book.getAvailableCopies());
        map.put("isAvailable", book.isAvailable());
        map.put("createdAt", book.getCreatedAt() != null ? book.getCreatedAt().toString() : null);
        return map;
    }
}