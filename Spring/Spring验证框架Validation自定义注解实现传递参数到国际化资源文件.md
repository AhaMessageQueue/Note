关于SpringMVC验证框架Validation的使用方法，不是本篇的重点，可参见博文`SpringMVC介绍之Validation`

在使用Validation时，一定有朋友遇到过一个问题，那就是：无法传递参数到国际化资源文件properties错误描述中。

举个例子：

User类中
```
@NotEmpty(message="{password.empty.error}")
private String password;
```

资源文件validation_zh_CN.properties中为
```
password.empty.error=password不能为空
```

实际开发中，很多参数都是要验证非空的，如果每个参数都单独加个错误描述，是很麻烦的。properties虽支持“{}”的写法传递参数，但使用JSR-303注解无法实现传递参数。我想了个办法可通过自定义注解方式实现。

首先，建立个自定义的@NotEmpty注解：
```
package com.itkt.payment.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import com.itkt.payment.core.handler.NotEmptyValidator;

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER })
@Constraint(validatedBy = { NotEmptyValidator.class })
public @interface NotEmpty {

    String field() default "";

    String message() default "{com.itkt.payment.core.handler.NotEmpty.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
```

自定义的NotEmpty注解中，我们新加了field字段，用于标识字段名称。

然后，建立NotNullValidator实现类：

```
package com.itkt.payment.core.handler;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.itkt.payment.core.annotation.NotNull;

public class NotNullValidator implements ConstraintValidator<NotNull, Object> {

    @Override
    public void initialize(NotNull annotation) {
    }

    @Override
    public boolean isValid(Object str, ConstraintValidatorContext constraintValidatorContext) {
        return str != null;
    }

}
```
之后，在资源文件validation_zh_CN.properties中，改变写法：
```
password.empty.error={field}不能为空
```
最后，我们就可以在User类中使用自定义的NotEmpty注解：
```
@NotEmpty(field = "password", message = "{password.empty.error}")
private String password;
```
实际上，国际化资源文件本身支持从JSR-303注解中获取属性的参数值的，例如从@Length注解中，获取min和max属性的值：
```
username.length.error=用户名长度必须在{min}-{max}之间
```
之所以自带的@NotEmpty注解无法实现，是因为没有一个属性能传递字段名，所以通过自定义@NotEmpty注解来拓展个field字段。