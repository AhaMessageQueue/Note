package com.github.Smartboooy.retrofit2;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by 刘春龙 on 2017/5/5.
 */
public class Param<T1, T2> {

    private Class<T2> entityClass;

    class A {}
    class B extends A {}

    protected Param() {

        Class clazz = this.getClass();
        String typeName = clazz.getTypeName();
        System.out.println("[clazz] = " + clazz);//[clazz] = class com.github.retrofit2.SubParam
        System.out.println("[type name] = " + typeName);//[type name] = com.github.retrofit2.SubParam
        Type type = clazz.getGenericSuperclass();
        System.out.println("[type] = " + type);//[type] = com.github.retrofit2.Param<com.github.retrofit2.SubParam$MyClass, com.github.retrofit2.SubParam$MyInvoke>

        Type actualType = ((ParameterizedType)type).getActualTypeArguments()[0];
        System.out.println("[actualType 1] = " + actualType);//[actualType 1] = class com.github.retrofit2.SubParam$MyClass
        actualType = ((ParameterizedType)type).getActualTypeArguments()[1];
        System.out.println("[actualType 2] = " + actualType);//[actualType 2] = class com.github.retrofit2.SubParam$MyInvoke
        this.entityClass = (Class<T2>)actualType;

        B t = new B();
        type = t.getClass().getGenericSuperclass();
        System.out.println("A is B's super class :" + ((ParameterizedType)type).getActualTypeArguments().length);
    }
}
