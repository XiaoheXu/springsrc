package com.chris.spring.annotation;

import java.lang.annotation.*;

/**
 * 类似spring中的Controller注解
 */
@Target(ElementType.TYPE)  //注解作用的目标 接口、类、枚举、注解
@Retention(RetentionPolicy.RUNTIME)// 运行时通过反射可以找到
public @interface Controller {
}
