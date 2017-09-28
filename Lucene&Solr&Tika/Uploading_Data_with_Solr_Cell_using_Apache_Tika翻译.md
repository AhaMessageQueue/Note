## Sending Documents to Solr with a POST

下面的示例将文件作为POST的主体进行流式传输，然后，向Solr提供有关文件名称的信息。

```text
curl "http://localhost:8983/solr/techproducts/update/extract?literal.id=doc6&defaultField=text&commit=true" --data-binary @example/exampledocs/sample.html -H 'Content-type:text/html'
```

## Sending Documents to Solr with Solr Cell and SolrJ

SolrJ是一个Java客户端，可用于向索引添加文档，更新索引或查询索引。您将在[Client API](https://lucene.apache.org/solr/guide/6_6/client-apis.html#client-apis)中找到有关SolrJ的更多信息。

以下是使用Solr Cell和SolrJ将文档添加到Solr索引的示例。

首先，让我们使用SolrJ创建一个新的SolrClient，然后我们构造一个包含ContentStream（本质上是一个文件的包装器）的请求，并发送给Solr：

```text
public class SolrCellRequestDemo {
  public static void main (String[] args) throws IOException, SolrServerException {
    SolrClient client = new HttpSolrClient.Builder("http://localhost:8983/solr/my_collection").build();
    ContentStreamUpdateRequest req = new ContentStreamUpdateRequest("/update/extract");
    req.addFile(new File("my-file.pdf"));
    req.setParam(ExtractingParams.EXTRACT_ONLY, "true");
    NamedList<Object> result = client.request(req);
    System.out.println("Result: " + result);
}
```

此操作将文件`my-file.pdf`流式导入Solr的`my_collection`索引。

上面的示例代码调用extract命令，但您可以轻松地替换Solr Cell支持的其他命令。 要使用的关键类是`ContentStreamUpdateRequest`，可以确保`ContentStream`设置正确。

请注意，`ContentStreamUpdateRequest`不仅仅是Solr Cell特有的。


>补充：

由SolrCell提取的文件主体。

注意：默认情况下，此字段未创建索引，它在下面使用copyField被复制到"text"字段。
这是为了节省空间。使用此字段返回和高亮显示。使用"text"字段搜索内容。