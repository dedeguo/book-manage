package edu.wtbu.cs.book.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * JSON 响应工具类
 */
public class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取 ObjectMapper 实例
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * 返回成功响应
     */
    public static String success(Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", data);
        return toJson(result);
    }

    /**
     * 返回成功响应（带消息）
     */
    public static String success(String message, Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", message);
        result.put("data", data);
        return toJson(result);
    }

    /**
     * 返回错误响应
     */
    public static String error(int code, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("message", message);
        result.put("data", null);
        return toJson(result);
    }

    /**
     * 返回错误响应（默认 400）
     */
    public static String error(String message) {
        return error(400, message);
    }

    /**
     * 返回未授权响应
     */
    public static String unauthorized(String message) {
        return error(401, message);
    }

    /**
     * 返回禁止访问响应
     */
    public static String forbidden(String message) {
        return error(403, message);
    }

    /**
     * 返回未找到响应
     */
    public static String notFound(String message) {
        return error(404, message);
    }

    /**
     * 返回服务器错误响应
     */
    public static String serverError(String message) {
        return error(500, message);
    }

    /**
     * 对象转 JSON 字符串
     */
    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "{\"code\":500,\"message\":\"JSON 转换失败\",\"data\":null}";
        }
    }
}