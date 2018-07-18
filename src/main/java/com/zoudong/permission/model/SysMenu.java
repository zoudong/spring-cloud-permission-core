package com.zoudong.permission.model;

import java.util.Date;
import javax.persistence.*;

@Table(name = "sys_menu")
public class SysMenu {
    /**
     * 主键id
     */
    @Id
    private Long id;

    /**
     * 菜单编号
     */
    private String code;

    /**
     * 菜单父编号
     */
    private String pcode;

    /**
     * 当前菜单的所有父菜单编号
     */
    private String pcodes;

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * url地址
     */
    private String url;

    /**
     * 资源所需权限编码(挂靠的权限单选)
     */
    @Column(name = "permission_code")
    private String permissionCode;

    /**
     * 菜单排序号
     */
    private Integer num;

    /**
     * 菜单层级
     */
    private Integer levels;

    /**
     * 是否是菜单（1：是  0：不是）
     */
    private Integer ismenu;

    /**
     * 备注
     */
    private String description;

    /**
     * 菜单状态 :  1:启用   0:不启用
     */
    private Integer status;

    /**
     * 是否打开:    1:打开   0:不打开
     */
    private Integer isopen;

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
     * 获取菜单编号
     *
     * @return code - 菜单编号
     */
    public String getCode() {
        return code;
    }

    /**
     * 设置菜单编号
     *
     * @param code 菜单编号
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 获取菜单父编号
     *
     * @return pcode - 菜单父编号
     */
    public String getPcode() {
        return pcode;
    }

    /**
     * 设置菜单父编号
     *
     * @param pcode 菜单父编号
     */
    public void setPcode(String pcode) {
        this.pcode = pcode;
    }

    /**
     * 获取当前菜单的所有父菜单编号
     *
     * @return pcodes - 当前菜单的所有父菜单编号
     */
    public String getPcodes() {
        return pcodes;
    }

    /**
     * 设置当前菜单的所有父菜单编号
     *
     * @param pcodes 当前菜单的所有父菜单编号
     */
    public void setPcodes(String pcodes) {
        this.pcodes = pcodes;
    }

    /**
     * 获取菜单名称
     *
     * @return name - 菜单名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置菜单名称
     *
     * @param name 菜单名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取菜单图标
     *
     * @return icon - 菜单图标
     */
    public String getIcon() {
        return icon;
    }

    /**
     * 设置菜单图标
     *
     * @param icon 菜单图标
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * 获取url地址
     *
     * @return url - url地址
     */
    public String getUrl() {
        return url;
    }

    /**
     * 设置url地址
     *
     * @param url url地址
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 获取资源所需权限编码(挂靠的权限单选)
     *
     * @return permission_code - 资源所需权限编码(挂靠的权限单选)
     */
    public String getPermissionCode() {
        return permissionCode;
    }

    /**
     * 设置资源所需权限编码(挂靠的权限单选)
     *
     * @param permissionCode 资源所需权限编码(挂靠的权限单选)
     */
    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    /**
     * 获取菜单排序号
     *
     * @return num - 菜单排序号
     */
    public Integer getNum() {
        return num;
    }

    /**
     * 设置菜单排序号
     *
     * @param num 菜单排序号
     */
    public void setNum(Integer num) {
        this.num = num;
    }

    /**
     * 获取菜单层级
     *
     * @return levels - 菜单层级
     */
    public Integer getLevels() {
        return levels;
    }

    /**
     * 设置菜单层级
     *
     * @param levels 菜单层级
     */
    public void setLevels(Integer levels) {
        this.levels = levels;
    }

    /**
     * 获取是否是菜单（1：是  0：不是）
     *
     * @return ismenu - 是否是菜单（1：是  0：不是）
     */
    public Integer getIsmenu() {
        return ismenu;
    }

    /**
     * 设置是否是菜单（1：是  0：不是）
     *
     * @param ismenu 是否是菜单（1：是  0：不是）
     */
    public void setIsmenu(Integer ismenu) {
        this.ismenu = ismenu;
    }

    /**
     * 获取备注
     *
     * @return description - 备注
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置备注
     *
     * @param description 备注
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取菜单状态 :  1:启用   0:不启用
     *
     * @return status - 菜单状态 :  1:启用   0:不启用
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置菜单状态 :  1:启用   0:不启用
     *
     * @param status 菜单状态 :  1:启用   0:不启用
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 获取是否打开:    1:打开   0:不打开
     *
     * @return isopen - 是否打开:    1:打开   0:不打开
     */
    public Integer getIsopen() {
        return isopen;
    }

    /**
     * 设置是否打开:    1:打开   0:不打开
     *
     * @param isopen 是否打开:    1:打开   0:不打开
     */
    public void setIsopen(Integer isopen) {
        this.isopen = isopen;
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
}