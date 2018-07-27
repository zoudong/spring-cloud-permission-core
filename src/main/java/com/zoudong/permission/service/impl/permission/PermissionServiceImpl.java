package com.zoudong.permission.service.impl.permission;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zoudong.permission.exception.BusinessException;
import com.zoudong.permission.mapper.*;
import com.zoudong.permission.model.*;
import com.zoudong.permission.param.permission.QuerySysPermissionParam;
import com.zoudong.permission.service.api.permission.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import java.util.Date;
import java.util.List;

/**
 * @author zd
 * @description class
 * @date 2018/6/15 17:41
 */
@Slf4j
@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private SysRolePermissionMapper sysRolePermissionMapper;

    @Autowired
    private SysPermissionMapper sysPermissionMapper;


    /**
     * 添加权限
     *
     * @param sysPermission
     * @throws Exception
     */
    @Override
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
    @Override
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
     * 按条件分页查询权限信息（用于后台权限添加时展示）
     *
     * @param querySysPermissionParam
     * @return
     * @throws Exception
     */
    @Override
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



}
