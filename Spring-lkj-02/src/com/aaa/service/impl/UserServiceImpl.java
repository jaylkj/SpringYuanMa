package com.aaa.service.impl;

import com.aaa.dao.BookDao;
import com.aaa.dao.UserDao;
import com.aaa.service.BookService;
import com.aaa.service.UserService;
import com.spring.anno.Autowired;
import com.spring.anno.Component;
import com.spring.anno.Scope;

@Component("userService")
//@Scope("prototype")
//@Scope("singleton")
@Scope()
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;

    public void test() {
        System.out.println(userDao);
    }

//    @Autowired
//    private BookService bookService;
//
//    public void test() {
//        System.out.println(bookService);
//        bookService.getBookDao();
//    }
}
