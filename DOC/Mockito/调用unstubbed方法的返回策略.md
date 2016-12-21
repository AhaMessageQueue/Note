之前我们学到了对mock对象的方法进行stubbing，如果在交互中调用到了没有stubbing的方法Mocktio会如何处理呢？
它内建了五种策略供我们选择，其实这些策略就是实现了Answer接口的类，我们在mock对象的时候可以指定使用某种策略，
如：mock(List.class, RETURNS_SMART_NULLS)。Mockito这种特性在平时用到的机会不多，
因为做单元测试的时候对于调用mock对象的什么方法大多数情况我们是可预见的。
下面介绍两种策略，其他比较简单读者可看mockito文档。

### RETURNS_SMART_NULLS
在创建mock对象时，有的方法我们没有进行stubbing，所以在调用的时候有时会返回Null这样在进行处理时就很可能抛出NullPointerException。如果通过RETURNS_SMART_NULLS参数来创建的mock对象在调用没有stubbed的方法时他将返回SmartNull。
例如：返回类型是String它将返回空字符串””；是int，它将返回0；如果是List，它会返回一个空的List。
另外，在堆栈中可以看到SmartNull的友好提示。

```
    @Test
    public void returnsSmartNullsTest() {
        List mock = mock(List.class, RETURNS_SMART_NULLS);
        System.out.println(mock.get(0));
        System.out.println(mock.toArray().length);
    }
```
由于使用了RETURNS_SMART_NULLS参数来创建mock对象，所以在执行下面的操作时将不会抛出NullPointerException异常，另外堆栈也提示了相关的信息“SmartNull returned by unstubbed get() method on mock”。

### RETURNS_DEEP_STUBS
同上面的参数一样RETURNS_DEEP_STUBS也是一个创建mock对象时的备选参数。
例如我们有Account对象和RailwayTicket对象，RailwayTicket是Account的一个属性。

```
    public class Account {
        private RailwayTicket railwayTicket;
        public RailwayTicket getRailwayTicket() {
            return railwayTicket;
        }
        public void setRailwayTicket(RailwayTicket railwayTicket) {
            this.railwayTicket = railwayTicket;
        }
    }
    public class RailwayTicket {
        private String destination;
        public String getDestination() {
            return destination;
        }
        public void setDestination(String destination) {
            this.destination = destination;
        }
    }
```
下面通过RETURNS_DEEP_STUBS来创建mock对象。
```
@Test
public void deepstubsTest(){

    Account account = mock(Account.class, RETURNS_DEEP_STUBS);
    when(account.getRailwayTicket().getDestination()).thenReturn("Beijing");
    account.getRailwayTicket().getDestination();
    verify(account.getRailwayTicket()).getDestination();
    assertEquals("Beijing", account.getRailwayTicket().getDestination());

}
```
上例中，我们只创建了Account的mock对象，没有对RailwayTicket创建mock，
因为通过RETURNS_DEEP_STUBS参数程序会自动进行mock所需要的对象，所以上面的例子等价于：
```
@Test
public void deepstubsTest2(){

    Account account = mock(Account.class);
    RailwayTicket railwayTicket = mock(RailwayTicket.class);

    when(account.getRailwayTicket()).thenReturn(railwayTicket);
    when(railwayTicket.getDestination()).thenReturn("Beijing");

    account.getRailwayTicket().getDestination();

    verify(account.getRailwayTicket()).getDestination();
    assertEquals("Beijing", account.getRailwayTicket().getDestination());

}
```
为了代码整洁和确保它的可读性，我们应该少用这个特性。