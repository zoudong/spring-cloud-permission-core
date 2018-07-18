package com.zoudong.permission.model;

import java.util.Date;
import javax.persistence.*;

@Table(name = "sys_resource")
public class SysResource {
    @Id
    private Long id;

    /**
     * 资源名称
     */
    @Column(name = "resource_code")
    private String resourceCode;

    /**
     * 资源编码
     */
    @Column(name = "resource_name")
    private String resourceName;

    /**
     * 资源挂靠权限
     */
    @Column(name = "permission_code")
    private String permissionCode;

    /**
     * 版本（乐观锁保留字段）
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

    /**
     * 资源状态 :  1:启用   0:不启用
     */
    private Integer status;

    /**
     * 资源备注
     */
    private String description;

    /**
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取资源名称
     *
     * @return resource_code - 资源名称
     */
    public String getResourceCode() {
        return resourceCode;
    }

    /**
     * 设置资源名称
     *
     * @param resourceCode 资源名称
     */
    public void setResourceCode(String resourceCode) {
        this.resourceCode = resourceCode;
    }

    /**
     * 获取资源编码
     *
     * @return resource_name - 资源编码
     */
    public String getResourceName() {
        return resourceName;
    }

    /**
     * 设置资源编码
     *
     * @param resourceName 资源编码
     */
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    /**
     * 获取资源挂靠权限
     *
     * @return permission_code - 资源挂靠权限
     */
    public String getPermissionCode() {
        return permissionCode;
    }

    /**
     * 设置资源挂靠权限
     *
     * @param permissionCode 资源挂靠权限
     */
    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    /**
     * 获取版本（乐观锁保留字段）
     *
     * @return version - 版本（乐观锁保留字段）
     */
    public Long getVersion() {
        return version;
    }

    /**
     * 设置版本（乐观锁保留字段）
     *
     * @param version 版本（乐观锁保留字段）
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

    /**
     * 获取资源状态 :  1:启用   0:不启用
     *
     * @return status - 资源状态 :  1:启用   0:不启用
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置资源状态 :  1:启用   0:不启用
     *
     * @param status 资源状态 :  1:启用   0:不启用
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 获取资源备注
     *
     * @return description - 资源备注
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置资源备注
     *
     * @param description 资源备注
     */
    public void setDescription(String description) {
        this.description = description;
    }
}