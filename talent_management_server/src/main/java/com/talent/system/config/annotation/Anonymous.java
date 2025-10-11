package com.talent.system.config.annotation;

import java.lang.annotation.*;

/**
 * 匿名访问不鉴权注解
 * 
 * @author JamesRay
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Anonymous
{
}
