package com.zoudong.permission.model;

import java.util.Date;
import javax.persistence.*;

@Table(name = "sys_permission")
public class SysPermission {
    /**
     * 主键id
     */
    @Id
    private Long id;

    /**
     * 权限名称
     */
    @Column(name = "permission_name")
    private String permissionName;

    /**
     * 权限描述
     */
    @Column(name = "permission_description")
    private String permissionDescription;

    /**
     * 权限编码
     */
    @Column(name = "permission_code")
    private String permissionCode;

    /**
     * 0前台、1后台
     */
    @Column(name = "permission_type")
    private String permissionType;

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

    /**
     * 乐观锁版本号
     */
    private Long version;

    /**
     * 权限系统属主
     */
    @Column(name = "permission_system")
    private String permissionSystem;

    /**
     * 权限范围编码add、delete、update、query
     */
    @Column(name = "permission_limt")
    private String permissionLimt;

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
     * 获取权限名称
     *
     * @return permission_name - 权限名称
     */
    public String getPermissionName() {
        return permissionName;
    }

    /**
     * 设置权限名称
     *
     * @param permissionName 权限名称
     */
    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    /**
     * 获取权限描述
     *
     * @return permission_description - 权限描述
     */
    public String getPermissionDescription() {
        return permissionDescription;
    }

    /**
     * 设置权限描述
     *
     * @param permissionDescription 权限描述
     */
    public void setPermissionDescription(String permissionDescription) {
        this.permissionDescription = permissionDescription;
    }

    /**
     * 获取权限编码
     *
     * @return permission_code - 权限编码
     */
    public String getPermissionCode() {
        return permissionCode;
    }

    /**
     * 设置权限编码
     *
     * @param permissionCode 权限编码
     */
    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    /**
     * 获取0前台、1后台
     *
     * @return permission_type - 0前台、1后台
     */
    public String getPermissionType() {
        return permissionType;
    }

    /**
     * 设置0前台、1后台
     *
     * @param permissionType 0前台、1后台
     */
    public void setPermissionType(String permissionType) {
        this.permissionType = permissionType;
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

    /**
     * 获取乐观锁版本号
     *
     * @return version - 乐观锁版本号
     */
    public Long getVersion() {
        return version;
    }

    /**
     * 设置乐观锁版本号
     *
     * @param version 乐观锁版本号
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * 获取权限系统属主
     *
     * @return permission_system - 权限系统属主
     */
    public String getPermissionSystem() {
        return permissionSystem;
    }

    /**
     * 设置权限系统属主
     *
     * @param permissionSystem 权限系统属主
     */
    public void setPermissionSystem(String permissionSystem) {
        this.permissionSystem = permissionSystem;
    }

    /**
     * 获取权限范围编码add、delete、update、query
     *
     * @return permission_limt - 权限范围编码add、delete、update、query
     */
    public String getPermissionLimt() {
        return permissionLimt;
    }

    /**
     * 设置权限范围编码add、delete、update、query
     *
     * @param permissionLimt 权限范围编码add、delete、update、query
     */
    public void setPermissionLimt(String permissionLimt) {
        this.permissionLimt = permissionLimt;
    }
}