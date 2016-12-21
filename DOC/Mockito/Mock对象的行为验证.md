之前介绍了如何设置mock对象预期调用的方法及返回值。下面介绍方法调用的验证，而它关注点则在mock对象的交互行为上，比如验证mock对象的某个方法调用参数，调用次数，顺序等等。下面来看例子：

```
@Test
public void verifyTest() {

    List<String> mock = mock(List.class);
        List<String> mock2 = mock(List.class);

        when(mock.get(0)).thenReturn("hello");

        mock.get(0);
        mock.get(1);
        mock.get(2);

        mock2.get(0);

        verify(mock).get(2);
        verify(mock, never()).get(3);
        verifyNoMoreInteractions(mock);
        verifyZeroInteractions(mock2);

}
```

### 验证的基本方法
我们已经熟悉了使用verify(mock).someMethod(…)来验证方法的调用。例子中，我们mock了List接口，然后调用了mock对象的一些方法。验证是否调用了mock.get(2)方法可以通过verify(mock).get(2)来进行。verify方法的调用不关心是否模拟了get(2)方法的返回值，只关心mock对象后，是否执行了mock.get(2)，如果没有执行，测试方法将不会通过。

### 验证未曾执行的方法
在verify方法中可以传入never()方法参数来确认mock.get(3)方法不曾被执行过。另外还有很多调用次数相关的参数将会在下面提到。

### 查询多余的方法调用
verifyNoMoreInteractions()方法可以传入多个mock对象作为参数，用来验证传入的这些mock对象是否存在没有验证过的调用方法。本例中传入参数mock，测试将不会通过，因为我们只verify了mock对象的get(2)方法，没有对get(0)和get(1)进行验证。为了增加测试的可维护性，官方不推荐我们过于频繁的在每个测试方法中都使用它，因为它只是测试的一个工具，只在你认为有必要的时候才用。

### 查询没有交互的mock对象
verifyZeroInteractions()也是一个测试工具，源码和verifyNoMoreInteractions()的实现是一样的，为了提高逻辑的可读性，所以只不过名字不同。在例子中，它的目的是用来确认mock2对象没有进行任何交互，但mock2执行了get(0)方法，所以这里测试会报错。由于它和verifyNoMoreInteractions()方法实现的源码都一样，因此如果在verifyZeroInteractions(mock2)执行之前对mock.get(0)进行了验证那么测试将会通过。

### 验证方法调用的次数
如果要验证Mock对象的某个方法调用次数，则需给verify方法传入相关的验证参数，它的调用接口是verify(T mock, VerificationMode mode)。如：verify(mock,times(3)).someMethod(argument)验证mock对象someMethod(argument)方法是否调用了三次。times(N)参数便是验证调用次数的参数，N代表方法调用次数。其实verify方法中如果不传调用次数的验证参数，它默认传入的便是times(1)，即验证mock对象的方法是否只被调用一次，如果有多次调用测试方法将会失败。

Mockito除了提供times(N)方法供我们调用外，还提供了很多可选的方法：
never() 没有被调用，相当于times(0)
atLeast(N) 至少被调用N次
atLeastOnce() 相当于atLeast(1)
atMost(N) 最多被调用N次

### 超时验证
Mocki****to提供对超时的验证，但是目前不支持在下面提到的顺序验证中使用。进行超时验证和上述的次数验证一样，也要在verify中进行参数的传入，参数为timeout(int millis)，timeout方法中输入的是毫秒值。下面看例子：
验证someMethod()是否能在指定的100毫秒中执行完毕
```
verify(mock, timeout(100)).someMethod();
```
结果和上面的例子一样，在超时验证的同时可进行调用次数验证，默认次数为1
```
verify(mock, timeout(100).times(1)).someMethod();
```
在给定的时间内完成执行次数
```
verify(mock, timeout(100).times(2)).someMethod();
```
给定的时间内至少执行两次
```
verify(mock, timeout(100).atLeast(2)).someMethod();
```
另外timeout也支持自定义的验证模式，
```
verify(mock, new Timeout(100, yourOwnVerificationMode)).someMethod();
```

### 验证方法调用的顺序
Mockito同样支持对不同Mock对象不同方法的调用次序进行验证。进行次序验证是，我们需要创建InOrder对象来进行支持。例：

#### **创建mock对象**
```
List<String> firstMock = mock(List.class);
List<String> secondMock = mock(List.class);
```
#### **调用mock对象方法**
```
firstMock.add("was called first");
firstMock.add("was called first");
secondMock.add("was called second");
secondMock.add("was called third");
```

#### **创建InOrder对象**
inOrder方法可以传入多个mock对象作为参数，这样便可对这些mock对象的方法进行调用顺序的验证
```
InOrder inOrder = inOrder( secondMock, firstMock );
```
#### **验证方法调用**
接下来我们要调用InOrder对象的verify方法对mock方法的调用顺序进行验证。注意，这里必须是你对调用顺序的预期。

InOrder对象的verify方法也支持调用次数验证，上例中，我们期望firstMock.add("was called first")方法先执行并执行两次，所以进行了下面的验证inOrder.verify(firstMock,times(2)).add("was called first")。其次执行了secondMock.add("was called second")方法，继续验证此方法的执行inOrder.verify(secondMock).add("was called second")。如果mock方法的调用顺序和InOrder中verify的顺序不同，那么测试将执行失败。

#### **InOrder的verifyNoMoreInteractions()方法**
它用于确认上一个顺序验证方法之后，mock对象是否还有多余的交互。它和Mockito提供的静态方法verifyNoMoreInteractions不同，InOrder的验证是基于顺序的，另外它只验证创建它时所提供的mock对象，在本例中只对firstMock和secondMock有效。例如：
```
inOrder.verify(secondMock).add("was called second");
inOrder.verifyNoMoreInteractions();
```
在验证secondMock.add("was called second")方法之后，加上InOrder的verifyNoMoreInteractions方法，**表示此方法调用后再没有多余的交互**。
例子中会报错，因为在此方法之后还执行了secondMock.add("was called third")。现在将上例改成：
```
inOrder.verify(secondMock).add("was called third");
inOrder.verifyNoMoreInteractions();
```
测试会恢复为正常，因为在secondMock.add("was called third")之后已经没有多余的方法调用了。
如果这里换成Mockito类的verifyNoMoreInteractions方法测试还是会报错，**它查找的是mock对象中是否存在没有验证的调用方法，和顺序是无关的**。