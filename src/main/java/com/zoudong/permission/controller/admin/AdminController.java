package com.zoudong.permission.controller.admin;

import com.github.pagehelper.PageInfo;
import com.zoudong.permission.annotation.RequiresPermissions;
import com.zoudong.permission.constant.Logical;
import com.zoudong.permission.model.SysUser;
import com.zoudong.permission.param.login.SysUserLoginParam;
import com.zoudong.permission.param.user.QuerySysUserParam;
import com.zoudong.permission.rabitmq.SendMessageProcess;
import com.zoudong.permission.result.base.BaseResult;
import com.zoudong.permission.result.base.ResultUtil;
import com.zoudong.permission.result.user.SysUserVO;
import com.zoudong.permission.service.api.user.UserService;
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
public class AdminController {
    @Autowired
    private UserService userService;

    @Autowired
    private SendMessageProcess sendMessageProcess;

    /**
     * api接口登录拿取token
     * @param sysUserLoginParam
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/permission/apiLogin", method = RequestMethod.POST)
    public BaseResult<String> apiLogin(@Valid @RequestBody SysUserLoginParam sysUserLoginParam)throws Exception {
        log.info("开始用户API接口登录:{}", sysUserLoginParam);
        String jwtToken = userService.apiLogin(sysUserLoginParam);
        log.info("结束用户API接口登录:{}", jwtToken);
        return fillSuccesData(jwtToken);

    }

    /**
     * 按条件分页查询用户信息
     * @param querySysUserParam
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequiresPermissions(value={"1","2"},logical = Logical.OR)
    @RequestMapping(value = "/permission/querySysUserByPage", method = RequestMethod.POST)
    public  BaseResult<PageInfo<SysUserVO>> test(@Valid @RequestBody QuerySysUserParam querySysUserParam, HttpServletRequest request, HttpServletResponse response)throws Exception {
       /* try {*/
        log.info("开始分页查询全部用户:{}", querySysUserParam);
        PageInfo<SysUser> pageInfo = userService.queryAllSysUser(querySysUserParam);
        PageInfo<SysUserVO> pageInfoVO=new PageInfo<>();
        BeanUtils.copyProperties(pageInfo,pageInfoVO);
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

    @RequestMapping(value = "/permission/mqtest", method = RequestMethod.GET)
    public BaseResult<String> mqtest()throws Exception {
        log.info("开始测试mq消息发送:");
        sendMessageProcess.send();
        log.info("结束测试mq消息发送:");
        return ResultUtil.succes();

    }




}
