package com.chris.myspring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * spring中的RequestMapping
 */
// 注解出现的位置 类，接口，方法
@Target({ElementType.TYPE, ElementType.METHOD})
// 注解不仅保留在class文件中，而且在运行时存在，可通过反射调用
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {
}
