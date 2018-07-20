package com.zoudong.permission.service.api;

import com.github.pagehelper.PageInfo;
import com.zoudong.permission.model.SysUser;
import com.zoudong.permission.param.user.login.SysUserLoginParam;
import com.zoudong.permission.param.user.query.QuerySysUserParam;

/**
 * @author zd
 * @description class
 * @date 2018/6/15 17:40
 */
public interface UserService {
    /**
     * 登录
     * @param sysUserLoginParam
     * @return
     * @throws Exception
     */
    String apiLogin(SysUserLoginParam sysUserLoginParam) throws Exception;

    /**
     * 分页查询所有用户
     * @param querySysUserParam
     * @return
     * @throws Exception
     */
    PageInfo<SysUser> queryAllSysUser(QuerySysUserParam querySysUserParam) throws Exception;

}
