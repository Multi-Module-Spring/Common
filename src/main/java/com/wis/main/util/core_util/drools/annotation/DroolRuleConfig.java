package com.wis.main.util.core_util.drools.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DroolRuleConfig {
    String packageName() default "rule";
    Class<?> ruleSource();
}
