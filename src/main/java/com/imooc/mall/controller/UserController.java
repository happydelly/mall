package com.imooc.mall.controller;

import com.imooc.mall.consts.MallConst;
import com.imooc.mall.enums.ResponseEnum;
import com.imooc.mall.form.UserRegisterForm;
import com.imooc.mall.form.UserLoginForm;
import com.imooc.mall.pojo.User;
import com.imooc.mall.service.IUserService;
import com.imooc.mall.vo.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@RestController
@Slf4j
public class UserController {

    @Autowired
    private IUserService userService;
    @PostMapping("/user/register")
    public ResponseVo register(@Valid @RequestBody UserRegisterForm userForm){

//        if(bindingResult.hasErrors()){
//            log.info("注册提交的参数有误,{}{}",
//                    bindingResult.getFieldError().getField(),
//                    bindingResult.getFieldError().getDefaultMessage());
//            return ResponseVo.error(ResponseEnum.PARAM_ERROR, bindingResult);
//        }

        User user = new User();
        BeanUtils.copyProperties(userForm,user);

        log.info("username={}",userForm.getUsername());
        return userService.register(user);
    }


    @PostMapping("/user/login")
    public ResponseVo<User> login(@Valid @RequestBody UserLoginForm userLoginForm,
                                  HttpSession session) {
        ResponseVo<User> userResponseVo = userService.login(userLoginForm.getUsername(), userLoginForm.getPassword());

        //设置Session
        session.setAttribute(MallConst.CURRENT_USER, userResponseVo.getData());
        log.info("/login sessionId={}", session.getId());

        return userResponseVo;
    }

    //session保存在内存里，改进版：token+redis
    @GetMapping("/user")
    public ResponseVo<User> userInfo(HttpSession session) {
        log.info("/user sessionId={}", session.getId());
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        return ResponseVo.success(user);
    }

    /**
     * {@link TomcatServletWebServerFactory} getSessionTimeoutInMinutes
     */
    @PostMapping("/user/logout")
    public ResponseVo logout(HttpSession session) {
        log.info("/user/logout sessionId={}", session.getId());
        session.removeAttribute(MallConst.CURRENT_USER);
        return ResponseVo.success();
    }

}
