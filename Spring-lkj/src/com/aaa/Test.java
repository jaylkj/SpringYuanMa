package com.aaa;

import com.spring.ApplicationContext;

public class Test {
    /**
     * 现在我写的，
     *  userService中 @Component("xxx") 里面必须加上名称
     *  不能通过UserService.class 获取bean
     * @param args
     */
    public static void main(String[] args) {
        ApplicationContext ioc = new ApplicationContext(AppConfig.class);
        System.out.println(ioc.getBean("userService"));
        System.out.println(ioc.getBean("userService"));
        System.out.println(ioc.getBean("userService"));


    }
}
