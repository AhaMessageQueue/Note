![](http://hi.csdn.net/attachment/201108/8/0_1312768262w885.gif)

如图所示，A,B,C三个Collection互相关联。 其中的数字为document的value值。

我们先建立A collection。
```
> var a={value:"1"}
> var b={value:"2"}
> var c={value:"9"}
> var d={value:"10"}
> db.A.save(a)
> db.A.save(b)
> db.A.save(c)
> db.A.save(d)
> db.A.find()
{ "_id" : ObjectId("4e3f33ab6266b5845052c02b"), "value" : "1" }
{ "_id" : ObjectId("4e3f33de6266b5845052c02c"), "value" : "2" }
{ "_id" : ObjectId("4e3f33e06266b5845052c02d"), "value" : "9" }
{ "_id" : ObjectId("4e3f33e26266b5845052c02e"), "value" : "10" }
```
B collection以A collection的  _id为ObjectId("4e3f33de6266b5845052c02c")作为Apid
所以：
```
> var Ba={Apid:[new DBRef('A',ObjectId("4e3f33de6266b5845052c02c"))],value:3}
> db.B.save(Ba)
> var Ba={Apid:[new DBRef('A',ObjectId("4e3f33de6266b5845052c02c"))],value:4}
> db.B.insert(Ba)
> var Ba={Apid:[new DBRef('A',ObjectId("4e3f33de6266b5845052c02c"))],value:7}
> db.B.insert(Ba)
> var Ba={Apid:[new DBRef('A',ObjectId("4e3f33de6266b5845052c02c"))],value:8}
> db.B.insert(Ba)
> db.B.find()
{ "_id" : ObjectId("4e3f3dd96266b5845052c035"), "Apid" : [ { "$ref" : "A", "$id" : ObjectId("4e3f33de6266b5845052c02c") } ], "value" : 3 }
{ "_id" : ObjectId("4e3f3de16266b5845052c036"), "Apid" : [ { "$ref" : "A", "$id" : ObjectId("4e3f33de6266b5845052c02c") } ], "value" : 4 }
{ "_id" : ObjectId("4e3f3dec6266b5845052c037"), "Apid" : [ { "$ref" : "A", "$id" : ObjectId("4e3f33de6266b5845052c02c") } ], "value" : 7 }
{ "_id" : ObjectId("4e3f3df06266b5845052c038"), "Apid" : [ { "$ref" : "A", "$id" : ObjectId("4e3f33de6266b5845052c02c") } ], "value" : 8 }
```
C collection以B collection的  _id为 ObjectId("4e3f3de16266b5845052c036") 作为Bpid
```
> var Ca={Bpid:[new DBRef('B',ObjectId("4e3f3de16266b5845052c036"))],value:5}
> db.C.save(Ca)
> var Ca={Bpid:[new DBRef('B',ObjectId("4e3f3de16266b5845052c036"))],value:6}
> db.C.save(Ca)
> db.C.find()
{ "_id" : ObjectId("4e3f42f36266b5845052c03d"), "Bpid" : [ { "$ref" : "B", "$id" : ObjectId("4e3f3de16266b5845052c036") } ], "value" : 5 }
{ "_id" : ObjectId("4e3f42f96266b5845052c03e"), "Bpid" : [ { "$ref" : "B", "$id" : ObjectId("4e3f3de16266b5845052c036") } ], "value" : 6 }
```
目前为止3个collection 的关系已经建成。

### 查询
```
> var a = db.B.findOne({"value":4})
> a.Apid.forEach(function(ref){printjson(db[ref.$ref].findOne({"_id":ref.$id}));})
{ "_id" : ObjectId("4e3f33de6266b5845052c02c"), "value" : "2" }
```
```
> db.A.findOne({"_id":db.B.findOne().Apid[0].$id})
{ "_id" : ObjectId("4e3f33de6266b5845052c02c"), "value" : "2" }
```
其实好好想想引用不是必须的。

MongoDB 权威指南说了这么一句：
>In short,the best time to use DBRefs are when you're storing heterogeneous references to documents in different collections.like when you want to take advantage of some additional DBRef-specific
functionality in a driver or tool.

### MongoDB Middle Level (关联多表查询)
```
 > var a = {name:"C++"}
> db
test
> db.language.save(a)
> db.language.find()
{ "_id" : ObjectId("4da32c897d2de864e0448e06"), "name" : "C++" }
> var b = {name:"javascript"}
> db.language.save(b)
> db.language.find()
{ "_id" : ObjectId("4da32c897d2de864e0448e06"), "name" : "C++" }
{ "_id" : ObjectId("4da32cb17d2de864e0448e07"), "name" : "javascript" }
> lan = {name:"obj1",computer:[new DBRef('language',a._id)]}
{
        "name" : "obj1",
        "computer" : [
                {
                        "$ref" : "language",
                        "$id" : ObjectId("4da32c897d2de864e0448e06")
                }
        ]
}
> lan.computer[0]
{ "$ref" : "language", "$id" : ObjectId("4da32c897d2de864e0448e06") }
> lan.computer[0].fetch()
{ "_id" : ObjectId("4da32c897d2de864e0448e06"), "name" : "C++" }
> db.language.insert(lan)
> db.language.find()
{ "_id" : ObjectId("4da32c897d2de864e0448e06"), "name" : "C++" }
{ "_id" : ObjectId("4da32cb17d2de864e0448e07"), "name" : "javascript" }
{ "_id" : ObjectId("4da33b487d2de864e0448e08"), "name" : "obj1", "computer" : [ { "$ref" : "language", "$id" : ObjectId("4da32c897d2de864e0448e06") } ] }
> db.language.findOne({name:"obj1"}).computer[0].fetch()
{ "_id" : ObjectId("4da32c897d2de864e0448e06"), "name" : "C++" }
> lan2 = {name:"obj2",computer:[new DBRef('language',b._id)]}
{
        "name" : "obj2",
        "computer" : [
                {
                        "$ref" : "language",
                        "$id" : ObjectId("4da32cb17d2de864e0448e07")
                }
        ]
}
> db.language.insert(lan2)
```

