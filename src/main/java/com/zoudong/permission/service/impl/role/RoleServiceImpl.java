package com.zoudong.permission.service.impl.role;

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
import com.zoudong.permission.param.role.SysRoleParam;
import com.zoudong.permission.param.user.QuerySysUserParam;
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
public class RoleServiceImpl{

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRolePermissionMapper sysRolePermissionMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

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
     * 按条件分页查询角色信息（用于后台角色管理时展示）
     *
     * @param sysRoleParam
     * @return
     * @throws Exception
     */
    public PageInfo<SysRole> querySysRoles(SysRoleParam sysRoleParam) throws Exception {
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


}
