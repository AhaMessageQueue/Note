### Mock对象的创建
```
mock(Class<T> classToMock)
mock(Class<T> classToMock, String name)
```
可以对类和接口进行mock对象的创建，创建的时候可以为mock对象命名，也可以忽略命名参数。为mock对象命名的好处就是调试的时候会很方便，比如，我们mock多个对象，在测试失败的信息中会把有问题的mock对象打印出来，有了名字我们可以很容易定位和辨认出是哪个mock对象出现的问题。另外它也有限制，对于final类、匿名类和Java的基本类型是无法进行mock的。

### Mock对象的期望行为及返回值设定
我们已经了解到可以通过when(mock.someMethod()).thenReturn(value) 来设定mock对象的某个方法调用时的返回值，但它也同样有限制对于static和final修饰的方法是无法进行设定的。下面来详细的介绍一下有关方法及返回值的设定：

首先假设我们创建Iterator接口的mock对象
```
Iterator<String> i = mock(Iterator.class);
```
对方法设定返回值
```
when(i.next()).thenReturn("Hello")
```
对方法设定返回异常
```
when(i.next()).thenThrow(new RuntimeException())
```
Mockito支持迭代风格的返回值设定
第一种方式
```
when(i.next()).thenReturn("Hello").thenReturn("World")
```
第二种方式
```
when(i.next()).thenReturn("Hello", "World")
```
上面的设定相当于：
```
when(i.next()).thenReturn("Hello")
when(i.next()).thenReturn("World")
```
第一次调用i.next()将返回”Hello”，第二次的调用会返回”World”。

### Stubbing的另一种语法
doReturn(Object) 设置返回值
```
doReturn("Hello").when(i).next();
```
迭代风格
```
doReturn("Hello").doReturn("World").when(i).next();
```
返回值的次序为从左至右，第一次调用返回”Hello”，第二次返回”World”。

doThrow(Throwable) 设置返回异常
```
doThrow(new RuntimeException()).when(i).next();
```
因为这种语法的可读性不如前者，所以能使用前者的情况下尽量使用前者，当然在后面要介绍的Spy除外。

### 对void方法进行方法预期设定
void方法的模拟不支持when(mock.someMethod()).thenReturn(value)这样的语法，只支持下面的方式：
doNothing() 模拟不做任何返回（mock对象void方法的默认返回）
```
doNothing().when(i).remove();
```
doThrow(Throwable) 模拟返回异常
```
doThrow(new RuntimeException()).when(i).remove();
```

迭代风格
```
doNothing().doThrow(new RuntimeException()).when(i).remove();
```
第一次调用remove方法什么都不做，第二次调用抛出RuntimeException异常。