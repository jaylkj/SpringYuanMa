package com.aaa.service.impl;


import com.aaa.service.UserService;
import com.spring.anno.Component;
import com.spring.anno.Scope;

@Component("userService")
//@Scope("prototype")
//@Scope("singleton")
@Scope()
public class UserServiceImpl implements UserService {

}
