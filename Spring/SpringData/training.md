一、
如果您不想扩展Spring Data接口，还可以使用`@RepositoryDefinition`对repository接口进行注释。


二、
```java
@Query("select u from User u")
Stream<User> findAllByCustomQueryAndStream();

Stream<User> readAllByFirstnameNotNull();

@Query("select u from User u")
Stream<User> streamAllPaged(Pageable pageable);
```

```java
// 使用try-with-resources代码块
try (Stream<User> stream = repository.findAllByCustomQueryAndStream()) {
  stream.forEach(…);
}
```

三、
```java
@Async
Future<User> findByFirstname(String firstname);// Use java.util.concurrent.Future as return type.       

@Async
CompletableFuture<User> findOneByFirstname(String firstname);// Use a Java 8 java.util.concurrent.CompletableFuture as return type.

@Async
ListenableFuture<User> findOneByLastname(String lastname);// Use a org.springframework.util.concurrent.ListenableFuture as return type.
```

四、
```java
// 例1.14 中间接口
@NoRepositoryBean
public interface MyRepository<T, ID extends Serializable>
  extends PagingAndSortingRepository<T, ID> {

  void sharedCustomMethod(ID id);
}
```

```java
// 自定义基类
public class MyRepositoryImpl<T, ID extends Serializable>
  extends SimpleJpaRepository<T, ID> implements MyRepository<T, ID> {

  private final EntityManager entityManager;

  public MyRepositoryImpl(JpaEntityInformation entityInformation,
                          EntityManager entityManager) {
    super(entityInformation, entityManager);

    // Keep the EntityManager around to used from the newly introduced methods.
    this.entityManager = entityManager;
  }

  public void sharedCustomMethod(ID id) {
    // implementation goes here
  }
}
```

```java
// 使用JavaConfig配置自定义Repository基类
@Configuration
@EnableJpaRepositories(repositoryBaseClass = MyRepositoryImpl.class)
class ApplicationConfiguration { … }
```

五、
```java
// 例1.18 启用web支持
@Configuration
@EnableWebMvc
@EnableSpringDataWebSupport
class WebConfiguration { }
```

```java
// 例1.20 使用领域类型
@Controller
@RequestMapping("/users")
public class UserController {

  @RequestMapping("/{id}")
  public String showUserForm(@PathVariable("id") User user, Model model) {

    model.addAttribute("user", user);
    return "userForm";
  }
}
```

```java
// 使用分页作为控制层参数
@Controller
@RequestMapping("/users")
public class UserController {
    @Autowired UserRepository repository;
    @RequestMapping
    public String showUsers(Model model, Pageable pageable) {
        model.addAttribute("users", repository.findAll(pageable));
        return "users";
    }
}
```


|page|获取的页数，默认0|
|size|一页中最大的数据量，默认20|
|sort|需要被排序的属性(格式：`property,property(,ASC/DESC)`)，默认是asc，使用多个`sort`参数，你可以使用`?sort=firstname&sort=lastname,asc`|

如果你需要对多个表写多个分页或排序，那么你需要用`@Qualifier`来区分，请求参数的前缀是`${qualifire}_`，那么你的方法可能变成这样：

```java
// 多个分页
public String showUsers(Model model,
@Qualifier("foo") Pageable first,
@Qualifier("bar") Pageable second) { … }
```

你需要填写`foo_page`和`bar_page`等。

默认的`Pageable`相当于`new PageRequest(0,20)`，你可以用`@PageableDefaults`注解来放在`Pageable`上。

