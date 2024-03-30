package com.fxh.HBPU.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fxh.HBPU.entity.Employee;
import com.fxh.HBPU.mapper.EmployeeMapper;
import com.fxh.HBPU.service.EmployeeService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
}
