package com.zoudong.permission.service.api.permission;

import com.github.pagehelper.PageInfo;
import com.zoudong.permission.model.SysPermission;
import com.zoudong.permission.param.permission.QuerySysPermissionParam;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author zd
 * @description class
 * @date 2018/7/27 10:50
 */
public interface PermissionService {
    /**
     * 添加权限
     *
     * @param sysPermission
     * @throws Exception
     */
    void addPermission(SysPermission sysPermission) throws Exception;

    /**
     * 删除权限(一般不使用)
     *
     * @param ids
     * @throws Exception
     */
    @Transactional
    void deletePermissions(List<Long> ids) throws Exception;

    /**
     * 按条件分页查询权限信息（用于后台权限添加时展示）
     *
     * @param querySysPermissionParam
     * @return
     * @throws Exception
     */
    PageInfo<SysPermission> querySysPermissions(QuerySysPermissionParam querySysPermissionParam) throws Exception;
}
