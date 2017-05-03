在实际中除了传入一些基本的参数以外，还需要传入一些类似于JavaBean等复合类型，或者Map等复杂类型的数据，这一篇博文主要来写两个demo，模拟一下CXF处理这类数据的过程。

## 1. CXF处理JavaBean等复合类型的数据
客户端提交一个数据过去，要检查权限，比如是管理员的话，就拥有多个权限，普通用户可能就一个权限之类的。这样的话需要一个用户的bean和权限的bean，然后数据库的数据我用List来模拟一下，demo如下：

```
public class User {

    private Integer id;
    private String name;
    private String password;
    //……
}

public class Role {

    private Integer id;
    private String roleName; //角色名称
    //……
}
```

ws的程序如下：

```
//接口
@WebService
public interface MyWebService {

    public String sayHello(String str);

    public List<Role> getRoleByUser(User user);
}

//实现类
@WebService
public class MyWebServiceImpl implements MyWebService{

    public String sayHello(String str) {
        return "Hello" + str;
    }

    public List<Role> getRoleByUser(User user) {
        List<Role> roleList = new ArrayList<Role>();        
        if(user != null) {
            if(user.getName().equals("admin") && user.getPassword().equals("123")) {
                roleList.add(new Role(1, "技术总监"));
                roleList.add(new Role(2, "架构师"));           
            } else if(user.getName().equals("eson15") && user.getPassword().equals("123")) {
                roleList.add(new Role(3, "java菜鸟"));
            }
            return roleList;
        } else {
            return null;
        }       
    }

}
```

主要来看getRoleByUser这个方法，当然了，也可以使用注解去配置一下要生成的wsdl，这里就不配置了，使用默认的即可，方法中自定义了一个List用来存储角色信息的，模拟的是先判断用户的身份，然后根据不同的身份赋予不同的角色。

生成wsdl后，通过解析，然后调用该ws的代码如下：

```
public class _Main {

    public static void main(String[] args) {
        MyWebServiceService service = new MyWebServiceService();
        MyWebService port = service.getMyWebServicePort();

        User user = new User();
        user.setName("admin");
        user.setPassword("123");
        List<Role> roleList = port.getRoleByUser(user);

        for(Role role : roleList) {
            System.out.println(role.getId() + "," + role.getRoleName());
        }
    }

}
```

这里为了模拟，直接new一个admin用户，然后调用ws的getRoleByUser方法获取该用户的角色，打印到控制台，结果肯定是两个角色，如果是eson15，那就是一个Java菜鸟的角色了。

## 2. CXF处理Map等复杂类型的数据
CXF是无法直接操作Map类型的数据的，需要进行一些处理才行。我们先写一个获取所有角色信息的方法，先看一下会出啥问题。

```
//接口
@WebService
public interface MyWebService {

    //这个注解等会有用，先注释掉
    //@XmlJavaTypeAdapter(MapAdapter.class)
    public Map<String, List<Role>> getRoles();
}

// 实现类
@WebService
public class MyWebServiceImpl implements MyWebService{

    // 省去上面不相关代码
    public Map<String, List<Role>> getRoles() {
        Map<String, List<Role>> map = new HashMap<String, List<Role>>();

        List<Role> roleList = new ArrayList<Role>();        
        roleList.add(new Role(1, "技术总监"));
        roleList.add(new Role(2, "架构师"));   
        map.put("admin", roleList);

        List<Role> roleList2 = new ArrayList<Role>();       
        roleList2.add(new Role(1, "java菜鸟"));
        map.put("eson15", roleList2);

        return map;
    }

}
```

这里还是使用静态数据模拟一下，Map中存储了两个用户，admin用户有两个角色，eson15用户就一个角色，当这个ws发布后，客户端可以调用getRoles方法获取这些角色，但是我们会发现，这个ws是无法发布的，控制台会报错。因为CXF要处理Map类型的数据，还需要一些转换工作。

如何转换呢？注@XmlJavaTypeAdapter(MapAdapter.class)就派上用场了，把上面的这个注解的注释给去掉，也就是说我们自定义一个适配器叫MapAdapter，来适配这个Map，接下来写这个MapAdapter：

```
public class MapAdapter extends XmlAdapter<MyRole[], Map<String, List<Role>>>{

    /**
     * 适配转换 MyRole[]  ->  Map<String, List<Role>>
     */
    @Override
    public Map<String, List<Role>> unmarshal(MyRole[] v) throws Exception {
        Map<String, List<Role>> map = new HashMap<String, List<Role>>();
        for(int i = 0; i < v.length; i++) {
            MyRole role = v[i];
            map.put(role.getKey(), role.getValue());
        }
        return map;
    }

    /**
     * 适配转换 Map<String, List<Role>>  ->  MyRole[]
     */
    @Override
    public MyRole[] marshal(Map<String, List<Role>> v) throws Exception {
        MyRole[] roles = new MyRole[v.size()];
        int i = 0;
        for(String key : v.keySet()) {
            List<Role> rolesList = v.get(key);
            roles[i] = new MyRole();
            roles[i].setKey(key);
            roles[i].setValue(rolesList);
            i++;
        }
        return roles;
    }

}
```

MapAdapter需要继承XmlAdapter，这个XmlAdapter的泛型里两个参数指的是从什么转到什么？即那两个对象之间在转换，这里需要转Map对象，一般我们会用一个数组对象来和Map对象进行转换，所以我们自定义一个MyRole[]数组：

```
public class MyRole {

    private String key;
    private List<Role> value;
    //……
}
```

数组中保存了MyRole对象，而这个对象中的两个属性要符合Map的结构，所以我们就好转了。

紧接着上面的分析，MapAdapter需要实现两个方法，这两个方法刚好是两个方向的转换，方法内部的代码很好理解，因为上面这个数组中的属性就是key和value的形式，所以这两个方法就是该数组和Map中的数据进行相互的转换而已。这样就可以操作Map类型的数据了。接下来就可以发布该ws服务了。

然后解析完wsdl后，在客户端就可以获取所有的角色信息了。

```
public class _Main {

    public static void main(String[] args) {
        MyWebServiceService service = new MyWebServiceService();
        MyWebService port = service.getMyWebServicePort();

        // 省去不相关代码

        List<MyRole> roles = port.getRoles().getItem();
        for(MyRole myrole : roles) {
            System.out.print("key:" + myrole.getKey() + ","); 
            System.out.print("role:");
            for(Role role : myrole.getValue()) {
                System.out.print(role.getRoleName() + " ");
            }
            System.out.println();
        }
    }

}
```

看下输出的结果：

```
key:eson15,role:java菜鸟
key:admin,role:技术总监 架构师 
```

所以CXF处理像Map等复杂类型的数据的时候，还需要先进行一些转换的处理，才能正常发布ws服务，不过也不算太麻烦。