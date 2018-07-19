package com.zoudong.permission.constant;

import com.zoudong.permission.model.SysUser;
import lombok.Data;

/**
 * 线程安全的常用信息存储
 */
@Data
public class PermissionThreadLocal {
    public final static ThreadLocal<String> tokenThreadLocal = new ThreadLocal<String>();
    public final static ThreadLocal<SysUser> userThreadLocal = new ThreadLocal<SysUser>();
}