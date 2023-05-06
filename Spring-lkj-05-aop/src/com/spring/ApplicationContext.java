package com.spring;

import com.aaa.AppConfig;
import com.spring.anno.Autowired;
import com.spring.anno.Component;
import com.spring.anno.ComponentScan;
import com.spring.anno.Scope;
import javafx.application.Application;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationContext {
    private Class configClass;

    /**
     * 两个map，不是浪费内存？ 为什么不是一个map---单例，一个set/map只记录多例呢【这里另一个map，单例、多例都有？】；
     * singletonObjects 里面存储的是对象；
     * beanDefinitionMap 里面存储的是beanDefinition
     *
     */
    // 单例bean都在这里
    private Map<String, Object> singletonObjects = new ConcurrentHashMap<>();  // 单例池

    // Bean 都在这里
    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    // BeanPostProcessor 都在这里，--- beanDefinitionMap中也有
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    public ApplicationContext(Class configClass) {
        this.configClass = configClass;

        // 注入到map中
            // 用户创建对象的时候，我们应该扫描注解中的包，创建对象，依赖注入到map中；
        this.scanBeanDefinition(configClass);

        // 注入到单例池中
        for (Map.Entry<String, BeanDefinition> entry: beanDefinitionMap.entrySet()) {
            BeanDefinition beanDefinition = entry.getValue();
            if (beanDefinition.getScope().equals("singleton")) {
                // 放入单例池中
                String beanName = entry.getKey();
                Object bean = createBean(beanName, beanDefinition);  // 这里创建了单例bean
                singletonObjects.put(beanName, bean);
            }
        }

    }

    private void scanBeanDefinition(Class configClass) {
        // 1.获取需要扫描的包名
            // 读取configClass对应的注解中的值---获取包名；  【为什么需要强转】
        ComponentScan componentScanAnnotation = (ComponentScan) configClass.getDeclaredAnnotation(ComponentScan.class);
        String path = componentScanAnnotation.value();  // com.aaa.service
        path = path.replace(".", "/");
        System.out.println(path);

        // 2.扫描
        // 2.1 通过包名 得到file（文件、文件夹）
            // 获取类加载器 根据上面扫描的路径，得到file
        ClassLoader classLoader = ApplicationContext.class.getClassLoader();
        URL resource = classLoader.getResource(path);  // 相对路径，相对于classpath
        File file = new File(resource.getFile());
//        System.out.println(file);

        // 2.2 遍历file
        this.readFile(file);
    }
    /**
     * 类加载器
     * Bootstrap --- jre/lib
     * Ext --- jre/ext/lib
     * App --- classpath  "F:\NewMavenProject\SpringYuanMa\out\production\Spring-lkj"
     *
     * -classpath
     *  "F:\NewMavenProject\SpringYuanMa\out\production\Spring-lkj" com.aaa.Test
     *
     */
    /**
     * file --- 文件、文件夹 的一个集合
     * 思想，每次遍历file，如果是文件夹，则递归；否则继续遍历；
     * @param file
     */
    private void readFile(File file) {
        File[] files = file.listFiles();
        for (File f: files) {
            // 如果是文件夹 递归
            if (f.isDirectory()) {
                this.readFile(f);
            } else {
//                System.out.println(f);
                // 具体操作 注入map中
                injectBeanDefinitionMap(f);
            }
        }
    }

    /**
     * 向beanDefinitionMap中设置值
     * @param f
     */
    private void injectBeanDefinitionMap(File f){
        // 1.得到class名称
        // F:\NewMavenProject\SpringYuanMa\out\production\Spring-lkj\com\aaa\service\UserService.class
        String fileName = f.getAbsolutePath();
        String className = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
        className = className.replace("\\", ".");

        // 2.如果class对象 中有我们需要的Component.class注解，则操作
        ClassLoader classLoader = ApplicationContext.class.getClassLoader();
        try {
            Class<?> clazz = classLoader.loadClass(className);
            if (clazz.isInterface()) {  // 因为接口，我们不需要注入
//                continue;  // 之前在方法中，是continue；使用return 会跳出方法readFile
                return ;  // 现在封装成了函数，就不会跳出readFile方法了
            }

            /**
             * 具体操作：
             * 1.有component.class
             * 2.是单例模式
             * 3.多例模式
             * 4.lazy加载？
             *
             */
            // 2.1 有component.class 说明一个bean
            if (clazz.isAnnotationPresent(Component.class)) {

                /**
                 * BeanPostProcessor操作
                 * 不能放在注入单例池中【也就是ApplicationContext初始化中】，
                 * 因为会调用creatBean---里面使用了beanPostProcessor对象--此时还没有初始化，所以出现null
                 *
                 * 注意，这样的操作，则list、和singleMap中都会有此对象 而实际singleMap中不需要此对象；
                 */
                // 加入beanPostProcessor对象到list中，他是在injectBeanDefinitionMap中创建的，我觉得没有必要
                // 通过字节码判断是不同是同一个类、子类； instanceOf 比较的是对象；
                if (BeanPostProcessor.class.isAssignableFrom(clazz)) {  // 注意前后顺序
                    BeanPostProcessor beanPostProcessor = null;
                    try {
                        beanPostProcessor = (BeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                    beanPostProcessorList.add(beanPostProcessor);
                }

                // 得到注解的value
                /**
                 * @Controller 名称eg：helloController 【自己指定、或者默认---小驼峰】
                 * 这里就用自己指定  【这里注意】
                 */
                Component componentAnnotation = clazz.getDeclaredAnnotation(Component.class);
                String beanName = componentAnnotation.value();

                // 2.2判断是单例还是多例
                BeanDefinition beanDefinition = new BeanDefinition();
                beanDefinition.setClazz(clazz);
                // 有，查看值
                if (clazz.isAnnotationPresent(Scope.class)) {
                    Scope scopeAnnotation = clazz.getDeclaredAnnotation(Scope.class);
                    // scope没有写，应该也是单例
                    beanDefinition.setScope(scopeAnnotation.value());
                } else {
                    // 默认是单例
                    beanDefinition.setScope("singleton");
                }

                // 只要是bean，就放入；
                beanDefinitionMap.put(beanName, beanDefinition);  // clazz 、 scope
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 对外提供的
     * @param beanName
     * @return
     */
    public Object getBean(String beanName) {
        // 有无此bean
        if (beanDefinitionMap.containsKey(beanName)) {
            // 判断是单例 还是多例
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
                // scope没有写，应该也是单例 【我在上面放入beanDefinitionMap 做了处理】
            if (beanDefinition.getScope().equals("singleton")) {
                // 单例，从池中获取
                return singletonObjects.get(beanName);
            } else {
                // 多例， 创建
                return createBean(beanName, beanDefinition);  // 注意不是根据beanName创建 ，而是根据clazz创建
            }
        } else {
            // 没有此bean
            throw new NullPointerException("没有这个bean：" + beanName);
        }
    }

    /**
     * 单例---构造器的时候，调用；
     * 多例---getBean的时候，都会调用的方法
     * @return
     */
    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class clazz = beanDefinition.getClazz();

        // 这个做法肯定不行；
        /**
         * userService --- bookService属性；
         * 而bookService--- bookDao属性；
         *
         * 我这里只是创建bookService对象，注入bookService属性
         * 而bookService中bookDao属性可能还没有赋值；【看userService 和bookService谁先被创建对象】
         *
         */

        Object o = null;
        try {
            o = clazz.getConstructor().newInstance();

            // 这个做法肯定不行；
            /**
             * userService --- bookService属性；
             * 而bookService--- bookDao属性；
             *
             * 我这里只是创建bookService对象，注入bookService属性
             * 而bookService中bookDao属性可能还没有赋值；【看userService 和bookService谁先被创建对象】
             */

            // 遍历o对象的所有属性；依次赋值---注入
            /**
             * 可能出现，当前遍历的是第一个对象UserService，为他的属性A---bookService赋值，
             *      属性可能对应的对象B---BookService是单例、多例，此时对应的对象B还没有赋值；
             *          如果是单例，getBean 得到的是 return singletonObjects.get(beanName); null
             *          如果是多例，getBean 得到的是 return createBean(beanDefinition); 【死循环---循环依赖】
             */
            Field[] fields = clazz.getDeclaredFields();
            for (Field field: fields) {
                // 1.得到field对应的对象，
                // 1.1查看是否有@Autowired
                    // 【注意】，我这里使用field.getClass.isAnnotationPresent(Autowired.class) 总是得到的是null
                if (field.isAnnotationPresent(Autowired.class)) {
                    // 如果这里field对应的是 beanDefinitionMap中没有的，就会报错；说明没有手动注入此对象；
                    // 实际开发的时候，你会手动注入xxService，xxDao
                    // 【注意】 这里我是查找beanDefinitionMap中是否有beanName为 这个属性名；
                        // 缺点，不是按照clazz查找的；所以，只要我的属性名 和 对应的对象的@Component("")中的名称不一样，就会报错；
                    /**
                     *      @Autowired
                     *     private UserDao userDao;
                     *     // 所以就有了，bytype，byName 也就是根据UserDao---类型， userDao名称去容器中查找
                     */
                    Object bean = getBean(field.getName());
                    // 1.2赋值
                    field.setAccessible(true);
                    field.set(o, bean);
                }
            }

            // 实现aware
            if (o instanceof BeanNameAware) {
                ((BeanNameAware) o).setBeanName(beanName);
            }

            // ---beanPostProcessor
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                o = beanPostProcessor.postProcessBeforeInitialization(o, beanName);
            }
            // 实现initializingBean
            if (o instanceof InitializingBean) {
                ((InitializingBean) o).afterPropertiesSet();
            }

            // ---beanPostProcessor
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                o = beanPostProcessor.postProcessAfterInitialization(o, beanName);
            }
            /**
             * 在beanPostProcessor的after操作，  返回代理对象；
             */



        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return o;
    }
}
