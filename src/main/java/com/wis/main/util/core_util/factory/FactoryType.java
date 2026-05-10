package com.wis.main.util.core_util.factory;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface FactoryType {

    String value();

}