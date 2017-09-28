Solr使用Apache Tika项目的代码提供了一个框架，用于将许多不同的文件格式解析器（如Apache PDFBox和Apache POI）合并到Solr本身。
使用此框架，Solr的`ExtractingRequestHandler`可以使用Tika来支持上传二进制文件，包括流行格式（如Word和PDF）的文件，用于数据提取和索引。

当这个框架正在开发中时，它被称为Solr内容提取库或CEL; 该缩写来自该框架的名称：Solr Cell。

如果想要solr使用你自己的`ContentHandler`，你需要继承`ExtractingRequestHandler`并重写`createFactory()`方法。
该工厂负责构建与Tika交互的`SolrContentHandler`，并允许字面值来覆盖Tika解析的值。
设置参数`literalsOverride`，默认为true。为false的话，在Tika解析值的后面添加字面值。

有关Solr提取请求处理程序的更多信息，请参阅<https://wiki.apache.org/solr/ExtractingRequestHandler>

## Key Concepts

使用Solr Cell框架时，请注意以下几点：
- Tika将自动尝试确定输入文档类型（Word，PDF，HTML）并适当地提取内容。如果您愿意，您可以使用`stream.type`参数明确指定Tika的MIME类型。
- Tika的工作原理是生成一个XHTML流，它提供给SAX `ContentHandler`。SAX是一个许多不同XML解析器实现的通用接口。有关更多信息，请参阅<http://www.saxproject.org/quickstart.html>。
- Solr然后响应Tika的SAX事件，并创建要索引的字段。
- Tika根据DublinCore等规范生成title，subject，Author等元数据。有关支持的文件类型，请参见http://tika.apache.org/1.7/formats.html。
- Tika将所有提取的文本添加到`content`字段。
- 您可以将Tika的元数据字段映射到Solr字段。
- 你可以为字段值传入字面值。字面值将会覆盖Tika解析的值，包含Tika元数据对象中的字段，Tika的内容字段，任何可以获取的Tika 内容字段。
- 您可以将XPath表达式应用于Tika XHTML以限制生成的内容。

>虽然Apache Tika功能非常强大，但对某些文件而言并不完美。PDF文件特别有问题，主要是由于PDF格式本身。
在处理任何文件失败的情况下，`ExtractingRequestHandler`没有辅助机制来尝试从文件中提取一些文本；它会抛出异常并失败。

## Trying out Tika with the Solr techproducts Example

您可以使用Solr中包含的`techproducts`示例来尝试Tika框架。开始示例：

```text
bin/solr -e techproducts
```

您现在可以使用curl通过HTTP POST发送示例PDF文件：

```text
curl 'http://localhost:8983/solr/techproducts/update/extract?literal.id=doc1&commit=true' -F "myfile=@example/exampledocs/solr-word.pdf"
```

上面的URL调用`ExtractingRequestHandler`，上传文件`solr-word.pdf`并为其分配唯一的ID `doc1`。 仔细看看这个命令的组成部分：

- literal.id = doc1参数为要索引的文档提供必要的唯一ID。
- commit = true参数导致Solr在索引文档后执行提交，使其立即可搜索。为了在加载多个文档时获得最佳性能，在完成之前不要调用commit命令。
- `-F`标志指示curl使用`Content-Type multipart/form-data`POST提交数据，并支持二进制文件的上传。 `@`符号指示curl上传附件。
- 参数`myfile=@tutorial.html`需要一个有效的路径，可以是绝对的或相对的。

您也可以使用`bin/post`将PDF文件发送到Solr（没有参数，literal.id参数将被设置为文件的绝对路径）：

```text
bin/post -c techproducts example/exampledocs/solr-word.pdf -params "literal.id=a"
```

现在你应该可以执行一个查询并找到那个文档。您可以发出`http://localhost:8983/solr/techproducts/select?q=pdf`的请求。

您可能会注意到，虽然示例文档的内容已经被索引和存储，但是与此文档相关联的元数据字段没有很多。
这是因为根据在`solrconfig.xml`中为`/update/extract`handler配置的默认参数忽略未知字段，并且可以轻松地更改或覆盖此行为。
例如，要存储并查看所有元数据和内容，请执行以下操作：

```text
bin/post -c techproducts example/exampledocs/solr-word.pdf -params "literal.id=doc1&uprefix=attr_"
```

在此命令中，`uprefix = attr_`参数会使未在schema中定义的所有生成的字段以`attr_`作为前缀，`attr_`是存储和索引的动态字段。
此命令允许您使用属性查询文档，如：http://localhost:8983/solr/techproducts/select?q=attr_meta:microsoft。

## Input Parameters

下表描述了`ExtractingRequestHandler`接受的参数。

|参数 	|描述|
|-------|----|
|boost.\<fieldname\> 	|为指定字段加权|
|capture    |捕获指定的XHTML元素,支持添加到solr文档中.这个参数在复制XHTML中的某一块儿内容到指定字段时,非常有用.例如,它可以搜索\<p\>,索引它们到一个特别的字段.注意:content仍旧被抓取到整个"content"字段.|
|captureAttr 	|索引Tika XHTML的属性到单独的字段.如果设置为true,例如,从HTML中抽取内容时,Tika可以返回\<a\>标签元素中的href属性作为"a"字段.参考下面例子.|
|commitWithin 	|在指定毫秒时间内提交索引到磁盘|
|date.formats 	|定义文档识别的日期格式|
|defaultField 	|如果uprefix参数没有指定,字段不能被识别的时候,使用这个默认字段.|
|extractOnly 	|默认时false,如果为true,返回这个Tika抽取的内容,不索引这个文档.这在响应中逐字的包含抽取的XHTML字符串.在手动查看时,相对于xml来说它可能是更有用.以避免查看更多的嵌入的XHTML标签.参考http://wiki.apache.org/solr/TikaExtractOnlyExampleOutput.|
|extractFormat 	|默认时"xml".另外一个格式是"text".-x 表示xml  -t 表示text格式.只有在extractOnly为true的时候,这个参数才会有效.|
|fmap.\<source_field\> 	|source_field必须是输入文档的字段,它的值是需要映射到的solr的字段.例如 fmap.content=text使Tika生成的content字段内容移动到solr的text字段|
|literal.\<fieldname\> 	|使用指定的值占据solr的字段.这个数据可以是多值的如果这个字段是多值类型的话.|
|lowernames 	 |true/false.如果为true,所有字段都被映射为小写带有下划线.例如:"Content-Type"被映射为"content_type"|
|multipartUploadLimitInKB 	 |在上传大文件时很有用.定义允许文档的KB大小.|
|passwordsFile 	 |Defines a file path and name for a file of file name to password mappings.|
|resource.name 	 |文件名,Tika可以使用这个文件名确定文件MIME类型.|
|resource.password 	 |PDF或者OOXML文件可能使用的密码|
|tika.config 	 |定义了tika的配置文件.只有在你自定义实现Tika时才要求使用.|
|uprefix 	 |所有schema中没有定义的字段使用的前缀匹配.联合动态字段使用是非常有用的.例如uprefix=ignored_将有效忽略所有Tika产生的未知字段.schema中包含 \<dynamicField name="ignored_*" type="ignored"/\>.|
|xpath      |在抽取时,只返回Tika XHTML中满足xpath表示的内容.参考http://tika.apache.org/1.4/index.html了解更多 Tika XHTML的细节.同样参考http://wiki.apache.org/solr/TikaExtractOnlyExampleOutput|

## Order of Operations

以下是Solr Cell框架使用`ExtractingRequestHandler`和Tika处理其输入的顺序。

- Tika生成字段或以`literal.<fieldname>=<value>`指定的literals(字面值)形式传递它们。
    如果`literalsOverride = false`，literals将作为多值附加到Tika生成的字段。
- `如果lowernames = true`，Tika将字段映射为小写。
- Tika应用`fmap.source=target`参数指定的映射规则。
- 如果指定了`uprefix`，则任何未知字段名称都以该值为前缀，否则，如果指定了`defaultField`，则将任何未知字段复制到默认字段。

## Configuring the Solr ExtractingRequestHandler

如果您不使用提供的`sample_techproducts_configs`或`data_driven_schema_configs`[config set](https://lucene.apache.org/solr/guide/6_6/config-sets.html#config-sets)，
则必须配置自己的`solrconfig.xml`以了解包含`ExtractingRequestHandler`及其依赖关系的Jar：

```text
<lib dir="${solr.install.dir:../../..}/contrib/extraction/lib" regex=".*\.jar" />
<lib dir="${solr.install.dir:../../..}/dist/" regex="solr-cell-\d.*\.jar" />
```
然后可以在`solrconfig.xml`中配置`ExtractingRequestHandler`。

```xml
<requestHandler name="/update/extract" class="org.apache.solr.handler.extraction.ExtractingRequestHandler">
  <lst name="defaults">
    <str name="fmap.Last-Modified">last_modified</str>
    <str name="uprefix">ignored_</str>
  </lst>
  <!--Optional.  Specify a path to a tika configuration file. See the Tika docs for details.-->
  <str name="tika.config">/my/path/to/tika.config</str>
  <!-- Optional. Specify one or more date formats to parse. See DateUtil.DEFAULT_DATE_FORMATS
       for default date formats -->
  <lst name="date.formats">
    <str>yyyy-MM-dd</str>
  </lst>
  <!-- Optional. Specify an external file containing parser-specific properties.
       This file is located in the same directory as solrconfig.xml by default.-->
  <str name="parseContext.config">parseContext.xml</str>
</requestHandler>
```

在默认部分，我们将Tika的`Last-Modified` Metadata属性映射到名为`last_modified`的字段。 我们也告诉它忽视未定义的字段。这些都是覆盖参数。

`tika.config`指向包含Tika配置的文件。`date.formats`允许您指定用于将提取的输入转换为Date的各种`java.text.SimpleDateFormats`日期格式。

Solr配置了以下日期格式（请参阅Solr中的DateUtil）：

    yyyy-MM-dd’T’HH:mm:ss’Z'
    yyyy-MM-dd’T’HH:mm:ss
    yyyy-MM-dd
    yyyy-MM-dd hh:mm:ss
    yyyy-MM-dd HH:mm:ss
    EEE MMM d hh:mm:ss z yyyy
    EEE, dd MMM yyyy HH:mm:ss zzz
    EEEE, dd-MMM-yy HH:mm:ss zzz
    EEE MMM d HH:mm:ss yyyy

如果提交的文件比较大，使用下面限制:

```text
<requestDispatcher handleSelect="true" >
  <requestParsers enableRemoteStreaming="false" multipartUploadLimitInKB="20480" />
  ...
```

## Parser specific properties

Tika使用的解析器可能具有特定属性来控制数据的提取方式。例如，当Java程序使用Tika库时，`PDFParserConfig`类具有可以提取垂直定向文本的方法`setSortByPosition(boolean)`。 
要通过使用`ExtractingRequestHandler`的配置访问该方法，可以将`parseContext.config`属性添加到`solrconfig.xml`文件（参见上文），然后在Tika的PDFParserConfig中设置属性，如下所示。
请参阅Tika Java API文档，了解可为需要此级别控制的任何特定解析器设置的配置参数。

```xml
<entries>
  <entry class="org.apache.tika.parser.pdf.PDFParserConfig" impl="org.apache.tika.parser.pdf.PDFParserConfig">
    <property name="extractInlineImages" value="true"/>
    <property name="sortByPosition" value="true"/>
  </entry>
  <entry>...</entry>
</entries>
```

## Multi-Core Configuration
对于多core配置，您可以在`solr.xml`的`<solr/>`部分中指定`sharedLib ='lib'`，并将必要的jar文件放在那里。

有关Solr cores的更多信息，请参阅[The Well-Configured Solr Instance](https://lucene.apache.org/solr/guide/6_6/the-well-configured-solr-instance.html#the-well-configured-solr-instance)。

## Indexing Encrypted Documents with the ExtractingUpdateRequestHandler

如果在请求中的`resource.password`或`passwordsFile`文件中提供密码，`ExtractingRequestHandler`会解密加密文件并对其内容进行索引。

在`passwordsFile`的情况下，提供的文件必须格式化，因此每个规则一行。每个规则包含文件名正则表达式，后跟"="，然后以明文形式显示密码。
因为密码是明文的，所以文件应该有严格的访问限制。

```text
# This is a comment
myFileName = myPassword
.*\.docx$ = myWordPassword
.*\.pdf$ = myPdfPassword
```

## Examples

### Metadata

如前所述，Tika生成有关文档的元数据。元数据描述文档的不同方面，例如作者的姓名，页数，文件大小等。
生成的元数据取决于提交的文档的类型。例如，PDF具有不同于Word文档的元数据。

除了Tika的元数据，Solr还添加了以下元数据（在`ExtractingMetadataConstants`中定义）：

|solr元数据 	|描述|
|---------------|----|
|stream_name 	|这个Conent Stream的名字|
|stream_source_info 	|这个Conent Stream的源信息|
|stream_size 	|这个Conent Stream的字节大小|
|stream_content_type 	|这个Conent Stream的content type|

我们建议您尝试使用`extractOnly`选项来查看Solr为这些元数据元素设置的值。

## Examples of Uploads Using the Extracting Request Handler

### Capture and Mapping

以下命令分别捕获`<div>`标签，然后将该字段的所有实例映射到名为`foo_t`的动态字段。

```text
bin/post -c techproducts example/exampledocs/sample.html -params "literal.id=doc2&captureAttr=true&defaultField=_text_&fmap.div=foo_t&capture=div"
```

## Using Literals to Define Your Own Metadata

要添加自己的元数据，请传入字面参数(literal)以及文件：

```
bin/post -c techproducts -params "literal.id=doc4&captureAttr=true&defaultField=text&capture=div&fmap.div=foo_t&literal.blah_s=Bah" example/exampledocs/sample.html
```

## XPath

下面的示例传递一个XPath表达式来限制Tika返回的XHTML：

```text
bin/post -c techproducts -params "literal.id=doc5&captureAttr=true&defaultField=text&capture=div&fmap.div=foo_t&xpath=/xhtml:html/xhtml:body/xhtml:div//node()" example/exampledocs/sample.html
```

## Extracting Data without Indexing It

Solr允许您提取数据而无需索引。如果您将Solr仅用作提取服务器，或者您有兴趣测试Solr提取，则可能需要执行此操作。

下面的示例设置`extractOnly = true`参数以提取数据而不对其进行索引。

```text
curl "http://localhost:8983/solr/techproducts/update/extract?&extractOnly=true" --data-binary @example/exampledocs/sample.html -H 'Content-type:text/html'
```

输出包括由Tika生成的XML（并由Solr的XML进一步转义），使用不同的输出格式使其更易读（`-out yes`是指示工具将Solr的输出回显到控制台）：

```text
bin/post -c techproducts -params "extractOnly=true&wt=ruby&indent=true" -out yes example/exampledocs/sample.html
```