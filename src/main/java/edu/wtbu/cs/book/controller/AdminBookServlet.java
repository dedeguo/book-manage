package edu.wtbu.cs.book.controller;

import edu.wtbu.cs.book.entity.Book;
import edu.wtbu.cs.book.entity.User;
import edu.wtbu.cs.book.service.BookService;
import edu.wtbu.cs.book.util.JsonUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员图书 Servlet - 处理图书管理（CRUD）
 */
@WebServlet(name = "adminBookServlet", value = "/api/admin/books")
public class AdminBookServlet extends HttpServlet {

    private final BookService bookService = new BookService();

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

        // 获取 action 参数
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
                listBooks(req, resp);
            }
        } else if ("POST".equalsIgnoreCase(method)) {
            if ("create".equals(action)) {
                createBook(req, resp);
            } else if ("update".equals(action)) {
                updateBook(req, resp);
            } else if ("delete".equals(action)) {
                deleteBook(req, resp);
            } else {
                resp.getWriter().write(JsonUtils.error("未知的操作"));
            }
        } else {
            resp.getWriter().write(JsonUtils.error("不支持的请求方法"));
        }
    }

    /**
     * 获取图书列表
     */
    private void listBooks(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String category = req.getParameter("category");
            List<Book> books = bookService.getBooksByCategory(category);
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
        System.out.println("=== getBookDetail ===");
        System.out.println("id param: " + req.getParameter("id"));
        System.out.println("action param: " + req.getParameter("action"));
        try {
            String idStr = req.getParameter("id");
            if (idStr == null || idStr.isEmpty()) {
                resp.getWriter().write(JsonUtils.error("缺少图书 ID"));
                return;
            }

            Integer id = Integer.parseInt(idStr);
            Book book = bookService.getBookById(id);

            System.out.println("Book from service: " + book);

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
            String category = req.getParameter("category");

            List<Book> books;
            if (keyword != null && !keyword.isEmpty()) {
                books = bookService.searchBooks(keyword);
            } else if (category != null && !category.isEmpty()) {
                books = bookService.getBooksByCategory(category);
            } else {
                books = bookService.getAllBooks();
            }

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
     * 新增图书
     */
    private void createBook(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Map<String, String> params = readJsonBody(req);

            Book book = new Book();
            book.setTitle(getParam(params, "title"));
            book.setAuthor(getParam(params, "author"));
            book.setPublisher(getParam(params, "publisher"));
            book.setIsbn(getParam(params, "isbn"));
            book.setDescription(getParam(params, "description"));
            book.setCoverUrl(getParam(params, "coverUrl"));
            book.setCategory(getParam(params, "category"));

            // 解析复本数
            try {
                String totalCopiesStr = getParam(params, "totalCopies");
                int totalCopies = totalCopiesStr != null && !totalCopiesStr.isEmpty()
                        ? Integer.parseInt(totalCopiesStr) : 1;
                book.setTotalCopies(totalCopies);
                book.setAvailableCopies(totalCopies);
            } catch (NumberFormatException e) {
                book.setTotalCopies(1);
                book.setAvailableCopies(1);
            }

            boolean success = bookService.addBook(book);
            if (success) {
                resp.getWriter().write(JsonUtils.success("图书添加成功", null));
            } else {
                resp.getWriter().write(JsonUtils.error("图书添加失败"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write(JsonUtils.serverError("添加图书失败：" + e.getMessage()));
        }
    }

    /**
     * 更新图书
     */
    private void updateBook(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Map<String, String> params = readJsonBody(req);

            String idStr = getParam(params, "id");
            if (idStr == null || idStr.isEmpty()) {
                resp.getWriter().write(JsonUtils.error("缺少图书 ID"));
                return;
            }

            Integer id = Integer.parseInt(idStr);
            Book book = bookService.getBookById(id);
            if (book == null) {
                resp.getWriter().write(JsonUtils.notFound("图书不存在"));
                return;
            }

            // 更新字段
            book.setTitle(getParam(params, "title"));
            book.setAuthor(getParam(params, "author"));
            book.setPublisher(getParam(params, "publisher"));
            book.setIsbn(getParam(params, "isbn"));
            book.setDescription(getParam(params, "description"));
            book.setCoverUrl(getParam(params, "coverUrl"));
            book.setCategory(getParam(params, "category"));
            // 不修改 totalCopies 和 availableCopies，避免借阅记录错乱

            boolean success = bookService.updateBook(book);
            if (success) {
                resp.getWriter().write(JsonUtils.success("图书更新成功", null));
            } else {
                resp.getWriter().write(JsonUtils.error("图书更新失败"));
            }
        } catch (NumberFormatException e) {
            resp.getWriter().write(JsonUtils.error("无效的图书 ID"));
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write(JsonUtils.serverError("更新图书失败：" + e.getMessage()));
        }
    }

    /**
     * 删除图书
     */
    private void deleteBook(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Map<String, String> params = readJsonBody(req);
            String idStr = getParam(params, "id");

            if (idStr == null || idStr.isEmpty()) {
                resp.getWriter().write(JsonUtils.error("缺少图书 ID"));
                return;
            }

            Integer id = Integer.parseInt(idStr);
            Book book = bookService.getBookById(id);
            if (book == null) {
                resp.getWriter().write(JsonUtils.notFound("图书不存在"));
                return;
            }

            // 检查是否有借阅记录
            if (book.hasBorrowRecord()) {
                resp.getWriter().write(JsonUtils.error("该图书有借阅记录，无法删除"));
                return;
            }

            boolean success = bookService.deleteBook(id);
            if (success) {
                resp.getWriter().write(JsonUtils.success("图书删除成功", null));
            } else {
                resp.getWriter().write(JsonUtils.error("图书删除失败"));
            }
        } catch (NumberFormatException e) {
            resp.getWriter().write(JsonUtils.error("无效的图书 ID"));
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write(JsonUtils.serverError("删除图书失败：" + e.getMessage()));
        }
    }

    /**
     * 从 Map 中获取参数，处理 null 和空字符串
     */
    private String getParam(Map<String, String> params, String key) {
        String value = params.get(key);
        return value != null && !value.isEmpty() ? value : null;
    }

    /**
     * 格式化图书列表
     */
    private List<Map<String, Object>> formatBooks(List<Book> books) {
        List<Map<String, Object>> list = new java.util.ArrayList<>();
        for (Book book : books) {
            list.add(formatBook(book));
        }
        return list;
    }

    /**
     * 格式化图书对象
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
        String json = sb.toString().trim();

        if (!json.isEmpty()) {
            try {
                com.fasterxml.jackson.databind.JsonNode node = JsonUtils.getObjectMapper().readTree(json);
                if (node.isObject()) {
                    node.fields().forEachRemaining(entry ->
                        params.put(entry.getKey(), entry.getValue().isTextual()
                            ? entry.getValue().asText()
                            : entry.getValue().toString())
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return params;
    }
}