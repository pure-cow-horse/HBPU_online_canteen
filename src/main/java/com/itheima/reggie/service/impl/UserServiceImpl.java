package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.mapper.UserMapper;
import com.itheima.reggie.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    //把yml配置的邮箱号赋值到from
    @Value("${spring.mail.username}")
    private String from;
    //发送邮件需要的对象
    @Autowired
    private JavaMailSender javaMailSender;

    //邮件发送人
    @Override
    public void sendMsg(String to, String subject, String text) {
        //发送简单邮件，简单邮件不包括附件等别的
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        //发送邮件
        javaMailSender.send(message);
    }
}
