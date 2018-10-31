package com.chris.spring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE) // 作用于类，接口，注解
@Retention(RetentionPolicy.RUNTIME) // 不仅保存到class 文件中，而且在运行是可以通过反射调用
public @interface Service {
    String value();
}
