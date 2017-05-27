### 安全管理器

**安全管理器**是一个允许应用程序**实现安全策略**的类。

它允许应用程序在执行一个可能不安全或敏感的操作前**确定该操作是什么**，以及**是否是在允许执行该操作的安全上下文中执行它**。应用程序可以允许或不允许该操作。

`SecurityManager`类包含了很多名称以单词 `check` 开头的方法。Java库中的各种方法在执行某些潜在的敏感操作前可以调用这些方法。对 `checkXXX` 方法的典型调用如下：
```java
    SecurityManager security = System.getSecurityManager();
    if (security != null) { security.checkXXX(argument,  . . . ); }
```
**安全管理器**通过**抛出异常来提供阻止操作完成的机会**。

如果允许执行该操作，则安全管理器例程只是简单地返回，但如果不允许执行该操作，则抛出一个 `SecurityException`。
该约定的唯一例外是 `checkTopLevelWindow`，它返回 boolean 值。

获取和设置当前的安全管理器是由System类中的`getSecurityManager` 和 `setSecurityManager` 方法处理的。

**特殊方法 `checkPermission(java.security.Permission)` 确定是应该允许还是拒绝由指定权限所指示的访问请求**。
默认的实现调用`AccessController.checkPermission(perm);`如果允许访问请求，则安静地返回 `checkPermission`。如果拒绝访问请求，则抛出 `SecurityException`。

从 Java 2 SDK v1.2 开始， **SecurityManager中其他所有 `check` 方法**的**默认实现**都是**调用`SecurityManager.checkPermission`方法来确定调用线程是否具有执行所请求操作的权限**。

```
创建安全管理器步骤：
(1) 创建一个SecurityManager的子类；
(2) 覆盖或创建一些方法。
````

### 创建安全管理器
```
public class PasswordSecurityManager extends SecurityManager{
        private String password;

        PasswordSecurityManager(String password) {
            super();
            this.password = password;
        }
        public boolean accessOK(String password) {
            if(this.password.equals(password))  return true;
                     else return false;
        }
    }
```

### 利用安全管理器
```
 public static void main(String args[]){
        PasswordSecurityManager manager = new PasswordSecurityManager("123456");
        if(!manager.accessOK(args[0])) return;
        ......
    }
```

### 方法
#### getThreadGroup()
public ThreadGroup getThreadGroup():
调用此方法时，返回所有新创建的线程实例化后所在的**线程组**。
默认情况下，**返回当前线程所在的线程组**。应该由指定的安全管理器重写此方法，以返回适当的线程组。
返回：
    ThreadGroup 新线程被实例化后所在的线程组






