package com.zoudong.permission.service.impl.user;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zoudong.permission.constant.JwtConstant;
import com.zoudong.permission.constant.PermissionCoreConstant;
import com.zoudong.permission.exception.BusinessException;
import com.zoudong.permission.mapper.*;
import com.zoudong.permission.model.*;
import com.zoudong.permission.param.login.SysUserLoginParam;
import com.zoudong.permission.param.user.QuerySysUserParam;
import com.zoudong.permission.utils.RedisUtils;
import com.zoudong.permission.utils.jwt.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author zd
 * @description class
 * @date 2018/6/15 17:41
 */
@Slf4j
@Service
public class UserServiceImpl implements com.zoudong.permission.service.api.user.UserService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRolePermissionMapper sysRolePermissionMapper;

    @Autowired
    private SysPermissionMapper sysPermissionMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;


    /**
     * 无状态登录（通过用户名密码和验证嘛获取交互token,空了密码加密）
     *
     * @param sysUserLoginParam
     * @return
     * @throws Exception
     */
    @Override
    public String apiLogin(SysUserLoginParam sysUserLoginParam) throws Exception {
        String account = sysUserLoginParam.getAccount();
        //登录成功后签发token
        if (StringUtils.isEmpty(account)) {
            log.info("获取用户名失败！");
            throw new BusinessException("login_error", "获取用户名失败");
        }
        SysUser sysUser = new SysUser();
        sysUser.setAccount(account);
        SysUser userInfo = sysUserMapper.selectOne(sysUser);
        if (null == userInfo) {
            log.info("获取用户信息失败！");
            throw new BusinessException("login_error", "获取用户信息失败");
        }
        if (userInfo.getStatus() == 1) {
            log.info("账户被冻结！");
            throw new BusinessException("login_error_freeze", "账户被冻结!");
        }
        if (!sysUserLoginParam.getPassword().equals(userInfo.getPassword())) {
            throw new BusinessException("login_error", "用户名或密码不正确！");
        }
        JSONObject jo = new JSONObject();
        jo.put("userId", userInfo.getId());
        jo.put("account", userInfo.getAccount());

        String token = jwtUtil.createJWT(jwtUtil.generalKey().toString(), jo.toJSONString(), JwtConstant.JWT_TTL);

        SysUser hyperUserInfo = fillUserInfo(userInfo);

        //填充充血的用户模型信息到redis
        boolean saveResult = redisUtils.set(PermissionCoreConstant.permission_token + token, hyperUserInfo);

        if (!saveResult) {
            log.info("用户登录后保存登录信息失败！");
            throw new BusinessException("login_error", "用户登录后保存登录信息失败!");
        }

        return token;
    }


    /**
     * 填充token对应的用户信息
     */
    protected SysUser fillUserInfo(SysUser userInfo) throws Exception {

        //找角色关联
        Example example = new Example(SysUserRole.class);
        example.createCriteria().andEqualTo("userId", userInfo.getId());
        List<SysUserRole> sysUserRoleList = sysUserRoleMapper.selectByExample(example);
        if (sysUserRoleList.isEmpty()) {
            log.info("登录后查询用户关联角色信息失败！");
            throw new BusinessException("login_fill_error", "登录后查询用户关联角色信息失败!");
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
            throw new BusinessException("login_fill_error", "登录后查询角色详情信息失败!");
        }

        List<SysPermission> allPermissionList = new ArrayList<SysPermission>();
        for (SysRole sysRole : sysRoles) {

            //找角色权限关联信息
            Example rolePermissionExample = new Example(SysRolePermission.class);
            rolePermissionExample.createCriteria().andEqualTo("roleId", sysRole.getId());
            List<SysRolePermission> sysRolePermissions = sysRolePermissionMapper.selectByExample(rolePermissionExample);
            if (sysRolePermissions.isEmpty()) {
                log.info("登录后查询角色关联权限信息失败！");
                throw new BusinessException("login_fill_error", "登录后查询角色关联权限信息失败!");
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
                throw new BusinessException("login_fill_error", "登录后查询权限详情信息失败!");
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
     * 注销用户(后面加个认证注解登录后的才有权注销)
     *
     * @param token
     * @return
     * @throws Exception
     */
    @Override
    public boolean loginOut(String token) throws Exception {
        if (StringUtils.isEmpty(token)) {
            throw new BusinessException("token_error", "token不能为空");
        }
        if (redisUtils.get(PermissionCoreConstant.permission_token + token) != null) {
            redisUtils.remove(PermissionCoreConstant.permission_token + token);
            return true;
        } else {
            throw new BusinessException("token_error", "查询用户token失败");
        }
    }


    /***
     * 新增系统后台管理用户(非前台用户注册)
     * @throws Exception
     */
    @Override
    public void addSysUser(SysUser sysUser) throws Exception {
        addUserParmCheck(sysUser);
        sysUser.setId(null);
        int result = sysUserMapper.insertSelective(sysUser);
        if (result != 1) {
            throw new BusinessException("add_user_error", "新增用户失败,受影响行数为0!");
        }
    }

    /**
     * 为用户分配角色（1个用户可同时拥有多个角色）
     *
     * @param userId
     * @param roleIds
     * @throws Exception
     */
    @Override
    @Transactional
    public void addSysUserRole(Long userId, List<Long> roleIds) throws Exception {

        //先移除再分配
        Example example = new Example(SysUserRole.class);
        example.createCriteria().andEqualTo("userId", userId);
        sysUserRoleMapper.deleteByExample(example);

        List<SysUserRole> sysUserRoles = new ArrayList<SysUserRole>();
        for (Long roleId : roleIds) {
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setUserId(userId);
            sysUserRole.setRoleId(roleId);
            sysUserRole.setCreateTime(new Date());
            sysUserRole.setVersion(0L);
            sysUserRoles.add(sysUserRole);
        }

        int result = sysUserRoleMapper.insertList(sysUserRoles);
        if (result != sysUserRoles.size()) {
            throw new BusinessException("add_user_role_error", "添加用户角色关联失败,受影响行数不对称!");
        }
    }


    private void addUserParmCheck(SysUser sysUser) throws Exception {
        if (StringUtils.isEmpty(sysUser.getAccount())) {
            throw new BusinessException("add_user_error", "新增用户失败,账户名不能为空!");
        }
        Example accountExample = new Example(SysUser.class);
        accountExample.createCriteria().andEqualTo("account", sysUser.getAccount());
        List<SysUser> accountUserResult = sysUserMapper.selectByExample(accountExample);
        if (!accountUserResult.isEmpty()) {
            throw new BusinessException("add_user_error", "新增用户失败,账户已存在，请更换名称!");
        }

        if (StringUtils.isEmpty(sysUser.getPassword())) {
            throw new BusinessException("add_user_error", "新增用户失败,密码不能为空!");
        }
        if (StringUtils.isEmpty(sysUser.getPhone())) {
            throw new BusinessException("add_user_error", "新增用户失败,手机号码不能为空!");
        }
        Example phoneExample = new Example(SysUser.class);
        phoneExample.createCriteria().andEqualTo("phone", sysUser.getPhone());
        List<SysUser> phoneUserResult = sysUserMapper.selectByExample(phoneExample);
        if (!phoneUserResult.isEmpty()) {
            throw new BusinessException("add_user_error", "新增用户失败,手机号码已存在，请更换手机号码!");
        }
        if (StringUtils.isEmpty(sysUser.getEmail())) {
            throw new BusinessException("add_user_error", "新增用户失败,邮箱地址不能为空!");
        }
        Example emailExample = new Example(SysUser.class);
        phoneExample.createCriteria().andEqualTo("email", sysUser.getPhone());
        List<SysUser> emailUserResult = sysUserMapper.selectByExample(emailExample);
        if (!emailUserResult.isEmpty()) {
            throw new BusinessException("add_user_error", "新增用户失败,邮箱已存在，请更换邮箱帐号!");
        }
    }


    /***
     * 为用户选择组织
     * @throws Exception
     */
    @Override
    @Transactional
    public void userAddDept(List<Long> userIds, Long deptId) throws Exception {
        SysUser sysUser = new SysUser();
        sysUser.setDeptId(deptId);

        Example example = new Example(SysUser.class);
        example.createCriteria().andIn("id", userIds);
        int result = sysUserMapper.updateByExampleSelective(sysUser, example);

        if (result != userIds.size()) {
            throw new BusinessException("user_add_dept_error", "为用户选择组织失败,受影响行数与要挂靠组织的用户数量不一致!");
        }
    }


    /**
     * 按条件分页查询用户信息
     *
     * @param querySysUserParam
     * @return
     * @throws Exception
     */
    @Override
    public PageInfo<SysUser> queryAllSysUser(QuerySysUserParam querySysUserParam) throws Exception {
        PageHelper.startPage(querySysUserParam.getPageNum(), querySysUserParam.getPageSize());

        Example example = new Example(SysUser.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotEmpty(querySysUserParam.getAccount())) {
            criteria.andLike("account", querySysUserParam.getAccount());
        }

        if (null != querySysUserParam.getId()) {
            criteria.andEqualTo("id", querySysUserParam.getId());
        }

        if (StringUtils.isNotEmpty(querySysUserParam.getEmail())) {
            criteria.andLike("email", querySysUserParam.getEmail());
        }

        if (StringUtils.isNotEmpty(querySysUserParam.getPhone())) {
            criteria.andLike("phone", querySysUserParam.getPhone());
        }

        if (StringUtils.isNotEmpty(querySysUserParam.getRealName())) {
            criteria.andLike("realName", querySysUserParam.getRealName());
        }

        if (null != querySysUserParam.getSex()) {
            criteria.andEqualTo("sex", querySysUserParam.getSex());
        }

        List<SysUser> sysUserList = sysUserMapper.selectByExample(example);
        PageInfo<SysUser> pageInfo = new PageInfo<>(sysUserList);
        return pageInfo;
    }



    /**
     * 通过某个属性去重复对象
     *
     * @param list
     * @return
     */
    static List<SysPermission> removeDuplicate(List<SysPermission> list) {
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
