### 两者区别
`@Consumes`注释代表的是一个资源可以接受的 MIME 类型。
`@Produces `注释代表的是一个资源可以返回的 MIME 类型。

这些注释均可在资源、资源方法、子资源方法、子资源定位器或子资源内找到。

清单 1. @Consumes/@Produces
```
package com.ibm.jaxrs.sample.organization;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Path(value="/contacts")
public class ContactsResource {

    @GET
    @Produces(value={"text/xml", "application/json"})
    @Path(value="/{emailAddress:.+@.+\\.[a-z]+}")
    public ContactInfo getByEmailAddress(@PathParam(value="emailAddress")
        String emailAddress) {
        ...
    }

    @GET
    @Path(value="/{lastName}")
    @Produces(value="text/xml")
    public ContactInfo getByLastName(@PathParam(value="lastName") String lastName) {
        ...
    }

    @POST
    @Consumes(value={"text/xml", "application/json"})
    public void addContactInfo(ContactInfo contactInfo) {
        ...
    }

}
```
`@Consumes`注释针对 Content-Type 请求头进行匹配，以决定方法是否能接受给定请求的内容。

在清单 2 中，application/json 的 Content-Type 头再加上对路径 /contacts 的 POST，
表明我们的 ContactsResource 类内的 addContactInfo 方法将会被调用以处理请求。

清单 2. Content-Type 头部的使用
```
POST /contacts HTTP/1.1
Content-Type: application/json
Content-Length: 32
```

相反地，@Produces 注释被针对 Accept 请求头进行匹配以决定客户机是否能够处理由给定方法返回的表示。

清单 3. Accept 头部的使用
```
GET /contacts/johndoe@us.ibm.com HTTP/1.1
Accept: application/json
```

在清单 3中，对 /contacts/johndoe@us.ibm.com 的 GET 请求表明了 getByEmailAddress
方法将会被调用并且返回的格式将会是application/json，而非 text/xml。