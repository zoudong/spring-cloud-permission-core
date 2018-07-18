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
public interface SysUserService {

    String apiLogin(SysUserLoginParam sysUserLoginParam) throws Exception;

    PageInfo<SysUser> queryAllSysUser(QuerySysUserParam querySysUserParam) throws Exception;

}
