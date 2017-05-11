package com.github.Smartboooy.retrofit2;

/**
 * Created by 刘春龙 on 2017/5/5.
 */
public class SubParam extends Param<SubParam.MyClass, SubParam.MyInvoke> {

    class MyClass{}
    class MyInvoke{}

    public static void main(String[] args) {
        SubParam subParam = new SubParam();
    }
}
