Mockito通过equals()方法，来对方法参数进行验证。
但有时我们需要更加灵活的参数需求，比如，匹配任何的String类型的参数等等。
参数匹配器就是一个能够满足这些需求的工具。
Mockito框架中的Matchers类内建了很多参数匹配器，
而我们常用的Mockito对象便是继承自Matchers。
这些内建的参数匹配器如，anyInt()匹配任何int类型参数，anyString()匹配任何字符串，anySet()匹配任何Set等。
下面通过例子来说明如何使用内建的参数匹配器：

```
@Test
public void argumentMatchersTest(){

    List<String> mock = mock(List.class);
    when(mock.get(anyInt())).thenReturn("Hello").thenReturn("World");
    String result = mock.get(100) + " " + mock.get(200);
    verify(mock,times(2)).get(anyInt());
    assertEquals("Hello World",result);
}
```
```
verify(database).addListener(any(ArticleListener.class));
```


### Stubbing时使用内建参数匹配器
例子中，首先mock了List接口，然后用迭代的方式模拟了get方法的返回值，这里用了anyInt()参数匹配器来匹配任何的int类型的参数。所以当第一次调用get方法时输入任意参数为100方法返回”Hello”，第二次调用时输入任意参数200返回值”World”。

### Verfiy时使用参数匹配器
最后进行verfiy验证的时候也可将参数指定为anyInt()匹配器，那么它将不关心调用时输入的参数的具体参数值。

### 注意事项
如果使用了参数匹配器，那么所有的参数需要由匹配器来提供，否则将会报错。假如我们使用参数匹配器stubbing了mock对象的方法，那么在verify的时候也需要使用它。如：
```
    @Test
    public void argumentMatchersTest(){
        Map mapMock = mock(Map.class);
        when(mapMock.put(anyInt(), anyString())).thenReturn("world");
        mapMock.put(1, "hello");
        verify(mapMock).put(anyInt(), eq("hello"));
    }
```
在最后的验证时如果只输入字符串”hello”是会报错的，必须使用Matchers类内建的eq方法。如果将anyInt()换成1进行验证也需要用eq(1)。

详细的内建参数匹配器请参考：
http://docs.mockito.googlecode.com/hg/org/mockito/Matchers.html