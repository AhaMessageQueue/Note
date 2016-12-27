Mock对象只能调用stubbed方法，调用不了它真实的方法。
但Mockito可以监视一个真实的对象，这时对它进行方法调用时它将调用真实的方法，同时也可以stubbing这个对象的方法让它返回我们的期望值。
另外不论是否是真实的方法调用都可以进行verify验证。
和创建mock对象一样，对于final类、匿名类和Java的基本类型是无法进行spy的。

### 监视对象
监视一个对象需要调用spy(T object)方法，如：List spy = spy(new LinkedList());那么spy变量就在监视LinkedList实例。


### 被监视对象的Stubbing
stubbing被监视对象的方法时要慎用when(Object)，如：
```
    List spy = spy(new LinkedList());
    //Impossible: real method is called so spy.get(0) throws IndexOutOfBoundsException (the list is yet empty)
    when(spy.get(0)).thenReturn("foo");
    //You have to use doReturn() for stubbing
    doReturn("foo").when(spy).get(0);
```
当调用when(spy.get(0)).thenReturn("foo")时，会调用真实对象的get(0)，由于list是空的所以会抛出IndexOutOfBoundsException异常，
用doReturn可以避免这种情况的发生，因为它不会去调用get(0)方法。

下面是官方文档给出的例子：
```
@Test
public void spyTest() {

    List list = new LinkedList();
        List spy = spy(list);

        //optionally, you can stub out some methods:
        when(spy.size()).thenReturn(100);

        //using the spy calls real methods
        spy.add("one");
        spy.add("two");

        //prints "one" - the first element of a list
        System.out.println(spy.get(0));

        //size() method was stubbed - 100 is printed
        System.out.println(spy.size());

        //optionally, you can verify
        verify(spy).add("one");
        verify(spy).add("two");

}
```