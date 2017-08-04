```java
@Override
public void bulkSave(List<FileObject> fileObjects) {
    mongoTemplate.bulkOps(BulkOperations.BulkMode.ORDERED, FileObject.class).insert(fileObjects).execute();
}
```

```java
@Override
public int bulkUpdatePath(Long userId, List<FileObject> fileObjects) {
    Assert.notNull(fileObjects, "FileObjects can not be null");
    // ORDERED - 按顺序进行批量操作。第一个错误将取消处理。
    // UNORDERED - 并行执行批量操作。出现错误，处理将继续执行。
    BulkOperations bulkOperations = mongoTemplate.bulkOps(BulkOperations.BulkMode.ORDERED, FileObject.class);
    List<Pair<Query, Update>> updatePairs = new ArrayList<>();
    for (FileObject fileObject : fileObjects) {
        Query query = Query.query(Criteria.where("_id").is(fileObject.getId()).and("userId").is(userId).and("status")
                .is(FileStatusEnum.NORMAL.ordinal()));
        Update update = Update.update("path", fileObject.getPath())
                .set("sequence", fileObject.getSequence())
                .set("updated", fileObject.getUpdated());
        updatePairs.add(Pair.of(query, update));
    }
    BulkWriteResult bulkWriteResult = bulkOperations.updateOne(updatePairs).execute();
    return bulkWriteResult.getModifiedCount();
}
```