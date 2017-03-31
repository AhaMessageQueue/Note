## JavaScript prototype 属性

### 定义和用法
prototype 属性使您有能力向对象添加属性和方法。

### 语法
```
object.prototype.name=value
```

### 实例
在本例中，我们将展示如何使用 prototype 属性来向对象添加属性：
```
<script type="text/javascript">

    function employee(name,job,born)
    {
    this.name=name;
    this.job=job;
    this.born=born;
    }

    var bill=new employee("Bill Gates","Engineer",1985);

    employee.prototype.salary=null;
    bill.salary=20000;

    document.write(bill.salary);

</script>

```

### 输出：

20000