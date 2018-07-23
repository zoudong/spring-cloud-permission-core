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
import com.zoudong.permission.service.api.UserService;
import com.zoudong.permission.utils.RedisUtils;
import com.zoudong.permission.utils.jwt.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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



    /**
     * 无状态登录
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


    @Override
    public PageInfo<SysUser> queryAllSysUser(QuerySysUserParam querySysUserParam) throws Exception {
        PageHelper.startPage(querySysUserParam.getPageNum(), querySysUserParam.getPageSize());
        List<SysUser> sysUserList = sysUserMapper.selectAll();
        PageInfo<SysUser> pageInfo = new PageInfo<>(sysUserList);
        return pageInfo;
    }


    /**
     * 注销用户(后面加个认证注解登录后的才有权注销)
     * @param token
     * @return
     * @throws Exception
     */
    public boolean loginOut(String token) throws Exception {
        if(StringUtils.isEmpty(token)){
            throw new BusinessException("token_error","token不能为空");
        }
        if(redisUtils.get(PermissionCoreConstant.permission_token+token)!=null){
            redisUtils.remove(PermissionCoreConstant.permission_token+token);
            return true;
        }else{
            throw new BusinessException("token_error","查询用户token失败");
        }
    }

    /**
     * 菜单缓存起来(增加菜单信息后更新菜单信息缓存)
     * @return
     * @throws Exception
     */
    public void cacheSysMenu() throws Exception {
        List<SysMenu> permSysMenus=new ArrayList<>();
        if(!permSysMenus.isEmpty()){
            List<SysMenu> sysMenus=sysMenuMapper.selectAll();
            redisUtils.set(PermissionCoreConstant.permission_menu,sysMenus);
        }else{
            throw new BusinessException("cacheMenu_error","查询系统菜失败");
        }
    }
    /**
     * 根据不同的token的权限从缓存里面加载不同的菜单
     * @param token
     * @return
     * @throws Exception
     */
    public List<SysMenu> queryAdminMenu(String token) throws Exception {
        if(StringUtils.isEmpty(token)){
            throw new BusinessException("token_error","token不能为空");
        }
        List<SysMenu> permSysMenus=new ArrayList<>();
        if(redisUtils.get(PermissionCoreConstant.permission_token+token)==null){
            throw new BusinessException("token_error","token已经被注销请重新登录");
        }
        SysUser sysUser= (SysUser) redisUtils.get(PermissionCoreConstant.permission_token+token);
        List<SysPermission> userSysPermissions=sysUser.getPermissionList();
        if(redisUtils.get(PermissionCoreConstant.permission_menu)!=null){
            List<SysMenu> sysMenus= (List<SysMenu>)redisUtils.get(PermissionCoreConstant.permission_menu);
            for(SysMenu sysMenu:sysMenus){
                for(SysPermission sysPermission:userSysPermissions){
                    if(sysMenu.getPermissionCode().equals(sysPermission.getPermissionCode())){
                        permSysMenus.add(sysMenu);
                    }
                }
            }
        }else{
            throw new BusinessException("token_error","查询用户token失败");
        }
        return permSysMenus;
    }


    /**
     * 添加权限
     * @param sysPermission
     * @throws Exception
     */
    public void addPermission(SysPermission sysPermission) throws Exception {
        sysPermission.setId(null);
        sysPermission.setVersion(0L);
        sysPermission.setCreateTime(new Date());
        int result=sysPermissionMapper.insert(sysPermission);
        if(result!=1){
            throw new BusinessException("add_permission_error","添加权限失败,受影响行数为0!");
        }
    }
    /**
     * 添加角色
     * @param sysRole
     * @throws Exception
     */
    public void addRole(SysRole sysRole) throws Exception {
        sysRole.setId(null);
        sysRole.setVersion(0L);
        sysRole.setCreateTime(new Date());
        int result=sysRoleMapper.insert(sysRole);
        if(result!=1){
            throw new BusinessException("add_role_error","添加角色失败,受影响行数为0!");
        }
    }

    /**
     * 为角色分配权限（1个角色可同时分配多个权限）
     * @param roleId
     * @param permissionIds
     * @throws Exception
     */
    public void addSysRolePermission(Long roleId,List<Long> permissionIds) throws Exception {
        List<SysRolePermission> sysRolePermissions=new ArrayList<SysRolePermission>();
        for(Long permissionId:permissionIds){
            SysRolePermission sysRolePermission=new SysRolePermission();
            sysRolePermission.setRoleId(roleId);
            sysRolePermission.setPermissionId(permissionId);
            sysRolePermission.setCreateTime(new Date());
            sysRolePermission.setVersion(0L);
            sysRolePermissions.add(sysRolePermission);
        }
        int result=sysRolePermissionMapper.insertList(sysRolePermissions);
        if(result!=sysRolePermissions.size()){
            throw new BusinessException("add_role_error","添加角色权限关联失败,受影响行数为0!");
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
        int result=sysMenuMapper.insert(sysMenu);
        if(result!=1){
            throw new BusinessException("add_role_error","添加菜单失败,受影响行数为0!");
        }
    }

    /***
     * 为菜单挂靠需要的权限（只有菜单才需要挂靠权限,不是每一个权限都要挂靠到菜单）(需要什么权限能展示这个菜单，与菜单内容的权限信息要一致)
     * @throws Exception
     */
    public void addSysMenuPermission(Long menuId,String permissionId) throws Exception {
        SysMenu sysMenu=new SysMenu();
        sysMenu.setId(menuId);
        sysMenu.setPermissionCode(permissionId);
        int result=sysMenuMapper.updateByPrimaryKeySelective(sysMenu);
        if(result!=1){
            throw new BusinessException("add_role_error","为菜单挂靠关联权限失败,受影响行数为0!");
        }
    }


}
