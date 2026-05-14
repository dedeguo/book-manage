/**
 * 图书管理系统 - 公共 API 工具模块
 */
const API = {
    baseURL: '/book/api',

    /**
     * 通用请求方法
     */
    async request(url, options = {}) {
        const config = {
            ...options,
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            }
        };

        try {
            const response = await fetch(this.baseURL + url, config);
            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || '请求失败');
            }

            return data;
        } catch (error) {
            console.error('API 请求错误:', error);
            throw error;
        }
    },

    /**
     * GET 请求
     */
    async get(url, params = {}) {
        const queryString = new URLSearchParams(params).toString();
        if (queryString) {
            // 如果 URL 已包含查询参数，使用 & 连接，否则使用 ?
            const separator = url.includes('?') ? '&' : '?';
            const fullUrl = `${url}${separator}${queryString}`;
            return this.request(fullUrl, { method: 'GET' });
        }
        return this.request(url, { method: 'GET' });
    },

    /**
     * POST 请求
     */
    async post(url, data = {}) {
        return this.request(url, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    /**
     * PUT 请求
     */
    async put(url, data = {}) {
        return this.request(url, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    },

    /**
     * DELETE 请求
     */
    async delete(url, data = {}) {
        return this.request(url, {
            method: 'DELETE',
            body: JSON.stringify(data)
        });
    }
};

/**
 * 用户认证管理
 * 注意：开发调试阶段使用 sessionStorage，重启 Tomcat 后自动失效，便于验证服务器端 Session
 */
const Auth = {
    storageKey: 'library_user',

    /**
     * 保存用户信息到 Session Storage（仅当前会话有效）
     */
    saveUser(user) {
        // 开发调试阶段：使用 sessionStorage，关闭浏览器/重启 Tomcat 后自动失效
        // 生产环境可改为 localStorage 实现持久登录
        sessionStorage.setItem(this.storageKey, JSON.stringify(user));
    },

    /**
     * 获取当前登录用户
     */
    getUser() {
        const userStr = sessionStorage.getItem(this.storageKey);
        return userStr ? JSON.parse(userStr) : null;
    },

    /**
     * 清除用户信息
     */
    clearUser() {
        sessionStorage.removeItem(this.storageKey);
    },

    /**
     * 检查是否已登录
     * 注意：仅检查 sessionStorage，不验证服务器端 session 状态
     * 如需验证 session 有效性，请使用 validateSession() 方法
     */
    isLoggedIn() {
        return this.getUser() !== null;
    },

    /**
     * 验证服务器端 session 是否有效
     * 调用后端 API 检查当前 session 是否仍然有效
     */
    async validateSession() {
        try {
            const result = await API.get('/auth?action=validate');
            return result.code === 200;
        } catch (error) {
            return false;
        }
    },

    /**
     * 检查是否为管理员
     */
    isAdmin() {
        const user = this.getUser();
        return user && user.role === 'admin';
    },

    /**
     * 跳转到登录页
     */
    redirectToLogin() {
        window.location.href = 'login.html';
    },

    /**
     * 根据角色跳转到首页
     */
    redirectToHome() {
        const user = this.getUser();
        if (user) {
            if (user.role === 'admin') {
                window.location.href = '../admin/admin-index.html';
            } else {
                window.location.href = '../user/index-user.html';
            }
        }
    }
};

/**
 * 工具函数
 */
const Utils = {
    /**
     * 显示提示消息
     */
    toast(message, type = 'info') {
        const colors = {
            success: '#10b981',
            error: '#ef4444',
            warning: '#f59e0b',
            info: '#3b82f6'
        };

        const toast = document.createElement('div');
        toast.style.cssText = `
            position: fixed;
            top: 20px;
            left: 50%;
            transform: translateX(-50%);
            background: ${colors[type]};
            color: white;
            padding: 12px 24px;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            z-index: 9999;
            animation: slideDown 0.3s ease;
        `;
        toast.textContent = message;
        document.body.appendChild(toast);

        setTimeout(() => {
            toast.remove();
        }, 3000);
    },

    /**
     * 格式化日期
     */
    formatDate(dateStr) {
        if (!dateStr) return '-';
        const date = new Date(dateStr);
        return date.toLocaleString('zh-CN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        });
    },

    /**
     * 计算超期天数
     */
    getOverdueDays(dueDate) {
        const due = new Date(dueDate);
        const now = new Date();
        const diff = now - due;
        return diff > 0 ? Math.floor(diff / (1000 * 60 * 60 * 24)) : 0;
    },

    /**
     * 获取 URL 参数
     */
    getUrlParam(name) {
        const params = new URLSearchParams(window.location.search);
        return params.get(name);
    }
};

// 添加动画样式
const style = document.createElement('style');
style.textContent = `
    @keyframes slideDown {
        from {
            opacity: 0;
            transform: translateX(-50%) translateY(-20px);
        }
        to {
            opacity: 1;
            transform: translateX(-50%) translateY(0);
        }
    }
`;
document.head.appendChild(style);