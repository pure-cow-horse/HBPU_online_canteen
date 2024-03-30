package com.fxh.HBPU.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fxh.HBPU.entity.User;

public interface UserService extends IService<User> {
    //邮件发送人
    void sendMsg(String to, String subject, String text);
}
