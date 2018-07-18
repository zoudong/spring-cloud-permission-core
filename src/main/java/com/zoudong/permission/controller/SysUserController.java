package com.zoudong.permission.controller;

import com.github.pagehelper.PageInfo;
import com.zoudong.permission.annotation.RequiresPermissions;
import com.zoudong.permission.constant.Logical;
import com.zoudong.permission.exception.BusinessException;
import com.zoudong.permission.mapper.SysResourceMapper;
import com.zoudong.permission.model.SysUser;
import com.zoudong.permission.param.user.login.SysUserLoginParam;
import com.zoudong.permission.param.user.query.QuerySysUserParam;
import com.zoudong.permission.result.base.BaseResult;
import com.zoudong.permission.result.user.SysUserVO;
import com.zoudong.permission.service.api.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import static com.zoudong.permission.result.base.ResultUtil.fillSuccesData;

/**
 * @author zd
 * @description class
 * @date 2018/6/4 17:47
 */
@Slf4j
@RestController
public class SysUserController {
    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysResourceMapper sysResourceMapper;

    /**
     * 未登录，shiro应重定向到登录界面，此处返回未登录状态信息由前端控制跳转页面
     * @return
     */
    @RequestMapping(value = "/permission/unAuth")
    @ResponseBody
    public Object unauth() {
        throw new BusinessException("unAuth","token认证失败,请重新登录。");
    }

    @RequiresPermissions(value={"1","2"},logical = Logical.OR)
    @RequestMapping(value = "/permission/querySysUserByPage", method = RequestMethod.POST)
    public  BaseResult<PageInfo<SysUserVO>> test(@Valid @RequestBody QuerySysUserParam querySysUserParam, HttpServletRequest request, HttpServletResponse response)throws Exception {
       /* try {*/
        log.info("开始分页查询全部用户:{}", querySysUserParam);
        PageInfo<SysUser> pageInfo = sysUserService.queryAllSysUser(querySysUserParam);
        PageInfo<SysUserVO> pageInfoVO=new PageInfo<>();
        BeanUtils.copyProperties(pageInfoVO,pageInfo);
        log.info("结束分页查询全部用户:{}", pageInfoVO);
        return fillSuccesData(pageInfoVO);
       /* } catch (BusinessException e) {
            log.info("业务异常test:{}", e.getMessage());
            e.printStackTrace();
            return ResultUtil.fillErrorMsg(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            log.info("系统异常test:{}", e.getMessage());
            e.printStackTrace();
            return ResultUtil.error();
        }*/
    }
    @RequestMapping(value = "/permission/apiLogin", method = RequestMethod.POST)
    public BaseResult<String> apiLogin(@Valid @RequestBody SysUserLoginParam sysUserLoginParam)throws Exception {
        log.info("开始用户API接口登录:{}", sysUserLoginParam);
        String jwtToken = sysUserService.apiLogin(sysUserLoginParam);
        log.info("结束用户API接口登录:{}", jwtToken);
        return fillSuccesData(jwtToken);

    }




}
