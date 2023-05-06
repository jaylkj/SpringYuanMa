package com.aaa;

import com.aaa.controller.HelloController;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(value = "com.aaa.controller")
public class TestMySpring {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext ioc = new AnnotationConfigApplicationContext(TestMySpring.class);
        HelloController helloController = ioc.getBean(HelloController.class);
        System.out.println(helloController);
    }

}
