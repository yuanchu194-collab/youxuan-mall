package com.youxuan.common.context;

/**
 * 当前请求用户上下文，后续网关透传用户信息后由下游服务写入和读取。
 */
public final class UserContext {

    private static final ThreadLocal<UserInfo> USER_HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    /**
     * 设置当前线程的用户信息。
     */
    public static void setUser(Long userId, String username, String role) {
        USER_HOLDER.set(new UserInfo(userId, username, role));
    }

    public static UserInfo getUser() {
        return USER_HOLDER.get();
    }

    public static Long getUserId() {
        UserInfo userInfo = USER_HOLDER.get();
        return userInfo == null ? null : userInfo.getUserId();
    }

    public static String getUsername() {
        UserInfo userInfo = USER_HOLDER.get();
        return userInfo == null ? null : userInfo.getUsername();
    }

    public static String getRole() {
        UserInfo userInfo = USER_HOLDER.get();
        return userInfo == null ? null : userInfo.getRole();
    }

    /**
     * 请求结束后必须清理 ThreadLocal，避免线程复用导致用户信息串用。
     */
    public static void clear() {
        USER_HOLDER.remove();
    }

    /**
     * 用户上下文数据载体，仅保存网关透传的基础身份字段。
     */
    public static class UserInfo {

        private final Long userId;
        private final String username;
        private final String role;

        public UserInfo(Long userId, String username, String role) {
            this.userId = userId;
            this.username = username;
            this.role = role;
        }

        public Long getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public String getRole() {
            return role;
        }
    }
}
