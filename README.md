# SpringYuanMa

手写spring01-简单版（渐进式写，有助于了解spring大致的流程）
Spring-lkj: 第一部分 通过beanName获取bean，加入singleton、prototype
    

    1.建立一个java的module （不是maven）
    
    2.com.aaa.Test ，观察Test类，
    
    - 发现需要ApplicationContext类，提供getBean方法
    - 需要AppConfig类，指定具体扫描包
      - 需要注解@ComponentScan
    - 需要UserService类，及其子类
      - 也就需要@Component
    
    3.具体操作
        ApplicationContext类，初始化的时候，应该读取需要扫描的类，然后加载对应的bean放到map中
        3.1 构造器 public ApplicationContext(Class configClass)
            3.1.1 beanDefinitionMap赋值
                ① scanBeanDefinition(Class configClass)
                获取配置类的class，得到注解@ComponentScan中对应的值path 进而得到file；
                    ComponentScan componentScanAnnotation = (ComponentScan) configClass.getDeclaredAnnotation(ComponentScan.class);
                    String path = componentScanAnnotation.value();  // com.aaa.service
                    path = path.replace(".", "/");
    
                ② readFile(File file)
                遍历file（文件 + 文件夹）找到所有的class类


                ③ injectBeanDefinitionMap(File f)
                如果得到的class类，有@component---也就是bean，
                    根据单例、多例，lazy等等，赋值beanDefinitionMap【Map<String, BeanDefinition>】
    
            3.1.2 singletonObjects单例池---map【Map<String, Object>】赋值
                遍历beanDefinitionMap，找到所有的单例bean，赋值到singleonObjects中
    
        3.2 Object getBean(String beanName)
            先确定是单例，还是多例；
                单例直接从singletonObjects单例池中获取
                多例使用createBean
    
                Object createBean(BeanDefinition beanDefinition) 通过beanDefinition的class对象，创建bean
    
    4.再次回忆；
        ApplicationContext ioc = new ApplicationContext(AppConfig.class); 读取配置类AppConfig对应的注解，进行相应操作；
            具体： 构造器中ApplicationContext中 获取AppConfig对应的包扫描注解，得到对应的扫描包；

Spring-lkj-02第二部分 依赖注入

- 做了什么：上面creatBean的时候，只是普通的通过反射，创建对象；
- 少了：没有为对象的所有属性赋值；
  - 按道理应在createBean中创建对象的时候，为属性赋值；但是，此时可能有的属性对应的对象，我们手动创建它的时候，只是一个无参构造器创建，而它里面的内容为null---它的属性没有赋值；
  - 应该是遍历该对象的所有属性，然后依次赋值；但属性---对象可能没有赋值，里面的属性也可能没有赋值；

```java
操作：
  操作一：ApplicationContext构造器中
    ①scanBeanDefinition将所有配置了的对象，放入beanDefinitionMap【beanName---BeanDefinition对象（clazz、scope）】
    ②为singletonObjects赋值【clazz---bean真实对象】，通过主动调用creatBean；
        我就需要为clazz创建bean对象，为这个对象的所有属性赋值；
            问题：
                A {
                    B;
                }

                B {
                    C;
                }
                beanDefinitionMap中有 beanDefinition对象Aclazz，Bclazz,Cclazz
                此时，singletonObjects需要实际bean对象
                操作： new A(); 为A的属性B赋值---强制创建B对象；  问题：此时的B对象里面的属性没有赋值；
                解决：如果使用递归，new A(); 然后遍历属性B，递归；
                                  new B(); 然后遍历属性C，递归；。。。

                而又会造成问题，
                A {
                    B;
                }

                B {
                    A;
                }
                new A(); 然后遍历属性B，递归；
                new B(); 然后遍历属性A，递归；。。。发现是死循环
    【待续---先可以不了解这个，后续我会继续更新更完善的版本】
```

Spring-lkj-03第3部分
        aware回调 --- 不难，更像一种思想；
        InitializingBean 一样，和aware回调

    ①写一个接口 BeanNameAware---里面有方法，setBeanName()
    ②让需要获取beanName的对象，实现该接口；
        eg，我让UserService extends BeanNameAware
    ③在实现类UserServiceImpl中重写此方法，但具体调用，交给spring---创建该对象的时候，调用这个方法，传入beanName

Spring-lkj-04-beanPostProcessor  思想也不难；easy

- 加入beanPostProcessor，也就是createBean中
              ① o = clazz.getConstructor().newInstance();前后，加入操作；
              ② initializingBean 前后，加入操作

- 操作
          新建一个接口，BeanPostProcessor在spring包下（写上两个方法）
                 

  ```java
   public Object postProcessBeforeInitialization(Object bean, String beanName)，
   public Object postProcessAfterInitialization(Object bean, String beanName) 
  ```


  ​            用户自己定义实现类，eg:LkjBeanPostProcessor，在方法里面进行相应操作，spring负责调用；
  ​            

  ```java
  /**
     * 这个是对bean做的一些通用操作；
     * 如果想对某些bean做操作，可以加上判断
     * 需要加上@Component 因为这个在spring中是通过对象，调用方法的【之前的aware回调，是直接通过接口，设置值的，不需要对象，所以不需要@Component】
  */
  ```

  ​        在ApplicationContext的 injectBeanDefinitionMap中 判断，如果是BeanPostProcessor的子类，则创建对象，加入list中；
  ​        在createBean中，遍历list，调用xxBeanPostProcessor的相应方法；

Spring-lkj-05-aop：基于beanPostProcessor实现的；
    它讲的很简单，
