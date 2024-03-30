package com.fxh.HBPU.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fxh.HBPU.common.R;
import com.fxh.HBPU.entity.User;
import com.fxh.HBPU.service.UserService;
import com.fxh.HBPU.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public String encryptPassword(String passwordToHash) {
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
     * 发送邮箱验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {


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
            log.info("密文密码=" + encryptPassword(password));
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


    /**
     * 更新用户信息，包括头像文件
     * @param user       接收前端传来的用户信息（除文件外）
     * @param avatarFile 接收头像文件
     * @param session    HttpSession提供当前用户ID
     * @return 返回统一结果类型
     */
    @PostMapping(value = "/update", consumes = {"multipart/form-data"})
    public R<String> updateUserInfo(
            User user,
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
            HttpSession session) {
        // 从session中获取当前登录用户的ID
        Long userId = (Long) session.getAttribute("user");
        if (userId == null) {
            // 用户未登录或会话已超时
            return R.error("未登录或会话已超时");
        }
        // 从数据库获取用户当前的完整信息
        User currentUser = userService.getById(userId);
        if (currentUser == null) {
            // 如果用户不存在
            return R.error("用户信息不存在");
        }

        // 更新用户信息，忽略邮箱字段
        currentUser.setName(user.getName());
        currentUser.setSex(user.getSex());
        currentUser.setIdNumber(user.getIdNumber());
        currentUser.setAvatar(user.getAvatar());

        // 如果用户提交了密码，更新加密后的密码
        if (StringUtils.isNotBlank(user.getPassword()) && StringUtils.isNotEmpty(user.getPassword())) {
            log.info("user.getPassword()=" + user.getPassword());
            String password = encryptPassword(user.getPassword());
            log.info("password=" + password);
            currentUser.setPassword(password);
        }

        // 保存更新后的用户信息到数据库
        boolean result = userService.updateById(currentUser);
        if (result) {
            // 更新成功
            return R.success("更新用户信息成功");
        } else {
            // 更新失败
            return R.error("更新用户信息失败");
        }
    }

    // 在UserController中添加以下方法

    /**
     * 获取用户信息
     * @param session 使用HttpSession从中获取当前登录用户ID
     * @return 返回用户信息
     */
    @GetMapping("/info")
    public R<User> getUserInfo(HttpSession session) {
        // 尝试从session中获取当前登录用户的ID
        Long userId = (Long) session.getAttribute("user");
        if (userId == null) {
            // 用户未登录或会话已超时
            return R.error("未登录或会话已超时");
        }

        try {
            // 使用用户ID查询信息
            User userInfo = userService.getById(userId);
            if (userInfo != null) {
                // 成功获取用户信息
                return R.success(userInfo);
            } else {
                // 用户信息不存在
                return R.error("用户信息不存在");
            }
        } catch (Exception e) {
            log.error("获取用户信息时发生错误", e);
            return R.error("获取用户信息失败");
        }
    }

    /**
     * 用户登出
     * @param session 使用HttpSession来清除用户会话
     * @return 返回响应状态
     */
    @PostMapping("/loginout")
    public R<String> logout(HttpSession session) {
        // 从session中移除用户ID
        session.removeAttribute("user");
        // 或者您可以使整个会话失效
        // session.invalidate();
        return R.success("登出成功");
    }
}
