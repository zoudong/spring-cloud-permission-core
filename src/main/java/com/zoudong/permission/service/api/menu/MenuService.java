package com.zoudong.permission.service.api.menu;

import com.github.pagehelper.PageInfo;
import com.zoudong.permission.model.SysMenu;
import com.zoudong.permission.param.menu.SysMenuParam;

import java.util.List;

/**
 * @author zd
 * @description class
 * @date 2018/7/27 10:49
 */
public interface MenuService {
    /**
     * 菜单缓存起来(增加菜单信息后更新菜单信息缓存)
     *
     * @return
     * @throws Exception
     */
    void cacheSysMenu() throws Exception;

    /**
     * 根据不同的token的权限从缓存里面加载不同的菜单
     *
     * @param token
     * @return
     * @throws Exception
     */
    List<SysMenu> queryAdminMenu(String token) throws Exception;

    /***
     * 新增菜单
     * @param sysMenu
     * @throws Exception
     */
    void addSysMenu(SysMenu sysMenu) throws Exception;

    /***
     * 为菜单挂靠需要的权限（只有菜单才需要挂靠权限,不是每一个权限都要挂靠到菜单）(需要什么权限能展示这个菜单，与菜单内容的权限信息要一致)
     * @throws Exception
     */
    void addSysMenuPermission(Long menuId, String permissionId) throws Exception;

    /**
     * 按条件分页查询菜单信息（后台菜单管理，非按权限展示不同的菜单）
     *
     * @param sysMenuParam
     * @return
     * @throws Exception
     */
    PageInfo<SysMenu> querySysMenu(SysMenuParam sysMenuParam) throws Exception;
}
