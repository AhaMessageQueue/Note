`CASE`语句可以在`SELECT`子句和`ORDER BY`子句中使用.
`CASE`语句分为两种Case Simple Expression and Case Search Expression

### Case Simple Expression:
```
CASE Column1
WHEN V1 THEN R1
WHEN V2 THEN R2
ELSE R3
END
```
### Case Search Expression
```
CASE 
WHEN C1=V1 THEN R1
WHEN C2=V2 THEN R2
ELSE R3
END
```

当在`Orderby`子句中使用`Case`语句时,如果排序是按照ASC的话，会将不满足条件的结果集无序地放在总结果集的前面，然后将满足条件的结果集排序后附加到总结果集中。
如果使用DESC的话，则将满足条件的结果集放在总结果集的前面，然后将未满足条件的结果集无序地附加到总结果集后面：
![](http://pic002.cnblogs.com/images/2012/174228/2012101220105352.jpg)

![](http://pic002.cnblogs.com/images/2012/174228/2012101220110894.jpg)

### 示例
```
/**
     * 获取微信用户列表
     *      
     *      查询逻辑： 根据`unionId`查询符合条件的微信用户列表，然后对该结果进行排序。
     *                  首先对指定`appId`的微信用户对于`user_id`字段进行降序，即指定`appId`的微信用户排在前面，
     *                  然后将不满足指定`appId`的微信用户排在后面
     *      
     * @param unionId
     * @param appId
     * @return
     */
@Override
public List<WXUser> queryWXUsersByUnionId(String unionId, String appId) {
    String sql = "select user_id,union_id,open_id,app_id from wx_user where union_id = :union_id "
            + "order by case when app_id = :appId then 1 else 0 end desc";
    Map<String, Object> parameterMap = new HashMap<String, Object>();
    parameterMap.put("union_id", unionId);
    parameterMap.put("appId", appId);
    return getObjList(sql.toString(), rowMapper, parameterMap);
}
```

