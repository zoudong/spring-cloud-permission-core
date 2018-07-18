package com.zoudong.permission.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zoudong.permission.constant.JwtConstant;
import com.zoudong.permission.constant.PermissionCoreConstant;
import com.zoudong.permission.exception.BusinessException;
import com.zoudong.permission.mapper.*;
import com.zoudong.permission.model.*;
import com.zoudong.permission.param.user.login.SysUserLoginParam;
import com.zoudong.permission.param.user.query.QuerySysUserParam;
import com.zoudong.permission.service.api.SysUserService;
import com.zoudong.permission.utils.RedisUtils;
import com.zoudong.permission.utils.jwt.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zd
 * @description class
 * @date 2018/6/15 17:41
 */
@Slf4j
@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RedisUtils redisUtils;


    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRolePermissionMapper sysRolePermissionMapper;

    @Autowired
    private SysPermissionMapper sysPermissionMapper;


    @Override
    public String apiLogin(SysUserLoginParam sysUserLoginParam) throws Exception {
        String account = sysUserLoginParam.getAccount();
        //登录成功后签发token
        if (StringUtils.isEmpty(account)) {
            log.info("获取用户名失败！");
            throw new BusinessException("login_error","获取用户名失败");
        }
        SysUser sysUser = new SysUser();
        sysUser.setAccount(account);
        SysUser userInfo = sysUserMapper.selectOne(sysUser);
        if (null==userInfo) {
            log.info("获取用户信息失败！");
            throw new BusinessException("login_error","获取用户信息失败");
        }
        if (userInfo.getStatus() == 1) {
            log.info("账户冻结！");
            throw new BusinessException("login_error_freeze","账户冻结!");
        }
        if(!sysUserLoginParam.getPassword().equals(userInfo.getPassword())){
            throw new BusinessException("login_error","用户名或密码不正确！");
        }
        JSONObject jo = new JSONObject();
        jo.put("userId", userInfo.getId());
        jo.put("account", userInfo.getAccount());

        String token = jwtUtil.createJWT(jwtUtil.generalKey().toString(), jo.toJSONString(), JwtConstant.JWT_TTL);

        SysUser hyperUserInfo=fillUserInfo(userInfo);

        //填充充血的用户模型信息到redis
        boolean saveResult=redisUtils.set(PermissionCoreConstant.permission_token+token,hyperUserInfo);

        if(!saveResult){
            log.info("用户登录后保存登录信息失败！");
            throw new BusinessException("login_error","用户登录后保存登录信息失败!");
        }

        return token;
    }

    @Override
    public PageInfo<SysUser> queryAllSysUser(QuerySysUserParam querySysUserParam) throws Exception {
        PageHelper.startPage(querySysUserParam.getPageNum(), querySysUserParam.getPageSize());
        List<SysUser> sysUserList = sysUserMapper.selectAll();
        PageInfo<SysUser> pageInfo = new PageInfo<>(sysUserList);
        return pageInfo;
    }



    /**
     * 填充token对应的用户信息
     *
     */
    protected SysUser fillUserInfo(SysUser userInfo) throws Exception {

        //找角色关联
        Example example = new Example(SysUserRole.class);
        example.createCriteria().andEqualTo("userId", userInfo.getId());
        List<SysUserRole> sysUserRoleList = sysUserRoleMapper.selectByExample(example);
        if (sysUserRoleList.isEmpty()) {
            log.info("登录后查询用户关联角色信息失败！");
            throw new BusinessException("login_fill_error","登录后查询用户关联角色信息失败!");
        }
        List<Long> roleIds = new ArrayList<>();
        for (SysUserRole sysUserRole : sysUserRoleList) {
            roleIds.add(sysUserRole.getId());
        }

        //找角色详情信息
        Example roleExample = new Example(SysRole.class);
        roleExample.createCriteria().andIn("id", roleIds);
        List<SysRole> sysRoles = sysRoleMapper.selectByExample(roleExample);
        if (sysRoles.isEmpty()) {
            log.info("登录后查询角色详情信息失败！");
            throw new BusinessException("login_fill_error","登录后查询角色详情信息失败!");
        }

        List<SysPermission> allPermissionList =new ArrayList<SysPermission>();
        for (SysRole sysRole : sysRoles) {

            //找角色权限关联信息
            Example rolePermissionExample = new Example(SysRolePermission.class);
            rolePermissionExample.createCriteria().andEqualTo("roleId", sysRole.getId());
            List<SysRolePermission> sysRolePermissions = sysRolePermissionMapper.selectByExample(rolePermissionExample);
            if (sysRolePermissions.isEmpty()) {
                log.info("登录后查询角色关联权限信息失败！");
                throw new BusinessException("login_fill_error","登录后查询角色关联权限信息失败!");
            }

            List<Long> permissionIds = new ArrayList<>();
            for (SysRolePermission sysRolePermission : sysRolePermissions) {
                permissionIds.add(sysRolePermission.getPermissionId());
            }

            Example permissionExample = new Example(SysPermission.class);
            permissionExample.createCriteria().andIn("id", permissionIds);
            List<SysPermission> sysPermissions = sysPermissionMapper.selectByExample(permissionExample);
            if (sysPermissions.isEmpty()) {
                log.info("登录后查询权限详情信息失败！");
                throw new BusinessException("login_fill_error","登录后查询权限详情信息失败!");
            }
            allPermissionList.addAll(sysPermissions);
            sysRole.setSysPermissions(sysPermissions);
        }

        userInfo.setRoleList(sysRoles);


        List<SysPermission> sysPermissionList = removeDuplicate(allPermissionList);
        //填充去重后的权限
        userInfo.setPermissionList(sysPermissionList);

        return userInfo;
    }

    /**
     * 通过某个属性去重复对象
     *
     * @param list
     * @return
     */
    public static List<SysPermission> removeDuplicate(List<SysPermission> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = list.size() - 1; j > i; j--) {
                if (list.get(j).getPermissionCode().equals(list.get(i).getPermissionCode())) {
                    list.remove(j);
                }
            }
        }
        return list;
    }


}
