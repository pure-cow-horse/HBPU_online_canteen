package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    // 假设您有一个方法来加密密码
    public static String encryptPassword(String passwordToHash) {
        String generatedPassword = null;
        try {
            // 创建MessageDigest实例，指定加密算法为SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // 对字符串进行加密，返回字节数组
            byte[] bytes = md.digest(passwordToHash.getBytes());
            // 将字节数组转换为十六进制字符串
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            // 获得加密后的字符串
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }

    /**
     * 发送手机短信验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        /*
        //获取手机号
        String phone = user.getPhone();

        if(StringUtils.isNotEmpty(phone)){
            //生成随机的4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code={}",code);

            //调用阿里云提供的短信服务API完成发送短信
            //SMSUtils.sendMessage("瑞吉外卖","",phone,code);

            //需要将生成的验证码保存到Session
            session.setAttribute(phone,code);

            return R.success("手机验证码短信发送成功");
        }

        return R.error("短信发送失败");*/

        //获取邮箱号
        //相当于发送短信定义的String to
        String email = user.getPhone();
        String subject = "湖理食堂";
        //StringUtils.isNotEmpty字符串非空判断
        if (StringUtils.isNotEmpty(email)) {
            //发送一个四位数的验证码,把验证码变成String类型
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            String text = "【湖理食堂】您好，您的登录验证码为：" + code + "，请尽快登录";
            log.info("验证码为：" + code);
            //发送短信
            userService.sendMsg(email, subject, text);
            //将验证码保存到session当中
            session.setAttribute(email, code);
            return R.success("验证码发送成功");
        }
        return R.error("验证码发送异常，请重新发送");
    }

    /**
     * 移动端用户登录
     * @param map
     * @param session
     * @return
     *//*
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {
        log.info(map.toString());

        //获取邮箱，用户输入的
        String phone = map.get("phone").toString();
        //获取验证码，用户输入的
        String code = map.get("code").toString();
        //获取session中保存的验证码
        Object sessionCode = session.getAttribute(phone);
        //如果session的验证码和用户输入的验证码进行比对,&&同时
        if (sessionCode != null && sessionCode.equals(code)) {
            //要是User数据库没有这个邮箱则自动注册,先看看输入的邮箱是否存在数据库
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            //获得唯一的用户，因为手机号是唯一的
            User user = userService.getOne(queryWrapper);
            //要是User数据库没有这个邮箱则自动注册
            if (user == null) {
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                //取邮箱的前五位为用户名
                user.setName(phone.substring(0, 6));
                userService.save(user);
            }
            //不保存这个用户名就登不上去，因为过滤器需要得到这个user才能放行，程序才知道你登录了
            session.setAttribute("user", user.getId());
            return R.success(user);
        }
        return R.error("登录失败");
    }*/
    @PostMapping("/login")
    public R<User> login(@RequestBody Map<String, String> map, HttpSession session) {
        log.info(map.toString());

        // 获取邮箱，用户输入的
        String phone = map.get("phone");
        // 获取密码，用户输入的
        String password = map.get("password");
        // 获取验证码，用户输入的
        String code = map.get("code");

        // 在数据库中查找用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, phone);
        User user = userService.getOne(queryWrapper);

        // 如果用户提供了密码，则进行密码登录验证
        if (password != null && !password.isEmpty()) {
            // 检查用户是否存在
            if (user == null) {
                return R.error("用户不存在");
            }
            // 检查用户是否设置了密码
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                return R.error("用户未设置密码");
            }
            // 验证密码（假设数据库中存储的是加密后的密码）
            if (user.getPassword().equals(encryptPassword(password))) {
                // 密码匹配，登录成功
                session.setAttribute("user", user.getId());
                return R.success(user);
            } else {
                // 密码不匹配，登录失败
                return R.error("密码错误");
            }
        } else if (code != null && !code.isEmpty()) {
            // 进行验证码登录验证
            Object sessionCode = session.getAttribute(phone);
            // 如果session的验证码和用户输入的验证码进行比对
            if (sessionCode != null && sessionCode.equals(code)) {
                // 验证码匹配，登录成功
                // 如果用户不存在，则自动注册
                if (user == null) {
                    user = new User();
                    user.setPhone(phone);
                    user.setStatus(1);
                    // 取邮箱的前五位为用户名
                    user.setName(phone.substring(0, 5));
                    userService.save(user);
                }
                session.setAttribute("user", user.getId());
                return R.success(user);
            } else {
                // 验证码不匹配，登录失败
                return R.error("验证码错误");
            }
        } else {
            // 未提供密码或验证码
            return R.error("请提供密码或验证码");
        }
    }

}
