Mockito支持对变量进行注解，例如将mock对象设为测试类的属性，然后通过注解的方式@Mock来定义它，
这样有利于减少重复代码，增强可读性，易于排查错误等。
除了支持@Mock，Mockito支持的注解还有@Spy（监视真实的对象），@Captor（参数捕获器），@InjectMocks（mock对象自动注入）。

### Annotation的初始化
只有Annotation还不够，要让它们工作起来还需要进行初始化工作。
初始化的方法为：MockitoAnnotations.initMocks(testClass)参数testClass是你所写的测试类。
一般情况下在Junit4的@Before定义的方法中执行初始化工作，如下：
```
    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }
```
除了上述的初始化的方法外，还可以使用Mockito提供的Junit Runner：MockitoJUnitRunner这样就省略了上面的步骤。
```
    @RunWith(MockitoJUnit44Runner.class)
    public class ExampleTest {
        ...
    }
```
### @Mock注解
使用@Mock注解来定义mock对象有如下的优点：

- 方便mock对象的创建
- 减少mock对象创建的重复代码
- 提高测试代码可读性
- 变量名字作为mock对象的标示，所以易于排错
@Mock注解也支持自定义name和answer属性。

下面是官方给出的@Mock使用的例子：
```
public class ArticleManagerTest extends SampleBaseTestCase {

    @Mock
    private ArticleCalculator calculator;//默认
    @Mock(name = "dbMock")//指定名称
    private ArticleDatabase database;
    @Mock(answer = RETURNS_MOCKS)//指定Answer
    private UserProvider userProvider;

    private ArticleManager manager;

    @Before
    public void setup() {
        manager = new ArticleManager(userProvider, database, calculator);
    }

}
public class SampleBaseTestCase {
    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }
}
```
### @Spy注解
Spy的使用方法请参阅前面的章节，在此不再赘述，下面是使用方法：
```
    public class Test{
        @Spy
        Foo spyOnFoo = new Foo();

        @Before
        public void init(){
           MockitoAnnotations.initMocks(this);
        }
        ...
    }
```

### @Captor注解
@Captor是参数捕获器的注解，有关用法见前章，通过注解的方式也可以更便捷的对它进行定义。使用例子如下：
```
public class Test {

    @Captor
    ArgumentCaptor<AsyncCallback<Foo>> captor;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldDoSomethingUseful() {
        // ...
        verify(mock.doStuff(captor.capture()));
        assertEquals("foo", captor.getValue());
    }
}
```
### @InjectMocks注解
通过这个注解，可实现自动注入mock对象。当前版本只支持setter的方式进行注入，
Mockito首先尝试类型注入，如果有多个类型相同的mock对象，那么它会根据名称进行注入。
当注入失败的时候Mockito不会抛出任何异常，所以你可能需要手动去验证它的安全性。
```
@RunWith(MockitoJUnit44Runner.class)
public class ArticleManagerTest {

    @Mock
    private ArticleCalculator calculator;
    @Mock
    private ArticleDatabase database;
    @Spy
    private UserProvider userProvider = new ConsumerUserProvider();
    @InjectMocks
    private ArticleManager manager = new ArticleManager();

    @Test
    public void shouldDoSomething() {
        manager.initiateArticle();
        verify(database).addListener(any(ArticleListener.class));
    }

}
```