package com.zoudong.permission.constant;

/**
 * @author zd
 * @description class
 * @date 2018/6/15 11:52
 */
public class JwtConstant {
    public static final String JWT_ID = "jwt";
    public static final String JWT_SECRET = "123456789";
    public static final int JWT_TTL = 60*60*1000;  //millisecond
    public static final int JWT_REFRESH_INTERVAL = 55*60*1000;  //millisecond
    public static final int JWT_REFRESH_TTL = 12*60*60*1000;  //millisecond
}
