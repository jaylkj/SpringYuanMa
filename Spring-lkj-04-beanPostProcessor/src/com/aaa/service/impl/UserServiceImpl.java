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

    /**
     * // 因为UserService中实现了BeanNameAware接口；我们在spring中调用setBeanName方法，
     */
    private String myBeanName;

    public void test() {
        System.out.println("依赖注入---userDao对象" + userDao);
        System.out.println("aware --- myBeanName:" +  myBeanName);
    }

//    @Autowired
//    private BookService bookService;
//
//    public void test() {
//        System.out.println(bookService);
//        bookService.getBookDao();
//    }

    @Override
    public void setBeanName(String beanName) {
        myBeanName = beanName;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("InitializingBean---spring为此：" + myBeanName + " 初始化前做的操作");
    }
}
