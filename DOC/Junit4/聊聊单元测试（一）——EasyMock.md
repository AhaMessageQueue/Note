### 单元测试是保证软件质量的重要方法。
单元测试是对系统中某个模块功能的验证，但我们总会遇到这样那样的问题，导致测试代码很难编写。最直接的一个原因便是强耦合关系，被测试者依赖一些不容易构造，比较复杂的对象，如：如果要测试一个servlet，我们必须获得HttpServletRequest，甚至需要一个Web容器；如果要测试Dao层，我们可能要获得JDBC相关对象，最终获得ResultSet。这些对象的构建并不那么容易，如果我们使用Mock方法（常见的一种单元测试技术，它的主要作用是模拟一些在应用中不容易构造或者比较复杂的对象，从而把测试与测试边界以外的对象隔离开），编写自定义Mock对象是可以解决问题，但引入额外复杂代码的同时，很容易引入额外的错误。

### 发现的源动力就是不将就！
 面对上述问题，有很多开源项目对动态构建 Mock 对象提供了支持，这些项目能够根据现有的接口或类动态生成Mock对象，这样不仅能避免额外的编码工作，同时也降低了引入错误的可能。

EasyMock 是一套用于通过简单的方法对于给定的接口生成 Mock 对象的类库。它提供对接口的模拟，能够通过录制、回放、检查三步来完成大体的测试过程，可以验证方法的调用种类、次数、顺序，可以令 Mock 对象返回指定的值或抛出指定异常。通过 EasyMock，我们可以方便的构造 Mock 对象从而使单元测试顺利进行。

### 使用EasyMock完成单元测试的过程大致可以划分为以下几个步骤：

>1.使用 EasyMock 生成 Mock 对象；<br>
2.设定 Mock 对象的预期行为和输出；<br>
3.将 Mock 对象切换到 Replay 状态；<br>
4.调用 Mock 对象方法进行单元测试；<br>
5.对 Mock 对象的行为进行验证。<br>

### 文字表达有时候是苍白的，想不通过代码说事，还不行，看样子离大师还是有一段距离的。
```
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class LoginServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        // check username & password:
        if("admin".equals(username) && "123456".equals(password)) {
            ServletContext context = getServletContext();
            RequestDispatcher dispatcher = context.getNamedDispatcher("dispatcher");
            dispatcher.forward(request, response);
        }
        else {
            throw new RuntimeException("Login failed.");
        }
    }

}
```
这个Servlet实现简单的用户验证的功能，若用户名和口令匹配“admin”和“123456”，则请求被转发到指定的dispatcher上，否则，直接抛出RuntimeException。

为了测试doPost()方法，我们需要模拟HttpServletRequest，ServletContext和RequestDispatcher对象，以便脱离J2EE容器来测试这个Servlet。


完整的LoginServletTest代码如下：
```
import javax.servlet.*;
import javax.servlet.http.*;
import org.easymock.*;

public class LoginServletTest {
  // 测试登陆失败
  @Test
   public void testLoginFailed() throws Exception {
        // 使用 EasyMock 生成 Mock 对象；
        MockControl mc = MockControl.createControl(HttpServletRequest.class);
        HttpServletRequest request = (HttpServletRequest)mc.getMock();
        // 设定 Mock 对象的预期行为和输出；
        request.getParameter("username");
        mc.setReturnValue("admin", 1);
        request.getParameter("password");
        mc.setReturnValue("1234", 1);
        // 将 Mock 对象切换到 Replay 状态；
        mc.replay();
        // now start test:
        LoginServlet servlet = new LoginServlet();
        try {
              // 里面会调用 Mock 对象方法进行单元测试；
            servlet.doPost(request, null);
            fail("Not caught exception!");
        }
        catch(RuntimeException re) {
            assertEquals("Login failed.", re.getMessage());
        }
        // 对 Mock 对象的行为进行验证。
        mc.verify();
    }

```
```
    // 测试登陆成功
    @Test
    public void testLoginOK() throws Exception {
        // 使用 EasyMock 生成 Mock 对象；
        MockControl requestCtrl = MockControl.createControl(HttpServletRequest.class);
        HttpServletRequest requestObj = (HttpServletRequest)requestCtrl.getMock();
        MockControl contextCtrl = MockControl.createControl(ServletContext.class);
        final ServletContext contextObj = (ServletContext)contextCtrl.getMock();
        MockControl dispatcherCtrl = MockControl.createControl(RequestDispatcher.class);
        RequestDispatcher dispatcherObj = (RequestDispatcher)dispatcherCtrl.getMock();
       // 设定 Mock 对象的预期行为和输出；
        requestObj.getParameter("username");
        requestCtrl.setReturnValue("admin", 1);
        requestObj.getParameter("password");
        requestCtrl.setReturnValue("123456", 1);
        contextObj.getNamedDispatcher("dispatcher");
        contextCtrl.setReturnValue(dispatcherObj, 1);
        dispatcherObj.forward(requestObj, null);
        dispatcherCtrl.setVoidCallable(1);
        // 将 Mock 对象切换到 Replay 状态；
        requestCtrl.replay();
        contextCtrl.replay();
        dispatcherCtrl.replay();
          // 里面会调用 Mock 对象方法进行单元测试；
        //为了让getServletContext()方法返回我们创建的ServletContext Mock对象，我们定义一个匿名类并覆写getServletContext()方法：
        LoginServlet servlet = new LoginServlet() {
            public ServletContext getServletContext() {
                return contextObj;
            }
        };
        servlet.doPost(requestObj, null);
         // 对 Mock 对象的行为进行验证。
        requestCtrl.verify();
        contextCtrl.verify();
        dispatcherCtrl.verify();
    }
}
```
### 总结
EasyMock 推荐根据指定接口动态构建 Mock 对象，这促使我们遵循“面向接口编程”的原则：如果不面向接口，则测试难于进行。是否容易进行单元测试也体现了代码质量的高低，难以测试的代码，通常也是充满坏味道的代码。可以这么说，如果代码在单元测试中难于应用，则它在真实环境中也将难于应用。总之，创建尽可能容易测试的代码就是创建高质量的代码。