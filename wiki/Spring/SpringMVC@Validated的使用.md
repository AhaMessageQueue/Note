@Valid是javax.validation里的。
@Validated是@Valid 的一次封装，是spring提供的校验机制使用。@Valid不提供分组功能

@Validated的特殊用法
### 分组
当一个实体类需要多种验证方式时，例：对于一个实体类的id来说，新增的时候是不需要的，对于更新时是必须的。

可以通过groups对验证进行分组

**分组接口类**（通过向groups分配不同类的class对象，达到分组目的）：
```
package com.valid.interfaces;

public interface First {

}
```
**实体类：**
```
package com.valid.pojo;

import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.NotEmpty;

import com.valid.interfaces.First;

public class People {

//在First分组时，判断不能为空
  @NotEmpty(groups={First.class})
  private String id;

  //name字段不为空，且长度在3-8之间
  @NotEmpty
  @Size(min=3,max=8)
  private String name;

  public String getName() {
      return name;
  }

  public void setName(String name) {
      this.name = name;
  }

  public String getId() {
      return id;
  }

  public void setId(String id) {
      this.id = id;
  }
}
```
注：

1. 不分配groups，默认每次都要进行验证
2. 对一个参数需要多种验证方式时，也可通过分配不同的组达到目的。例：

```
@NotEmpty(groups={First.class})
@Size(min=3,max=8,groups={Second.class})
private String name;
```
**控制类：**
```
package com.valid.controller;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.valid.interfaces.First;
import com.valid.pojo.People;

@Controller
public class FirstController {
 @RequestMapping("/addPeople")
    //不需验证ID
    public @ResponseBody String addPeople(@Validated People p,BindingResult result)
    {
        System.out.println("people's ID:" + p.getId());
        if(result.hasErrors())
        {
            return "0";
        }
        return "1";
    }

    @RequestMapping("/updatePeople")
    //需要验证ID
    public @ResponseBody String updatePeople(@Validated({First.class}) People p,BindingResult result)
    {
        System.out.println("people's ID:" + p.getId());
        if(result.hasErrors())
        {
            return "0";
        }
        return "1";
    }
}
```
注：

1. @Validated没有添加groups属性时，默认验证没有分组的验证属性，如该例子：People的name属性。
2. @Validated没有添加groups属性时，所有参数的验证类型都有分组（即本例中People的name的@NotEmpty、@Size都添加groups属性），则不验证任何参数

### 组序列
默认情况下，不同组别的约束验证是无序的，然而在某些情况下，约束验证的顺序却很重要。
例：

1. 第二个组中的约束验证依赖于一个稳定状态来运行，而这个稳定状态是由第一个组来进行验证的。
2. 某个组的验证比较耗时，CPU 和内存的使用率相对比较大，最优的选择是将其放在最后进行验证。
因此，在进行组验证的时候尚需提供一种有序的验证方式，这就提出了组序列的概念。

一个组可以定义为其他组的序列，使用它进行验证的时候必须符合该序列规定的顺序。
在使用组序列验证的时候，如果序列前边的组验证失败，则后面的组将不再给予验证。

分组接口类 （通过@GroupSequence注解对组进行排序）：
```
package com.valid.interfaces;

public interface First {

}
```
```
package com.valid.interfaces;

public interface Second {

}
```
```
package com.valid.interfaces;

import javax.validation.GroupSequence;

@GroupSequence({First.class,Second.class})
public interface Group {

}
```
**实体类：**
```
package com.valid.pojo;

import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.NotEmpty;

import com.valid.interfaces.First;
import com.valid.interfaces.Second;

public class People {

    //在First分组时，判断不能为空
    @NotEmpty(groups={First.class})
    private String id;

    //name字段不为空，且长度在3-8之间
    @NotEmpty(groups={First.class})
    @Size(min=3,max=8,groups={Second.class})
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
```
**控制类：**
```
package com.valid.controller;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.valid.interfaces.Group;
import com.valid.pojo.People;
import com.valid.pojo.Person;

@Controller
public class FirstController {

    @RequestMapping("/addPeople")
    public @ResponseBody String addPeople(@Validated({Group.class}) People p,BindingResult result)
    {
        if(result.hasErrors())
        {
            return "0";
        }
        return "1";
    }
}
```

### 验证多个对象
一个功能方法上处理多个模型对象时，需添加多个验证结果对象
```
package com.valid.controller;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.valid.pojo.People;
import com.valid.pojo.Person;

@Controller
public class FirstController {

    @RequestMapping("/addPeople")
    public @ResponseBody String addPeople(@Validated People p,BindingResult result,@Validated Person p2,BindingResult result2)
    {
        if(result.hasErrors())
        {
            return "0";
        }
        if(result2.hasErrors())
        {
            return "-1";
        }
        return "1";
    }
}
```