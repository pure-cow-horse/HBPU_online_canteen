package com.fxh.HBPU.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fxh.HBPU.entity.AddressBook;
import com.fxh.HBPU.mapper.AddressBookMapper;
import com.fxh.HBPU.service.AddressBookService;
import org.springframework.stereotype.Service;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {

}
