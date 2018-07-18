package com.zoudong.permission.model;

import java.util.Date;
import javax.persistence.*;

@Table(name = "sys_dept")
public class SysDept {
    /**
     * 主键id
     */
    @Id
    private Long id;

    /**
     * 排序
     */
    private Integer num;

    /**
     * 父部门id
     */
    private Long pid;

    /**
     * 父级ids
     */
    private String pids;

    /**
     * 简称
     */
    private String simplename;

    /**
     * 全称
     */
    private String fullname;

    /**
     * 描述
     */
    private String description;

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
     * 获取排序
     *
     * @return num - 排序
     */
    public Integer getNum() {
        return num;
    }

    /**
     * 设置排序
     *
     * @param num 排序
     */
    public void setNum(Integer num) {
        this.num = num;
    }

    /**
     * 获取父部门id
     *
     * @return pid - 父部门id
     */
    public Long getPid() {
        return pid;
    }

    /**
     * 设置父部门id
     *
     * @param pid 父部门id
     */
    public void setPid(Long pid) {
        this.pid = pid;
    }

    /**
     * 获取父级ids
     *
     * @return pids - 父级ids
     */
    public String getPids() {
        return pids;
    }

    /**
     * 设置父级ids
     *
     * @param pids 父级ids
     */
    public void setPids(String pids) {
        this.pids = pids;
    }

    /**
     * 获取简称
     *
     * @return simplename - 简称
     */
    public String getSimplename() {
        return simplename;
    }

    /**
     * 设置简称
     *
     * @param simplename 简称
     */
    public void setSimplename(String simplename) {
        this.simplename = simplename;
    }

    /**
     * 获取全称
     *
     * @return fullname - 全称
     */
    public String getFullname() {
        return fullname;
    }

    /**
     * 设置全称
     *
     * @param fullname 全称
     */
    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    /**
     * 获取描述
     *
     * @return description - 描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置描述
     *
     * @param description 描述
     */
    public void setDescription(String description) {
        this.description = description;
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