package com.sohu.cache.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.sohu.cache.constant.AppUserTypeEnum;
import com.sohu.cache.constant.CacheCloudConstants;
import com.sohu.cache.entity.AppUser;
import com.sohu.cache.entity.LoginResult;
import com.sohu.cache.web.enums.AdminEnum;
import com.sohu.cache.web.enums.LoginEnum;
import com.sohu.cache.web.util.LoginUtil;

/**
 * 登录逻辑
 *
 * @author leifu
 * @Time 2014年6月12日
 */
@Controller
@RequestMapping("manage")
public class LoginController extends BaseController {

    /**
     * 用户登录界面
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ModelAndView init(HttpServletRequest request) {
        return new ModelAndView("manage/login");
    }

    /**
     * 用户登录
     *
     * @param userName 用户名
     * @param password 密码
     * @param isAdmin  是否勾选超级管理员选项,1是0否
     * @return
     */
    @RequestMapping(value = "/loginIn", method = RequestMethod.POST)
    public ModelAndView loginIn(HttpServletRequest request,
                                HttpServletResponse response, Model model, String userName, String password, boolean isAdmin) {
        // 登录结果
        LoginResult loginResult = new LoginResult();
        loginResult.setAdminEnum((isAdmin == true ? AdminEnum.IS_ADMIN : AdminEnum.NOT_ADMIN));
        loginResult.setLoginEnum(LoginEnum.LOGIN_WRONG_USER_OR_PASSWORD);

        AppUser userModel = null;
        if (CacheCloudConstants.SUPER_ADMIN_NAME.equals(userName) && CacheCloudConstants.SUPER_ADMIN_PASS.equals(password)) {
            userModel = userService.getByName(userName);
            loginResult.setLoginEnum(LoginEnum.LOGIN_SUCCESS);
        } else {
            if (LoginUtil.passportCheck(userName, password)) {
                // 同时要验证是否有cachecloud权限
                userModel = userService.getByName(userName);
                if (userModel != null && userModel.getType() != AppUserTypeEnum.NO_USER.value()) {
                    if (isAdmin) {
                        if (AppUserTypeEnum.ADMIN_USER.value().equals(userModel.getType())) {
                            loginResult.setLoginEnum(LoginEnum.LOGIN_SUCCESS);
                        } else {
                            loginResult.setLoginEnum(LoginEnum.LOGIN_NOT_ADMIN);
                        }
                    } else {
                        loginResult.setLoginEnum(LoginEnum.LOGIN_SUCCESS);
                    }
                } else {
                    // 用户不存在
                    loginResult.setLoginEnum(LoginEnum.LOGIN_USER_NOT_EXIST);
                }
            }
        }
        
        if (loginResult.getLoginEnum().equals(LoginEnum.LOGIN_SUCCESS)) {
            request.getSession().setAttribute(CacheCloudConstants.LOGIN_USER_SESSION_NAME, userModel);
        }
        model.addAttribute("success", loginResult.getLoginEnum().value());
        model.addAttribute("admin", loginResult.getAdminEnum().value());
        return new ModelAndView();
    }

    /**
     * 用户注销
     *
     * @param reqeust
     * @return
     */
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public ModelAndView logout(HttpServletRequest reqeust, HttpServletResponse response) {
        reqeust.getSession().removeAttribute(CacheCloudConstants.LOGIN_USER_SESSION_NAME);
        return new ModelAndView("redirect:/manage/login");
    }

}