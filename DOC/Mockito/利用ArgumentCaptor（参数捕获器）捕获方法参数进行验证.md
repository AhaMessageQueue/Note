在某些场景中，不光要对方法的返回值和调用进行验证，同时需要验证一系列交互后所传入方法的参数。
那么我们可以用参数捕获器来捕获传入方法的参数进行验证，看它是否符合我们的要求。

### ArgumentCaptor介绍
通过ArgumentCaptor对象的forClass(Class<T> clazz)方法来构建ArgumentCaptor对象。
然后便可在验证时对方法的参数进行捕获，最后验证捕获的参数值。
如果方法有多个参数都要捕获验证，那就需要创建多个ArgumentCaptor对象处理。

### ArgumentCaptor的Api
```
argument.capture() 要捕获方法的参数
argument.getValue() 获取方法参数值，如果方法进行了多次调用，它将返回最后一个参数值
argument.getAllValues() 方法进行多次调用后，返回多个参数值
```

应用实例:
```
    @Test
    public void argumentCaptorTest() {
        List mock = mock(List.class);
        List mock2 = mock(List.class);
        mock.add("John");
        mock2.add("Brian");
        mock2.add("Jim");

        ArgumentCaptor argument = ArgumentCaptor.forClass(String.class);

        verify(mock).add(argument.capture());
        assertEquals("John", argument.getValue());

        verify(mock2, times(2)).add(argument.capture());

        assertEquals("Jim", argument.getValue());
        assertArrayEquals(new Object[]{"Brian","Jim"},argument.getAllValues().toArray());
    }
```
首先构建ArgumentCaptor需要传入捕获参数的对象，例子中是String。
接着要在verify方法的参数中调用argument.capture()方法来捕获输入的参数，之后argument变量中就保存了参数值，可以用argument.getValue()获取。
当某个对象进行了多次调用后，如mock2对象，这时调用argument.getValue()获取到的是最后一次调用的参数。
如果要获取所有的参数值可以调用argument.getAllValues()，它将返回参数值的List。

在某种程度上参数捕获器和参数匹配器有很大的相关性。它们都用来确保传入mock对象参数的正确性。
然而，当自定义的参数匹配器的重用性较差时，用参数捕获器会更合适，只需在最后对参数进行验证即可。
