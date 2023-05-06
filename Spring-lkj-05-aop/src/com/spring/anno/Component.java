package com.spring.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Component {
    /**
     * // 默认是"" 这样，我们解析的时候，就使用小驼峰规则---我们可以先不实现
     * @return
     */
//    String value() default "";  // 实现待续

    String value() default "";
}
