package com.aaa;

import com.spring.BeanPostProcessor;
import com.spring.anno.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 这个是对bean做的一些通用操作；
 *   如果想对某些bean做操作，可以加上判断
 *
 * 需要加上@Component 因为这个在spring中是通过对象，调用方法的【之前的aware回调，是直接通过接口，设置值的，不需要对象，所以不需要@Component】
 *
 */
@Component("lkjBeanPostProcessor")
public class LkjBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("postProcessBeforeInitialization--初始化前");
        if ("userService".equals(beanName)) {
            System.out.println("对userService做的操作beforInitialization");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("postProcessBeforeInitialization------初始化后");

        // 实际，应该是查看注解，，，来判断是否需要返回代理对象；
        if ("userService".equals(beanName)) {
            Object proxyInstance = Proxy.newProxyInstance(LkjBeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("代理逻辑---切点");
                    return method.invoke(bean, args);
                }
            });

            return proxyInstance;
        }
        return bean;
    }
}
