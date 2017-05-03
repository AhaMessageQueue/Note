## RDBMS : Apache DBCP
`DataNucleus`提供了一个内建版本的DBCP来提供连接池。 如果使用RDBMS，则会自动选择此选项，除非另有说明。 
另一种方法是使用外部DBCP（DBCP）。 
这可以通过指定持久性属性`datanucleus.connectionPoolingType`等来访问

```
// Specify our persistence properties used for creating our PMF
Properties props = new Properties();
properties.setProperty("datanucleus.ConnectionDriverName","com.mysql.jdbc.Driver");
properties.setProperty("datanucleus.ConnectionURL","jdbc:mysql://localhost/myDB");
properties.setProperty("datanucleus.ConnectionUserName","login");
properties.setProperty("datanucleus.ConnectionPassword","password");
properties.setProperty("datanucleus.connectionPoolingType", "DBCP");
```
所以PMF将使用DBCP连接池。 为此，您将需要commons-dbcp，commons-pool和commons-collections JARs位于CLASSPATH中。您还可以指定持久性属性来控制连接池。 DBCP当前支持的属性如下所示
```
# Pooling of Connections
datanucleus.connectionPool.maxIdle=10
datanucleus.connectionPool.minIdle=3
datanucleus.connectionPool.maxActive=5
datanucleus.connectionPool.maxWait=60

# Pooling of PreparedStatements
datanucleus.connectionPool.maxStatements=0

datanucleus.connectionPool.testSQL=SELECT 1

datanucleus.connectionPool.timeBetweenEvictionRunsMillis=2400000
datanucleus.connectionPool.minEvictableIdleTimeMillis=18000000
```

