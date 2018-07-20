package com.zoudong.permission.config.interceptor;


import com.zoudong.permission.annotation.RequiresPermissions;
import com.zoudong.permission.constant.Logical;
import com.zoudong.permission.constant.PermissionCoreConstant;
import com.zoudong.permission.constant.PermissionThreadLocal;
import com.zoudong.permission.exception.BusinessException;
import com.zoudong.permission.model.SysPermission;
import com.zoudong.permission.model.SysUser;
import com.zoudong.permission.utils.RedisUtils;
import com.zoudong.permission.utils.SpringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;

/**
 *
 * 核心权限检查
 * 需要权限所有请求都要从redis缓存里面取出来确认一下是否拥有访问权限
 */
public class TokenCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler) throws Exception {
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequiresPermissions requiresPermissions = handlerMethod.getMethodAnnotation(RequiresPermissions.class);
        if (requiresPermissions == null) {
            return true;
        }
        String token = httpServletRequest.getHeader("Authorization");
        if (!StringUtils.isBlank(token)) {
            //把token放入本地线程变量要用的时候方便
            PermissionThreadLocal.tokenThreadLocal.set(token);
            RedisUtils redisUtils = (RedisUtils) SpringUtils.getBean("redisUtils");
            SysUser superUserInfo = (SysUser) redisUtils.get(PermissionCoreConstant.permission_token + token);
            if (superUserInfo == null) {
                throw new BusinessException("token_error", "认证token无效,请重新登录后重试！");
            }
            //把userinfo放入本地线程变量要用的时候方便
            PermissionThreadLocal.userThreadLocal.set(superUserInfo);
            String[] perms = this.getAnnotationValue(requiresPermissions);
            if (Logical.AND.equals(requiresPermissions.logical())) {
                boolean isPerm;
                for (String perm : perms) {
                    isPerm = false;
                    for (SysPermission sysPermission : superUserInfo.getPermissionList()) {
                        if (perm.equals(sysPermission.getPermissionCode())) {
                            isPerm = true;
                            break;
                        }
                    }
                    if (isPerm != true) {
                        throw new BusinessException("token_error", "认证token权限不够,请重新配置后重试！");
                    }

                }
            }
            if (Logical.OR.equals(requiresPermissions.logical())) {
                for (String perm : perms) {
                    for (SysPermission sysPermission : superUserInfo.getPermissionList()) {
                        if (perm.equals(sysPermission.getPermissionCode())) {
                            return true;
                        }
                    }
                }
                throw new BusinessException("token_error", "认证token权限不够,请重新配置后重试！");
            }
            throw new BusinessException("token_error", "恭喜你绕过了所有安全机制,但还是token无效！");
        } else {
            return true;
        }
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler, Exception e) throws Exception {

    }


    protected String[] getAnnotationValue(Annotation a) {
        RequiresPermissions rpAnnotation = (RequiresPermissions) a;
        return rpAnnotation.value();
    }

}
