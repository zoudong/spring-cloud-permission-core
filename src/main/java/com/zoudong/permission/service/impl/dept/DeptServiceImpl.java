package com.zoudong.permission.service.impl.dept;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zoudong.permission.exception.BusinessException;
import com.zoudong.permission.mapper.*;
import com.zoudong.permission.model.*;
import com.zoudong.permission.param.dept.SysDeptParam;
import com.zoudong.permission.service.api.dept.DeptService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @author zd
 * @description class
 * @date 2018/6/15 17:41
 */
@Slf4j
@Service
public class DeptServiceImpl implements DeptService {

    @Autowired
    private SysDeptMapper sysDeptMapper;


    /***
     * 新建组织
     * @throws Exception
     */
    @Override
    public void addSysDept(SysDept sysDept) throws Exception {

        addSysDeptParamCheck(sysDept);

        int result = sysDeptMapper.insertSelective(sysDept);
        if (result != 1) {
            throw new BusinessException("add_sys_dept_error", "新建组织失败,受影响行数不一致!");
        }
    }

    private void addSysDeptParamCheck(SysDept sysDept) throws Exception {
        if (StringUtils.isEmpty(sysDept.getFullname())) {
            throw new BusinessException("add_sys_dept_error", "新建组织部门全称不能为空!");
        }

        Example fullnameExample = new Example(SysUser.class);
        fullnameExample.createCriteria().andEqualTo("fullname", sysDept.getFullname());
        List<SysDept> fullnameDeptResult = sysDeptMapper.selectByExample(fullnameExample);
        if (!fullnameDeptResult.isEmpty()) {
            throw new BusinessException("add_sys_dept_error", "新建组织失败,组织全称已存在，请更换名称!");
        }


        if (StringUtils.isEmpty(sysDept.getSimplename())) {
            throw new BusinessException("add_sys_dept_error", "新建组织部门简称称不能为空!");
        }

        Example simplenameExample = new Example(SysUser.class);
        simplenameExample.createCriteria().andEqualTo("simplename", sysDept.getSimplename());
        List<SysDept> simplenameDeptResult = sysDeptMapper.selectByExample(simplenameExample);
        if (!simplenameDeptResult.isEmpty()) {
            throw new BusinessException("add_sys_dept_error", "新建组织失败,组织简称已存在，请更换名称!");
        }
    }



    /**
     * 按条件分页查询组织信息（用于后台组织管理时展示）
     *
     * @param sysDeptParam
     * @return
     * @throws Exception
     */
    @Override
    public PageInfo<SysDept> querySysDepts(SysDeptParam sysDeptParam) throws Exception {
        PageHelper.startPage(sysDeptParam.getPageNum(), sysDeptParam.getPageSize());

        Example example = new Example(SysPermission.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotEmpty(sysDeptParam.getFullname())) {
            criteria.andLike("fullname", sysDeptParam.getFullname());
        }

        if (null != sysDeptParam.getId()) {
            criteria.andEqualTo("id", sysDeptParam.getId());
        }

        if (StringUtils.isNotEmpty(sysDeptParam.getSimplename())) {
            criteria.andEqualTo("simplename", sysDeptParam.getSimplename());
        }

        if (StringUtils.isNotEmpty(sysDeptParam.getDescription())) {
            criteria.andLike("description", sysDeptParam.getDescription());
        }

        List<SysDept> sysDeptList = sysDeptMapper.selectByExample(example);
        PageInfo<SysDept> pageInfo = new PageInfo<SysDept>(sysDeptList);
        return pageInfo;
    }


}
