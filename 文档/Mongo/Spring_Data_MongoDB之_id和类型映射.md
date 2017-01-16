### _id字段的映射：
MongoDB要求所有的document都要有一个_id的字段。
如果我们在使用中没有传入_id字段，它会自己创建一个ObjectId.
```
{
    "_id":ObjectId("53e0ff0b0364cb4a98ce3bfd"),
    "_class":"org.springframework.data.mongodb.examples.hello.domain.Person",
    "name":"John",
    "age":39,
    "accounts":[
        {
            "_id":null,
            "accountNumber":"1234-59873-893-1",
            "accountType":"SAVINGS",
            "balance":123.45
        }
    ]
}
```
`MongoMappingConverter`按照下面的规则将java class对应到_id字段：

1. 被标记为@Id (org.springframework.data.annotation.Id) 的字段或者属性。
2. 没有标记，但是名字是id的字段或者属性（类型为string或者BigInteger）。

### 类型映射
如果一个object中包含另一个object，默认存放在_class字段下，例如
```
public class Sample {
  Contact value;
}
public abstract class Contact { … }

public class Person extends Contact { … }

Sample sample = new Sample();
sample.value = new Person();

mongoTemplate.save(sample);

{ "_class" : "com.acme.Sample",
  "value" : { "_class" : "com.acme.Person" }
}
```
通过使用@TypeAlias可以把在_class下面存放的值变成固定的值，下面的例子为pers。
```
@TypeAlias("pers")
class Person {

}
```