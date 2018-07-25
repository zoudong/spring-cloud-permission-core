package com.zoudong.permission.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zoudong.permission.constant.JwtConstant;
import com.zoudong.permission.constant.PermissionCoreConstant;
import com.zoudong.permission.exception.BusinessException;
import com.zoudong.permission.mapper.*;
import com.zoudong.permission.model.*;
import com.zoudong.permission.param.dept.SysDeptParam;
import com.zoudong.permission.param.login.SysUserLoginParam;
import com.zoudong.permission.param.menu.SysMenuParam;
import com.zoudong.permission.param.permission.QuerySysPermissionParam;
import com.zoudong.permission.param.user.QuerySysUserParam;
import com.zoudong.permission.param.role.SysRoleParam;
import com.zoudong.permission.service.api.UserService;
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
public class UserServiceImpl implements UserService {

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
    private SysMenuMapper sysMenuMapper;

    @Autowired
    private SysPermissionMapper sysPermissionMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysDeptMapper sysDeptMapper;


    /**
     * 无状态登录
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


    /**
     * 注销用户(后面加个认证注解登录后的才有权注销)
     *
     * @param token
     * @return
     * @throws Exception
     */
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

    /**
     * 菜单缓存起来(增加菜单信息后更新菜单信息缓存)
     *
     * @return
     * @throws Exception
     */
    public void cacheSysMenu() throws Exception {
        List<SysMenu> permSysMenus = new ArrayList<>();
        if (!permSysMenus.isEmpty()) {
            List<SysMenu> sysMenus = sysMenuMapper.selectAll();
            redisUtils.set(PermissionCoreConstant.permission_menu, sysMenus);
        } else {
            throw new BusinessException("cacheMenu_error", "查询系统菜失败");
        }
    }

    /**
     * 根据不同的token的权限从缓存里面加载不同的菜单
     *
     * @param token
     * @return
     * @throws Exception
     */
    public List<SysMenu> queryAdminMenu(String token) throws Exception {
        if (StringUtils.isEmpty(token)) {
            throw new BusinessException("token_error", "token不能为空");
        }
        List<SysMenu> permSysMenus = new ArrayList<>();
        if (redisUtils.get(PermissionCoreConstant.permission_token + token) == null) {
            throw new BusinessException("token_error", "token已经被注销请重新登录");
        }
        SysUser sysUser = (SysUser) redisUtils.get(PermissionCoreConstant.permission_token + token);
        List<SysPermission> userSysPermissions = sysUser.getPermissionList();
        if (redisUtils.get(PermissionCoreConstant.permission_menu) != null) {
            List<SysMenu> sysMenus = (List<SysMenu>) redisUtils.get(PermissionCoreConstant.permission_menu);
            for (SysMenu sysMenu : sysMenus) {
                for (SysPermission sysPermission : userSysPermissions) {
                    if (sysMenu.getPermissionCode().equals(sysPermission.getPermissionCode())) {
                        permSysMenus.add(sysMenu);
                    }
                }
            }
        } else {
            throw new BusinessException("token_error", "查询用户token失败");
        }
        return permSysMenus;
    }


    /**
     * 添加权限
     *
     * @param sysPermission
     * @throws Exception
     */
    public void addPermission(SysPermission sysPermission) throws Exception {
        sysPermission.setId(null);
        sysPermission.setVersion(0L);
        sysPermission.setCreateTime(new Date());
        int result = sysPermissionMapper.insert(sysPermission);
        if (result != 1) {
            throw new BusinessException("add_permission_error", "添加权限失败,受影响行数为0!");
        }
    }

    /**
     * 删除权限(一般不使用)
     *
     * @param ids
     * @throws Exception
     */
    @Transactional
    public void deletePermissions(List<Long> ids) throws Exception {
        Example example = new Example(SysRolePermission.class);
        example.createCriteria().andIn("permissionId", ids);
        List<SysRolePermission> permissions = sysRolePermissionMapper.selectByExample(example);
        if (permissions.size() != 0) {
            log.info("有权限正在被角色使用", permissions);
            throw new BusinessException("delete_permissions_error", "删除权限失败,有权限正在被角色使用!");
        }
        Example permiissionExample = new Example(SysPermission.class);
        permiissionExample.createCriteria().andIn("permissionId", ids);
        int result = sysPermissionMapper.deleteByExample(permiissionExample);
        if (result != ids.size()) {
            throw new BusinessException("delete_permissions_error", "删除权限失败,受影响行数不对称!");
        }
    }

    /**
     * 添加角色
     *
     * @param sysRole
     * @throws Exception
     */
    public void addRole(SysRole sysRole) throws Exception {
        sysRole.setId(null);
        sysRole.setVersion(0L);
        sysRole.setCreateTime(new Date());
        int result = sysRoleMapper.insert(sysRole);
        if (result != 1) {
            throw new BusinessException("add_role_error", "添加角色失败,受影响行数为0!");
        }
    }

    /**
     * 删除角色
     *
     * @param ids
     * @throws Exception
     */
    @Transactional
    public void deleteRoles(List<Long> ids) throws Exception {
        Example example = new Example(SysUserRole.class);
        example.createCriteria().andIn("roleId", ids);
        List<SysUserRole> sysUserRoles = sysUserRoleMapper.selectByExample(example);
        if (sysUserRoles.size() != 0) {
            log.info("有角色正在被用户使用", sysUserRoles);
            throw new BusinessException("delete_permission_error", "删除角色失败,有角色正在被用户使用!");
        }
        Example roleExample = new Example(SysPermission.class);
        roleExample.createCriteria().andIn("roleId", ids);
        int result = sysRoleMapper.deleteByExample(roleExample);
        if (result != ids.size()) {
            throw new BusinessException("delete_roles_error", "删除权限失败,受影响行数与入参不对称!");
        }
    }

    /**
     * 为角色分配权限（1个角色可同时分配多个权限）
     *
     * @param roleId
     * @param permissionIds
     * @throws Exception
     */
    @Transactional
    public void addSysRolePermission(Long roleId, List<Long> permissionIds) throws Exception {

        //先移除再分配
        Example example = new Example(SysRolePermission.class);
        example.createCriteria().andEqualTo("roleId", roleId);
        sysRolePermissionMapper.deleteByExample(example);

        List<SysRolePermission> sysRolePermissions = new ArrayList<SysRolePermission>();
        for (Long permissionId : permissionIds) {
            SysRolePermission sysRolePermission = new SysRolePermission();
            sysRolePermission.setRoleId(roleId);
            sysRolePermission.setPermissionId(permissionId);
            sysRolePermission.setCreateTime(new Date());
            sysRolePermission.setVersion(0L);
            sysRolePermissions.add(sysRolePermission);
        }

        int result = sysRolePermissionMapper.insertList(sysRolePermissions);
        if (result != sysRolePermissions.size()) {
            throw new BusinessException("add_role_error", "添加角色权限关联失败,受影响行数为0!");
        }
    }

    /***
     * 新增菜单
     * @param sysMenu
     * @throws Exception
     */
    public void addSysMenu(SysMenu sysMenu) throws Exception {
        sysMenu.setId(null);
        sysMenu.setVersion(0L);
        sysMenu.setCreateTime(new Date());
        int result = sysMenuMapper.insert(sysMenu);
        if (result != 1) {
            throw new BusinessException("add_role_error", "添加菜单失败,受影响行数为0!");
        }
    }

    /***
     * 为菜单挂靠需要的权限（只有菜单才需要挂靠权限,不是每一个权限都要挂靠到菜单）(需要什么权限能展示这个菜单，与菜单内容的权限信息要一致)
     * @throws Exception
     */
    public void addSysMenuPermission(Long menuId, String permissionId) throws Exception {
        SysMenu sysMenu = new SysMenu();
        sysMenu.setId(menuId);
        sysMenu.setPermissionCode(permissionId);
        int result = sysMenuMapper.updateByPrimaryKeySelective(sysMenu);
        if (result != 1) {
            throw new BusinessException("add_role_error", "为菜单挂靠关联权限失败,受影响行数为0!");
        }
    }

    /***
     * 新增系统后台管理用户(非前台用户注册)
     * @throws Exception
     */
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


    /***
     * 新建组织
     * @throws Exception
     */
    public void addSysDept(SysDept sysDept) throws Exception {

        addSysDeptParamCheck(sysDept);

        int result = sysDeptMapper.insertSelective(sysDept);
        if (result != 1) {
            throw new BusinessException("add_sys_dept_error", "新建组织失败,受影响行数不一致!");
        }
    }

    private void addSysDeptParamCheck(SysDept sysDept) throws Exception {
        if (StringUtils.isEmpty(sysDept.getFullname())) {
            throw new BusinessException("add_sys_dept_error", "新建组织部门全称不能为空!");
        }

        Example fullnameExample = new Example(SysUser.class);
        fullnameExample.createCriteria().andEqualTo("fullname", sysDept.getFullname());
        List<SysDept> fullnameDeptResult = sysDeptMapper.selectByExample(fullnameExample);
        if (!fullnameDeptResult.isEmpty()) {
            throw new BusinessException("add_sys_dept_error", "新建组织失败,组织全称已存在，请更换名称!");
        }


        if (StringUtils.isEmpty(sysDept.getSimplename())) {
            throw new BusinessException("add_sys_dept_error", "新建组织部门简称称不能为空!");
        }

        Example simplenameExample = new Example(SysUser.class);
        simplenameExample.createCriteria().andEqualTo("simplename", sysDept.getSimplename());
        List<SysDept> simplenameDeptResult = sysDeptMapper.selectByExample(simplenameExample);
        if (!simplenameDeptResult.isEmpty()) {
            throw new BusinessException("add_sys_dept_error", "新建组织失败,组织简称已存在，请更换名称!");
        }
    }


    /**
     * 按条件分页查询用户信息
     *
     * @param querySysUserParam
     * @return
     * @throws Exception
     */
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
     * 按条件分页查询菜单信息（后台菜单管理，非按权限展示不同的菜单）
     *
     * @param sysMenuParam
     * @return
     * @throws Exception
     */
    public PageInfo<SysMenu> querySysMenu(SysMenuParam sysMenuParam) throws Exception {
        PageHelper.startPage(sysMenuParam.getPageNum(), sysMenuParam.getPageSize());

        Example example = new Example(SysMenu.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotEmpty(sysMenuParam.getName())) {
            criteria.andLike("name", sysMenuParam.getName());
        }

        if (null != sysMenuParam.getId()) {
            criteria.andEqualTo("id", sysMenuParam.getId());
        }

        if (StringUtils.isNotEmpty(sysMenuParam.getUrl())) {
            criteria.andLike("url", sysMenuParam.getUrl());
        }

        if (StringUtils.isNotEmpty(sysMenuParam.getDescription())) {
            criteria.andLike("description", sysMenuParam.getDescription());
        }

        List<SysMenu> sysUserList = sysMenuMapper.selectByExample(example);
        PageInfo<SysMenu> pageInfo = new PageInfo<SysMenu>(sysUserList);
        return pageInfo;
    }


    /**
     * 按条件分页查询权限信息（用于后台权限添加时展示）
     *
     * @param querySysPermissionParam
     * @return
     * @throws Exception
     */
    public PageInfo<SysPermission> querySysPermissions(QuerySysPermissionParam querySysPermissionParam) throws Exception {
        PageHelper.startPage(querySysPermissionParam.getPageNum(), querySysPermissionParam.getPageSize());

        Example example = new Example(SysPermission.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotEmpty(querySysPermissionParam.getPermissionName())) {
            criteria.andLike("permissionName", querySysPermissionParam.getPermissionName());
        }

        if (null != querySysPermissionParam.getId()) {
            criteria.andEqualTo("id", querySysPermissionParam.getId());
        }

        if (StringUtils.isNotEmpty(querySysPermissionParam.getPermissionCode())) {
            criteria.andEqualTo("permissionCode", querySysPermissionParam.getPermissionCode());
        }

        if (StringUtils.isNotEmpty(querySysPermissionParam.getPermissionDescription())) {
            criteria.andLike("permissionDescription", querySysPermissionParam.getPermissionDescription());
        }

        List<SysPermission> sysPermissionList = sysPermissionMapper.selectByExample(example);
        PageInfo<SysPermission> pageInfo = new PageInfo<SysPermission>(sysPermissionList);
        return pageInfo;
    }

    /**
     * 按条件分页查询角色信息（用于后台角色管理时展示）
     *
     * @param sysRoleParam
     * @return
     * @throws Exception
     */
    public PageInfo<SysRole> querySysPermissions(SysRoleParam sysRoleParam) throws Exception {
        PageHelper.startPage(sysRoleParam.getPageNum(), sysRoleParam.getPageSize());

        Example example = new Example(SysPermission.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotEmpty(sysRoleParam.getRoleName())) {
            criteria.andLike("roleName", sysRoleParam.getRoleName());
        }

        if (null != sysRoleParam.getId()) {
            criteria.andEqualTo("id", sysRoleParam.getId());
        }

        if (StringUtils.isNotEmpty(sysRoleParam.getRoleCode())) {
            criteria.andEqualTo("roleCode", sysRoleParam.getRoleCode());
        }

        if (StringUtils.isNotEmpty(sysRoleParam.getRoleDescription())) {
            criteria.andLike("roleDescription", sysRoleParam.getRoleDescription());
        }

        List<SysRole> sysRoleList = sysRoleMapper.selectByExample(example);
        PageInfo<SysRole> pageInfo = new PageInfo<SysRole>(sysRoleList);
        return pageInfo;
    }


    /**
     * 按条件分页查询组织信息（用于后台组织管理时展示）
     *
     * @param sysDeptParam
     * @return
     * @throws Exception
     */
    public PageInfo<SysDept> querySysPermissions(SysDeptParam sysDeptParam) throws Exception {
        PageHelper.startPage(sysDeptParam.getPageNum(), sysDeptParam.getPageSize());

        Example example = new Example(SysPermission.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotEmpty(sysDeptParam.getFullname())) {
            criteria.andLike("fullname", sysDeptParam.getFullname());
        }

        if (null != sysDeptParam.getId()) {
            criteria.andEqualTo("id", sysDeptParam.getId());
        }

        if (StringUtils.isNotEmpty(sysDeptParam.getSimplename())) {
            criteria.andEqualTo("simplename", sysDeptParam.getSimplename());
        }

        if (StringUtils.isNotEmpty(sysDeptParam.getDescription())) {
            criteria.andLike("description", sysDeptParam.getDescription());
        }

        List<SysDept> sysDeptList = sysDeptMapper.selectByExample(example);
        PageInfo<SysDept> pageInfo = new PageInfo<SysDept>(sysDeptList);
        return pageInfo;
    }


}
