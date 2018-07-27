package com.zoudong.permission.service.api.user;

import com.github.pagehelper.PageInfo;
import com.zoudong.permission.model.SysPermission;
import com.zoudong.permission.model.SysUser;
import com.zoudong.permission.param.login.SysUserLoginParam;
import com.zoudong.permission.param.user.QuerySysUserParam;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author zd
 * @description class
 * @date 2018/7/27 10:50
 */
public interface UserService {


    /**
     * 无状态登录
     *
     * @param sysUserLoginParam
     * @return
     * @throws Exception
     */
    String apiLogin(SysUserLoginParam sysUserLoginParam) throws Exception;

    /**
     * 注销用户(后面加个认证注解登录后的才有权注销)
     *
     * @param token
     * @return
     * @throws Exception
     */
    boolean loginOut(String token) throws Exception;

    /***
     * 新增系统后台管理用户(非前台用户注册)
     * @throws Exception
     */
    void addSysUser(SysUser sysUser) throws Exception;

    /**
     * 为用户分配角色（1个用户可同时拥有多个角色）
     *
     * @param userId
     * @param roleIds
     * @throws Exception
     */
    @Transactional
    void addSysUserRole(Long userId, List<Long> roleIds) throws Exception;

    /***
     * 为用户选择组织
     * @throws Exception
     */
    @Transactional
    void userAddDept(List<Long> userIds, Long deptId) throws Exception;

    /**
     * 按条件分页查询用户信息
     *
     * @param querySysUserParam
     * @return
     * @throws Exception
     */
    PageInfo<SysUser> queryAllSysUser(QuerySysUserParam querySysUserParam) throws Exception;
}
