最近有用到**mysql批量更新**，使用最原始的批量update发现性能很差，将网上看到的总结一下一共有以下三种办法：

### 一条记录update一次，性能很差
```
update test_tbl set dr='2' where id=1;
```
### replace into 或者insert into ...on duplicate key update
```
replace into test_tbl  (id,dr) values (1,'2'),(2,'3'),...(x,'y');
```
或者使用
```
insert into test_tbl (id,dr) values (1,'2'),(2,'3'),...(x,'y') on duplicate key update dr=values(dr);
```
### 创建临时表，先更新临时表，然后从临时表中update
```
create temporary table tmp (id int(4) primary key,dr varchar(50));

insert into tmp values (0,'gone'),(1,'xx'),...(m,'yy');

update test_tbl, tmp set test_tbl.dr=tmp.dr where test_tbl.id=tmp.id;
```

注意：这种方法需要用户有temporary 表的create 权限。

下面是上述方法update 100000条数据的性能测试结果：
```
逐条update
real 0m15.557s
user 0m1.684s
sys 0m1.372s

replace into
real 0m1.394s
user 0m0.060s
sys 0m0.012s

insert into on duplicate key updateal 0m1.474s
user 0m0.052s
sys 0m0.008s

create temporary table and update:
real 0m0.643s
user 0m0.064s
sys 0m0.004s
```

就测试结果来看，测试当时使用replace into性能较好。

replace into 和insert into on duplicate key update的不同在于：
- replace into　操作本质是对重复的记录先delete 后insert，如果更新的字段不全会将缺失的字段置为缺省值
- insert into 则是只update重复记录，不会改变其它字段。 但主键会自增+1。



**实例：**
```
truncate table student;
 
INSERT INTO student (name, age, count) VALUES ('xiaoming',1,1),('xiaohong',2,2),('xiaohui',3,3);
 
insert into student (name,age, count) values ('xiaoming',4,4),('xiaohong',5,5) on duplicate key update age=values(age), count=values(count);
 
replace into student (name,age) values ('xiaoming',4),('xiaohong',5);

```


