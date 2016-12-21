Mockito是一个流行的Mocking框架。它使用起来简单，学习成本很低，而且具有非常简洁的API，测试代码的可读性很高。因此它十分受欢迎，用户群越来越多，很多的开源的软件也选择了Mockito。要想了解更多有关Mockito的信息，请访问它的官方网站：http://mockito.org/

在开始使用Mockito之前，先简单的了解一下Stub和Mock的区别。
Stub对象用来提供测试时所需要的测试数据，可以对各种交互设置相应的回应。例如我们可以设置方法调用的返回值等等。Mockito中when(…).thenReturn(…) 这样的语法便是设置方法调用的返回值。另外也可以设置方法在何时调用会抛异常等。Mock对象用来验证测试中所依赖对象间的交互是否能够达到预期。Mockito中用 verify(…).methodXxx(…) 语法来验证 methodXxx方法是否按照预期进行了调用。
有关stub和mock的详细论述见，Martin Fowler文章《Mocks Aren't Stub》http://martinfowler.com/articles/mocksArentStubs.html 。在Mocking框架中所谓的mock对象实际上是作为上述的stub和mock对象同时使用的。因为它既可以设置方法调用返回值，又可以验证方法的调用。

### Mockito的获取
Jar包的获取
可以访问下面的链接来下载最新的Jar包，笔者使用的当前最新版为：1.8.5 http://code.google.com/p/mockito/downloads/list

Maven
如果项目是通过Maven管理的，需要在项目的Pom.xml中增加如下的依赖：
```
<dependencies>
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-all</artifactId>
        <version>1.8.5</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```
### Mocktio包的引入
在程序中可以import org.mockito.Mockito;然后调用它的static方法，或者import static org.mockito.Mockito.*;个人倾向于后者，因为这样可以更方便些。

一个简单的例子
```
    import static org.junit.Assert.*;
    import static org.mockito.Mockito.*;
    import java.util.Iterator;
    import org.junit.Test;

    /**
     *
     * @author Brian Zhao
     */
    public class SimpleTest {

        @Test
        public void simpleTest(){
            //arrange
            Iterator i=mock(Iterator.class);
            when(i.next()).thenReturn("Hello").thenReturn("World");
            //act
            String result=i.next()+" "+i.next();
            //verify
    verify(i, times(2)).next();
            //assert
            assertEquals("Hello World", result);
        }
    }
```

在上面的例子中包含了Mockito的基本功能：

**创建Mock对象:**<br>
创建Mock对象的语法为，mock(class or interface)。例子中创建了Iterator接口的mock对象。

**设置方法调用的预期返回**<br>
通过when(mock.someMethod()).thenReturn(value) 来设定mock对象某个方法调用时的返回值。例子中我们对Iterator接口的next()方法调用进行了预期设定，当调用next()方法时会返回”Hello”，由于连续设定了返回值，因此当第二次调用时将返回”World”。

**验证方法调用**<br>
接下来对mock对象的next()方法进行了一系列实际的调用。mock对象一旦建立便会自动记录自己的交互行为，所以我们可以有选择的对它的交互行为进行验证。在Mockito中验证mock对象交互行为的方法是verify(mock).someMethod(…)。于是用此方法验证了next()方法调用，因为调用了两次，所以在verify中我们指定了times参数（times的具体应用在后面会继续介绍）。最后assert返回值是否和预期一样。