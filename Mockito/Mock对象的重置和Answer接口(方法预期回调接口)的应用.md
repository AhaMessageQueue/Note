### Mock对象的重置
Mockito提供了reset(mock1,mock2……)方法，用来重置mock对象。当mock对象被重置后，它将回到刚创建完的状态，没有任何stubbing和方法调用。这个特性平时是很少用到的，因为我们大都为每个test方法创建mock，所以没有必要对它进行重置。

官方提供这个特性的唯一目的是使得我们能在有容器注入的mock对象中工作更为方便。所以，当决定要使用这个方法的时候，首先应该考虑一下我们的测试代码是否简洁和专注，测试方法是否已经超长了。

### Answer接口（方法预期回调接口）的应用
#### Answer接口说明
对mock对象的方法进行调用预期的设定，可以通过thenReturn()来指定返回值，thenThrow()指定返回时所抛异常，通常来说这两个方法足以应对一般的需求。
但有时我们需要自定义方法执行的返回结果，Answer接口就是满足这样的需求而存在的。
另外，创建mock对象的时候所调用的方法也可以传入Answer的实例mock(java.lang.Class<T> classToMock, Answer defaultAnswer)，
它可以用来处理那些mock对象没有stubbing的方法的返回值。

#### InvocationOnMock对象的方法
Answer接口定义了参数为InvocationOnMock对象的answer方法，利用InvocationOnMock提供的方法可以获取mock方法的调用信息。下面是它提供的方法：
```
getArguments() 调用后会以Object数组的方式返回mock方法调用的参数。
getMethod() 返回java.lang.reflect.Method 对象
getMock() 返回mock对象
callRealMethod() 真实方法调用，如果mock的是接口它将会抛出异常
```
通过一个例子来看一下Answer的使用。我们自定义CustomAnswer类，它实现了Answer接口，返回值为String类型。
```
    public class CustomAnswer implements Answer<String> {
        public String answer(InvocationOnMock invocation) throws Throwable {
            Object[] args = invocation.getArguments();
            Integer num = (Integer)args[0];
            if( num>3 ){
                return "yes";
            } else {
                throw new RuntimeException();
            }
        }
    }
```
这个返回值是这样的逻辑，如果调用mock某个方法输入的参数大于3返回”yes”，否则抛出异常。

#### Answer接口的使用
应用方式如下：

首先对List接口进行mock
```
List<String> mock = mock(List.class);
```
指定方法的返回处理类CustomAnswer，因为参数为4大于3所以返回字符串”yes”
```
when(mock.get(4)).thenAnswer(new CustomAnswer());
```


另外一种方式
```
doAnswer(new CustomAnswer()).when(mock.get(4));
```
对void方法也可以指定Answer来进行返回处理，如：
```
doAnswer(new xxxAnswer()).when(mock).clear();
```
当设置了Answer后，指定方法的调用结果就由我们定义的Answer接口来处理了。

另外我们也可以使用匿名内部类来进行应用：
```
    @Test
    public void customAnswerTest(){
        List<String> mock = mock(List.class);
        when(mock.get(4)).thenAnswer(new Answer(){
            public String answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                Integer num = (Integer)args[0];
                if( num>3 ){
                    return "yes";
                } else {
                    throw new RuntimeException();
                }
            }
        });
        System.out.println(mock.get(4));
    }
```