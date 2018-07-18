package com.zoudong.permission.model;

import java.util.Date;
import java.util.List;
import javax.persistence.*;

@Table(name = "sys_role")
public class SysRole {
    /**
     * 主键id
     */
    @Id
    private Long id;

    /**
     * 父角色id
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 角色名称
     */
    @Column(name = "role_name")
    private String roleName;

    /**
     * 角色名称
     */
    @Column(name = "role_code")
    private String roleCode;

    /**
     * 角色描述
     */
    @Column(name = "role_description")
    private String roleDescription;

    /**
     * 保留字段
     */
    private Long version;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 修改时间
     */
    @Column(name = "update_time")
    private Date updateTime;

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    @Transient
    private List<SysPermission> sysPermissions;


    public List<SysPermission> getSysPermissions() {
        return sysPermissions;
    }

    public void setSysPermissions(List<SysPermission> sysPermissions) {
        this.sysPermissions = sysPermissions;
    }

    /**
     * 获取主键id
     *
     * @return id - 主键id
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置主键id
     *
     * @param id 主键id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取父角色id
     *
     * @return parent_id - 父角色id
     */
    public Long getParentId() {
        return parentId;
    }

    /**
     * 设置父角色id
     *
     * @param parentId 父角色id
     */
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    /**
     * 获取角色名称
     *
     * @return role_name - 角色名称
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * 设置角色名称
     *
     * @param roleName 角色名称
     */
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    /**
     * 获取角色描述
     *
     * @return role_description - 角色描述
     */
    public String getRoleDescription() {
        return roleDescription;
    }

    /**
     * 设置角色描述
     *
     * @param roleDescription 角色描述
     */
    public void setRoleDescription(String roleDescription) {
        this.roleDescription = roleDescription;
    }

    /**
     * 获取保留字段
     *
     * @return version - 保留字段
     */
    public Long getVersion() {
        return version;
    }

    /**
     * 设置保留字段
     *
     * @param version 保留字段
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * 获取创建时间
     *
     * @return create_time - 创建时间
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 设置创建时间
     *
     * @param createTime 创建时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 获取修改时间
     *
     * @return update_time - 修改时间
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * 设置修改时间
     *
     * @param updateTime 修改时间
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}