package com.zoudong.permission.service.impl.menu;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zoudong.permission.constant.PermissionCoreConstant;
import com.zoudong.permission.exception.BusinessException;
import com.zoudong.permission.mapper.*;
import com.zoudong.permission.model.*;
import com.zoudong.permission.param.menu.SysMenuParam;
import com.zoudong.permission.utils.RedisUtils;
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
public class MenuServiceImpl{

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private SysMenuMapper sysMenuMapper;

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



}
