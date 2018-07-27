package com.zoudong.permission.service.api.role;

import com.github.pagehelper.PageInfo;
import com.zoudong.permission.model.SysRole;
import com.zoudong.permission.param.role.SysRoleParam;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author zd
 * @description class
 * @date 2018/7/27 10:50
 */
public interface RoleService {
    /**
     * 添加角色
     *
     * @param sysRole
     * @throws Exception
     */
    void addRole(SysRole sysRole) throws Exception;

    /**
     * 删除角色
     *
     * @param ids
     * @throws Exception
     */
    @Transactional
    void deleteRoles(List<Long> ids) throws Exception;

    /**
     * 按条件分页查询角色信息（用于后台角色管理时展示）
     *
     * @param sysRoleParam
     * @return
     * @throws Exception
     */
    PageInfo<SysRole> querySysRoles(SysRoleParam sysRoleParam) throws Exception;

    /**
     * 为角色分配权限（1个角色可同时分配多个权限）
     *
     * @param roleId
     * @param permissionIds
     * @throws Exception
     */
    @Transactional
    void addSysRolePermission(Long roleId, List<Long> permissionIds) throws Exception;
}
