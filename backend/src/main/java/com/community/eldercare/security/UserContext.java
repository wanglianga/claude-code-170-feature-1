package com.community.eldercare.security;

import com.community.eldercare.domain.User;

/** 当前登录用户线程上下文 */
public class UserContext {
    private static final ThreadLocal<User> HOLDER = new ThreadLocal<>();

    public static void set(User user) {
        HOLDER.set(user);
    }

    public static User get() {
        return HOLDER.get();
    }

    public static String currentName() {
        User u = HOLDER.get();
        return u == null ? "系统" : u.getName();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
