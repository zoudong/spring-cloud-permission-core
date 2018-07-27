package com.zoudong.permission.service.api.dept;

import com.github.pagehelper.PageInfo;
import com.zoudong.permission.model.SysDept;
import com.zoudong.permission.param.dept.SysDeptParam;

/**
 * @author zd
 * @description class
 * @date 2018/7/27 10:49
 */
public interface DeptService {
    /***
     * 新建组织
     * @throws Exception
     */
    void addSysDept(SysDept sysDept) throws Exception;

    /**
     * 按条件分页查询组织信息（用于后台组织管理时展示）
     *
     * @param sysDeptParam
     * @return
     * @throws Exception
     */
    PageInfo<SysDept> querySysDepts(SysDeptParam sysDeptParam) throws Exception;
}
