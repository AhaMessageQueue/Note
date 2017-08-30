Solr使用Apache Tika项目中的代码来提供一个框架，将许多不同的文件格式解析器（如Apache PDFBox和Apache POI）合并到Solr本身。 
使用此框架，Solr的ExtractingRequestHandler可以使用Tika来支持上传二进制文件，包括流行格式（如Word和PDF）的文件，用于数据提取和索引。

当这个框架正在开发中时，它被称为Solr内容提取库或CEL; 该缩写来自这个框架的名称：Solr Cell。

如果你要为Solr提供自己的`ContentHandler`，可以扩展`ExtractingRequestHandler`并覆盖`createFactory()`方法。
该工厂负责构建与Tika交互的`SolrContentHandler`，并允许文字覆盖Tika解析的值。
将通常默认为*true的参数`literalsOverride`设置为*false以将Tika解析的值作为多值附加到literals。

有关Solr提取请求处理程序的更多信息，请参阅https://wiki.apache.org/solr/ExtractingRequestHandler。

## Key Concepts

使用Solr Cell框架时，请记住以下几点：

- Tika将自动尝试确定输入文档类型（Word，PDF，HTML）并适当地提取内容。如果您愿意，您可以使用`stream.type`参数明确指定Tika的MIME类型。

- Tika通过生成一个XHTML流，它提供给SAX ContentHandler。SAX是为许多不同的XML解析器实现的通用接口。有关更多信息，请参阅http://www.saxproject.org/quickstart.html。

- Solr然后响应Tika的SAX事件并创建要索引的字段。

- Tika根据DublinCore等规范生成诸如“标题”，“主题”和“作者”等元数据。有关支持的文件类型，请参见http://tika.apache.org/1.7/formats.html。

- Tika将所有提取的文本添加到内容字段。

- 您可以将Tika的元数据字段映射到Solr字段。

- 您可以传入字面值。字面值将覆盖Tika解析的值，包括Tika元数据对象中的字段，Tika内容字段和任何"captured content"字段。

- 您可以将XPath表达式应用于Tika XHTML以限制生成的内容。

>虽然Apache Tika功能非常强大，但对某些文件而言并不完美。 PDF文件问题很多，主要是由于PDF格式本身。在处理任何文件失败的情况下，`ExtractingRequestHandler`没有辅助机制来尝试从文件中提取一些文本;它会抛出异常并失败。

## Trying out Tika with the Solr techproducts Example

您可以使用Solr中包含的技术产品示例来尝试Tika框架。

Start the example:

```text
bin/solr -e techproducts
```

您现在可以使用curl通过HTTP POST发送PDF文件：

```text
curl 'http://localhost:8983/solr/techproducts/update/extract?literal.id=doc1&commit=true' -F "myfile=@example/exampledocs/solr-word.pdf"
```

eg,

请求：

```text
curl 'http://localhost:8983/solr/techproducts/update/extract?literal.id=doc1&commit=true' -F "README_file=@/开发/软件/solr-6.6.0/README.txt"
```

响应：

```text
liuchunongdembp:bin liuchunlong$ curl 'http://localhost:8983/solr/techproducts/update/extract?literal.id=doc1&commit=true' -F "README_file=@/开发/软件/solr-6.6.0/README.txt"
<?xml version="1.0" encoding="UTF-8"?>
<response>
<lst name="responseHeader"><int name="status">0</int><int name="QTime">530</int></lst>
</response>
liuchunongdembp:bin liuchunlong$ 
```

查询：

```text
{
    "id":"doc1",
    "content_type":["text/plain; charset=windows-1252"],
    "content":[" \n \n  \n  \n  \n  \n  \n  \n  \n  \n  \n \n  # Licensed to the Apache Software Foundation (ASF) under one or more\r\n# contributor license agreements.  See the NOTICE file distributed with\r\n# this work for additional information regarding copyright ownership.\r\n# The ASF licenses this file to You under the Apache License, Version 2.0\r\n# (the \"License\"); you may not use this file except in compliance with\r\n# the License.  You may obtain a copy of the License at\r\n#\r\n#     http://www.apache.org/licenses/LICENSE-2.0\r\n#\r\n# Unless required by applicable law or agreed to in writing, software\r\n# distributed under the License is distributed on an \"AS IS\" BASIS,\r\n# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\r\n# See the License for the specific language governing permissions and\r\n# limitations under the License.\r\n\r\n\r\nWelcome to the Apache Solr project!\r\n-----------------------------------\r\n\r\nSolr is the popular, blazing fast open source enterprise search platform\r\nfrom the Apache Lucene project.\r\n\r\nFor a complete description of the Solr project, team composition, source\r\ncode repositories, and other details, please see the Solr web site at\r\nhttp://lucene.apache.org/solr\r\n\r\n\r\nGetting Started\r\n---------------\r\n\r\nTo start Solr for the first time after installation, simply do:\r\n\r\n  bin/solr start\r\n\r\nThis will launch a standalone Solr server in the background of your shell,\r\nlistening on port 8983. Alternatively, you can launch Solr in \"cloud\" mode,\r\nwhich allows you to scale out using sharding and replication. To launch Solr\r\nin cloud mode, do:\r\n\r\n  bin/solr start -cloud\r\n\r\nTo see all available options for starting Solr, please do:\r\n\r\n  bin/solr start -help\r\n\r\nAfter starting Solr, create either a core or collection depending on whether\r\nSolr is running in standalone (core) or SolrCloud mode (collection) by doing:\r\n\r\n  bin/solr create -c <name>\r\n\r\nThis will create a collection that uses a data-driven schema which tries to guess\r\nthe correct field type when you add documents to the index. To see all available\r\noptions for creating a new collection, execute:\r\n\r\n  bin/solr create -help\r\n\r\nAfter starting Solr, direct your Web browser to the Solr Admin Console at:\r\n\r\n  http://localhost:8983/solr/\r\n\r\n\r\nSolr Examples\r\n---------------\r\n\r\nSolr includes a few examples to help you get started. To run a specific example, do:\r\n\r\n  bin/solr -e <EXAMPLE> where <EXAMPLE> is one of:\r\n\r\n    cloud        : SolrCloud example\r\n    dih          : Data Import Handler (rdbms, mail, atom, tika)\r\n    schemaless   : Schema-less example (schema is inferred from data during indexing)\r\n    techproducts : Kitchen sink example providing comprehensive examples of Solr features\r\n\r\nFor instance, if you want to run the Solr Data Import Handler example, do:\r\n\r\n  bin/solr -e dih\r\n\r\n\r\nIndexing Documents\r\n---------------\r\n\r\nTo add documents to the index, use bin/post.  For example:\r\n\r\n     bin/post -c <collection_name> example/exampledocs/*.xml\r\n\r\nFor more information about Solr examples please read...\r\n\r\n * example/README.txt\r\n   For more information about the \"Solr Home\" and Solr specific configuration\r\n * http://lucene.apache.org/solr/quickstart.html\r\n   For a Quick Start guide\r\n * http://lucene.apache.org/solr/resources.html\r\n   For a list of other tutorials and introductory articles.\r\n\r\nor linked from \"docs/index.html\" in a binary distribution.\r\n\r\nAlso, there are Solr clients for many programming languages, see \r\n   http://wiki.apache.org/solr/IntegratingSolr\r\n\r\n\r\nFiles included in an Apache Solr binary distribution\r\n----------------------------------------------------\r\n\r\nserver/\r\n  A self-contained Solr instance, complete with a sample\r\n  configuration and documents to index. Please see: bin/solr start -help\r\n  for more information about starting a Solr server.\r\n\r\nexample/\r\n  Contains example documents and an alternative Solr home\r\n  directory containing examples of how to use the Data Import Handler,\r\n  see example/example-DIH/README.txt for more information.\r\n\r\ndist/solr-<component>-XX.jar\r\n  The Apache Solr libraries.  To compile Apache Solr Plugins,\r\n  one or more of these will be required.  The core library is\r\n  required at a minimum. (see http://wiki.apache.org/solr/SolrPlugins\r\n  for more information).\r\n\r\ndocs/index.html\r\n  A link to the online version of Apache Solr Javadoc API documentation and Tutorial\r\n\r\n\r\nInstructions for Building Apache Solr from Source\r\n-------------------------------------------------\r\n\r\n1. Download the Java SE 8 JDK (Java Development Kit) or later from http://www.oracle.com/java/\r\n   You will need the JDK installed, and the $JAVA_HOME/bin (Windows: %JAVA_HOME%\\bin) \r\n   folder included on your command path. To test this, issue a \"java -version\" command \r\n   from your shell (command prompt) and verify that the Java version is 1.8 or later.\r\n\r\n2. Download the Apache Ant binary distribution (1.8.2+) from \r\n   http://ant.apache.org/  You will need Ant installed and the $ANT_HOME/bin (Windows: \r\n   %ANT_HOME%\\bin) folder included on your command path. To test this, issue a \r\n   \"ant -version\" command from your shell (command prompt) and verify that Ant is \r\n   available. \r\n\r\n   You will also need to install Apache Ivy binary distribution (2.2.0) from \r\n   http://ant.apache.org/ivy/ and place ivy-2.2.0.jar file in ~/.ant/lib -- if you skip \r\n   this step, the Solr build system will offer to do it for you.\r\n\r\n3. Download the Apache Solr distribution, linked from the above web site. \r\n   Unzip the distribution to a folder of your choice, e.g. C:\\solr or ~/solr\r\n   Alternately, you can obtain a copy of the latest Apache Solr source code\r\n   directly from the GIT repository:\r\n\r\n     http://lucene.apache.org/solr/versioncontrol.html\r\n\r\n4. Navigate to the \"solr\" folder and issue an \"ant\" command to see the available options\r\n   for building, testing, and packaging Solr.\r\n  \r\n   NOTE: \r\n   To see Solr in action, you may want to use the \"ant server\" command to build\r\n   and package Solr into the server directory. See also server/README.txt.\r\n\r\n\r\nExport control\r\n-------------------------------------------------\r\nThis distribution includes cryptographic software.  The country in\r\nwhich you currently reside may have restrictions on the import,\r\npossession, use, and/or re-export to another country, of\r\nencryption software.  BEFORE using any encryption software, please\r\ncheck your country's laws, regulations and policies concerning the\r\nimport, possession, or use, and re-export of encryption software, to\r\nsee if this is permitted.  See <http://www.wassenaar.org/> for more\r\ninformation.\r\n\r\nThe U.S. Government Department of Commerce, Bureau of Industry and\r\nSecurity (BIS), has classified this software as Export Commodity\r\nControl Number (ECCN) 5D002.C.1, which includes information security\r\nsoftware using or performing cryptographic functions with asymmetric\r\nalgorithms.  The form and manner of this Apache Software Foundation\r\ndistribution makes it eligible for export under the License Exception\r\nENC Technology Software Unrestricted (TSU) exception (see the BIS\r\nExport Administration Regulations, Section 740.13) for both object\r\ncode and source code.\r\n\r\nThe following provides more details on the included cryptographic\r\nsoftware:\r\n    Apache Solr uses the Apache Tika which uses the Bouncy Castle generic encryption libraries for\r\n    extracting text content and metadata from encrypted PDF files.\r\n    See http://www.bouncycastle.org/ for more details on Bouncy Castle.\r\n \n  "],
    "_version_":1576667887261712384
}
```

上面的URL调用Extracting Request Handler，上传文件`solr-word.pdf`并为其分配唯一的ID doc1。仔细的看看这个命令的组成部分：

`literal.id = doc1`参数为要索引的文档提供必要的唯一ID。

`commit = true`参数使Solr在索引文档后执行提交，使其立即可搜索。为了在加载多个文档时获得最佳性能，在完成之前不要调用commit命令。

`-F`标志指示CURL使用`Content-Type`为`multipart/form-data`来POST数据，并支持二进制文件的上传。`@`符号指示curl上传附件。

参数`myfile=@example/exampledocs/solr-word.pdf`需要一个有效的路径，可以是绝对的或相对的。

您也可以使用`bin/post`将PDF文件发送到Solr
（without the params, the `literal.id` parameter would be set to the absolute path to the file）：

```text
bin/post -c techproducts example/exampledocs/solr-word.pdf -params "literal.id=a"
```

eg,

请求：

```text
bin/post -c techproducts /开发/软件/solr-6.6.0/README.txt -params "literal.id=a"
```

响应：

```text
liuchunongdembp:bin liuchunlong$ ./post -c techproducts /开发/软件/solr-6.6.0/README.txt -params "literal.id=a"
/Library/Java/JavaVirtualMachines/jdk1.8.0_131.jdk/Contents/Home/bin/java -classpath /开发/软件/solr-6.6.0/dist/solr-core-6.6.0.jar -Dauto=yes -Dparams=literal.id=a -Dc=techproducts -Ddata=files org.apache.solr.util.SimplePostTool /开发/软件/solr-6.6.0/README.txt
SimplePostTool version 5.0.0
Posting files to [base] url http://localhost:8983/solr/techproducts/update?literal.id=a...
Entering auto mode. File endings considered are xml,json,jsonl,csv,pdf,doc,docx,ppt,pptx,xls,xlsx,odt,odp,ods,ott,otp,ots,rtf,htm,html,txt,log
POSTing file README.txt (text/plain) to [base]/extract
1 files indexed.
COMMITting Solr index changes to http://localhost:8983/solr/techproducts/update?literal.id=a...
Time spent: 0:00:00.119
```

查询：

```json
{
    "id":"a",
    "resourcename":"/开发/软件/solr-6.6.0/README.txt",
    "content_type":["text/plain; charset=windows-1252"],
    "content":[" \n \n  \n  \n  \n  \n  \n  \n  \n  \n \n  # Licensed to the Apache Software Foundation (ASF) under one or more\r\n# contributor license agreements.  See the NOTICE file distributed with\r\n# this work for additional information regarding copyright ownership.\r\n# The ASF licenses this file to You under the Apache License, Version 2.0\r\n# (the \"License\"); you may not use this file except in compliance with\r\n# the License.  You may obtain a copy of the License at\r\n#\r\n#     http://www.apache.org/licenses/LICENSE-2.0\r\n#\r\n# Unless required by applicable law or agreed to in writing, software\r\n# distributed under the License is distributed on an \"AS IS\" BASIS,\r\n# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\r\n# See the License for the specific language governing permissions and\r\n# limitations under the License.\r\n\r\n\r\nWelcome to the Apache Solr project!\r\n-----------------------------------\r\n\r\nSolr is the popular, blazing fast open source enterprise search platform\r\nfrom the Apache Lucene project.\r\n\r\nFor a complete description of the Solr project, team composition, source\r\ncode repositories, and other details, please see the Solr web site at\r\nhttp://lucene.apache.org/solr\r\n\r\n\r\nGetting Started\r\n---------------\r\n\r\nTo start Solr for the first time after installation, simply do:\r\n\r\n  bin/solr start\r\n\r\nThis will launch a standalone Solr server in the background of your shell,\r\nlistening on port 8983. Alternatively, you can launch Solr in \"cloud\" mode,\r\nwhich allows you to scale out using sharding and replication. To launch Solr\r\nin cloud mode, do:\r\n\r\n  bin/solr start -cloud\r\n\r\nTo see all available options for starting Solr, please do:\r\n\r\n  bin/solr start -help\r\n\r\nAfter starting Solr, create either a core or collection depending on whether\r\nSolr is running in standalone (core) or SolrCloud mode (collection) by doing:\r\n\r\n  bin/solr create -c <name>\r\n\r\nThis will create a collection that uses a data-driven schema which tries to guess\r\nthe correct field type when you add documents to the index. To see all available\r\noptions for creating a new collection, execute:\r\n\r\n  bin/solr create -help\r\n\r\nAfter starting Solr, direct your Web browser to the Solr Admin Console at:\r\n\r\n  http://localhost:8983/solr/\r\n\r\n\r\nSolr Examples\r\n---------------\r\n\r\nSolr includes a few examples to help you get started. To run a specific example, do:\r\n\r\n  bin/solr -e <EXAMPLE> where <EXAMPLE> is one of:\r\n\r\n    cloud        : SolrCloud example\r\n    dih          : Data Import Handler (rdbms, mail, atom, tika)\r\n    schemaless   : Schema-less example (schema is inferred from data during indexing)\r\n    techproducts : Kitchen sink example providing comprehensive examples of Solr features\r\n\r\nFor instance, if you want to run the Solr Data Import Handler example, do:\r\n\r\n  bin/solr -e dih\r\n\r\n\r\nIndexing Documents\r\n---------------\r\n\r\nTo add documents to the index, use bin/post.  For example:\r\n\r\n     bin/post -c <collection_name> example/exampledocs/*.xml\r\n\r\nFor more information about Solr examples please read...\r\n\r\n * example/README.txt\r\n   For more information about the \"Solr Home\" and Solr specific configuration\r\n * http://lucene.apache.org/solr/quickstart.html\r\n   For a Quick Start guide\r\n * http://lucene.apache.org/solr/resources.html\r\n   For a list of other tutorials and introductory articles.\r\n\r\nor linked from \"docs/index.html\" in a binary distribution.\r\n\r\nAlso, there are Solr clients for many programming languages, see \r\n   http://wiki.apache.org/solr/IntegratingSolr\r\n\r\n\r\nFiles included in an Apache Solr binary distribution\r\n----------------------------------------------------\r\n\r\nserver/\r\n  A self-contained Solr instance, complete with a sample\r\n  configuration and documents to index. Please see: bin/solr start -help\r\n  for more information about starting a Solr server.\r\n\r\nexample/\r\n  Contains example documents and an alternative Solr home\r\n  directory containing examples of how to use the Data Import Handler,\r\n  see example/example-DIH/README.txt for more information.\r\n\r\ndist/solr-<component>-XX.jar\r\n  The Apache Solr libraries.  To compile Apache Solr Plugins,\r\n  one or more of these will be required.  The core library is\r\n  required at a minimum. (see http://wiki.apache.org/solr/SolrPlugins\r\n  for more information).\r\n\r\ndocs/index.html\r\n  A link to the online version of Apache Solr Javadoc API documentation and Tutorial\r\n\r\n\r\nInstructions for Building Apache Solr from Source\r\n-------------------------------------------------\r\n\r\n1. Download the Java SE 8 JDK (Java Development Kit) or later from http://www.oracle.com/java/\r\n   You will need the JDK installed, and the $JAVA_HOME/bin (Windows: %JAVA_HOME%\\bin) \r\n   folder included on your command path. To test this, issue a \"java -version\" command \r\n   from your shell (command prompt) and verify that the Java version is 1.8 or later.\r\n\r\n2. Download the Apache Ant binary distribution (1.8.2+) from \r\n   http://ant.apache.org/  You will need Ant installed and the $ANT_HOME/bin (Windows: \r\n   %ANT_HOME%\\bin) folder included on your command path. To test this, issue a \r\n   \"ant -version\" command from your shell (command prompt) and verify that Ant is \r\n   available. \r\n\r\n   You will also need to install Apache Ivy binary distribution (2.2.0) from \r\n   http://ant.apache.org/ivy/ and place ivy-2.2.0.jar file in ~/.ant/lib -- if you skip \r\n   this step, the Solr build system will offer to do it for you.\r\n\r\n3. Download the Apache Solr distribution, linked from the above web site. \r\n   Unzip the distribution to a folder of your choice, e.g. C:\\solr or ~/solr\r\n   Alternately, you can obtain a copy of the latest Apache Solr source code\r\n   directly from the GIT repository:\r\n\r\n     http://lucene.apache.org/solr/versioncontrol.html\r\n\r\n4. Navigate to the \"solr\" folder and issue an \"ant\" command to see the available options\r\n   for building, testing, and packaging Solr.\r\n  \r\n   NOTE: \r\n   To see Solr in action, you may want to use the \"ant server\" command to build\r\n   and package Solr into the server directory. See also server/README.txt.\r\n\r\n\r\nExport control\r\n-------------------------------------------------\r\nThis distribution includes cryptographic software.  The country in\r\nwhich you currently reside may have restrictions on the import,\r\npossession, use, and/or re-export to another country, of\r\nencryption software.  BEFORE using any encryption software, please\r\ncheck your country's laws, regulations and policies concerning the\r\nimport, possession, or use, and re-export of encryption software, to\r\nsee if this is permitted.  See <http://www.wassenaar.org/> for more\r\ninformation.\r\n\r\nThe U.S. Government Department of Commerce, Bureau of Industry and\r\nSecurity (BIS), has classified this software as Export Commodity\r\nControl Number (ECCN) 5D002.C.1, which includes information security\r\nsoftware using or performing cryptographic functions with asymmetric\r\nalgorithms.  The form and manner of this Apache Software Foundation\r\ndistribution makes it eligible for export under the License Exception\r\nENC Technology Software Unrestricted (TSU) exception (see the BIS\r\nExport Administration Regulations, Section 740.13) for both object\r\ncode and source code.\r\n\r\nThe following provides more details on the included cryptographic\r\nsoftware:\r\n    Apache Solr uses the Apache Tika which uses the Bouncy Castle generic encryption libraries for\r\n    extracting text content and metadata from encrypted PDF files.\r\n    See http://www.bouncycastle.org/ for more details on Bouncy Castle.\r\n \n  "],
    "_version_":1576669851445886976
}
```

现在你应该可以执行一个查询并找到那个文件。 您可以发出`http://localhost:8983/solr/techproducts/select?q=pdf`的请求。

您可能会注意到，虽然示例文档的内容已经被索引和存储，但是与该文档相关联的元数据字段没有很多。
这是因为根据为`solrconfig.xml`中的`/update/extract` handler 配置的默认参数忽略未知字段，并且可以轻松地更改或覆盖此行为。 
例如，要存储和查看所有元数据和内容，请执行以下操作：

```text
bin/post -c techproducts example/exampledocs/solr-word.pdf -params "literal.id=doc1&uprefix=attr_"
```

eg,
```text
bin/post -c techproducts /开发/软件/solr-6.6.0/README.txt -params "literal.id=doc1&uprefix=attr_"
```

```json
{
    "attr_meta":["stream_size",
      "7453",
      "X-Parsed-By",
      "org.apache.tika.parser.DefaultParser",
      "X-Parsed-By",
      "org.apache.tika.parser.txt.TXTParser",
      "stream_content_type",
      "text/plain",
      "Content-Encoding",
      "windows-1252",
      "resourceName",
      "/开发/软件/solr-6.6.0/README.txt",
      "Content-Type",
      "text/plain; charset=windows-1252"],
    "id":"doc1",
    "attr_stream_size":["7453"],
    "attr_x_parsed_by":["org.apache.tika.parser.DefaultParser",
      "org.apache.tika.parser.txt.TXTParser"],
    "attr_stream_content_type":["text/plain"],
    "attr_content_encoding":["windows-1252"],
    "resourcename":"/开发/软件/solr-6.6.0/README.txt",
    "content_type":["text/plain; charset=windows-1252"],
    "content":[" \n \n  \n  \n  \n  \n  \n  \n  \n  \n \n  # Licensed to the Apache Software Foundation (ASF) under one or more\r\n# contributor license agreements.  See the NOTICE file distributed with\r\n# this work for additional information regarding copyright ownership.\r\n# The ASF licenses this file to You under the Apache License, Version 2.0\r\n# (the \"License\"); you may not use this file except in compliance with\r\n# the License.  You may obtain a copy of the License at\r\n#\r\n#     http://www.apache.org/licenses/LICENSE-2.0\r\n#\r\n# Unless required by applicable law or agreed to in writing, software\r\n# distributed under the License is distributed on an \"AS IS\" BASIS,\r\n# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\r\n# See the License for the specific language governing permissions and\r\n# limitations under the License.\r\n\r\n\r\nWelcome to the Apache Solr project!\r\n-----------------------------------\r\n\r\nSolr is the popular, blazing fast open source enterprise search platform\r\nfrom the Apache Lucene project.\r\n\r\nFor a complete description of the Solr project, team composition, source\r\ncode repositories, and other details, please see the Solr web site at\r\nhttp://lucene.apache.org/solr\r\n\r\n\r\nGetting Started\r\n---------------\r\n\r\nTo start Solr for the first time after installation, simply do:\r\n\r\n  bin/solr start\r\n\r\nThis will launch a standalone Solr server in the background of your shell,\r\nlistening on port 8983. Alternatively, you can launch Solr in \"cloud\" mode,\r\nwhich allows you to scale out using sharding and replication. To launch Solr\r\nin cloud mode, do:\r\n\r\n  bin/solr start -cloud\r\n\r\nTo see all available options for starting Solr, please do:\r\n\r\n  bin/solr start -help\r\n\r\nAfter starting Solr, create either a core or collection depending on whether\r\nSolr is running in standalone (core) or SolrCloud mode (collection) by doing:\r\n\r\n  bin/solr create -c <name>\r\n\r\nThis will create a collection that uses a data-driven schema which tries to guess\r\nthe correct field type when you add documents to the index. To see all available\r\noptions for creating a new collection, execute:\r\n\r\n  bin/solr create -help\r\n\r\nAfter starting Solr, direct your Web browser to the Solr Admin Console at:\r\n\r\n  http://localhost:8983/solr/\r\n\r\n\r\nSolr Examples\r\n---------------\r\n\r\nSolr includes a few examples to help you get started. To run a specific example, do:\r\n\r\n  bin/solr -e <EXAMPLE> where <EXAMPLE> is one of:\r\n\r\n    cloud        : SolrCloud example\r\n    dih          : Data Import Handler (rdbms, mail, atom, tika)\r\n    schemaless   : Schema-less example (schema is inferred from data during indexing)\r\n    techproducts : Kitchen sink example providing comprehensive examples of Solr features\r\n\r\nFor instance, if you want to run the Solr Data Import Handler example, do:\r\n\r\n  bin/solr -e dih\r\n\r\n\r\nIndexing Documents\r\n---------------\r\n\r\nTo add documents to the index, use bin/post.  For example:\r\n\r\n     bin/post -c <collection_name> example/exampledocs/*.xml\r\n\r\nFor more information about Solr examples please read...\r\n\r\n * example/README.txt\r\n   For more information about the \"Solr Home\" and Solr specific configuration\r\n * http://lucene.apache.org/solr/quickstart.html\r\n   For a Quick Start guide\r\n * http://lucene.apache.org/solr/resources.html\r\n   For a list of other tutorials and introductory articles.\r\n\r\nor linked from \"docs/index.html\" in a binary distribution.\r\n\r\nAlso, there are Solr clients for many programming languages, see \r\n   http://wiki.apache.org/solr/IntegratingSolr\r\n\r\n\r\nFiles included in an Apache Solr binary distribution\r\n----------------------------------------------------\r\n\r\nserver/\r\n  A self-contained Solr instance, complete with a sample\r\n  configuration and documents to index. Please see: bin/solr start -help\r\n  for more information about starting a Solr server.\r\n\r\nexample/\r\n  Contains example documents and an alternative Solr home\r\n  directory containing examples of how to use the Data Import Handler,\r\n  see example/example-DIH/README.txt for more information.\r\n\r\ndist/solr-<component>-XX.jar\r\n  The Apache Solr libraries.  To compile Apache Solr Plugins,\r\n  one or more of these will be required.  The core library is\r\n  required at a minimum. (see http://wiki.apache.org/solr/SolrPlugins\r\n  for more information).\r\n\r\ndocs/index.html\r\n  A link to the online version of Apache Solr Javadoc API documentation and Tutorial\r\n\r\n\r\nInstructions for Building Apache Solr from Source\r\n-------------------------------------------------\r\n\r\n1. Download the Java SE 8 JDK (Java Development Kit) or later from http://www.oracle.com/java/\r\n   You will need the JDK installed, and the $JAVA_HOME/bin (Windows: %JAVA_HOME%\\bin) \r\n   folder included on your command path. To test this, issue a \"java -version\" command \r\n   from your shell (command prompt) and verify that the Java version is 1.8 or later.\r\n\r\n2. Download the Apache Ant binary distribution (1.8.2+) from \r\n   http://ant.apache.org/  You will need Ant installed and the $ANT_HOME/bin (Windows: \r\n   %ANT_HOME%\\bin) folder included on your command path. To test this, issue a \r\n   \"ant -version\" command from your shell (command prompt) and verify that Ant is \r\n   available. \r\n\r\n   You will also need to install Apache Ivy binary distribution (2.2.0) from \r\n   http://ant.apache.org/ivy/ and place ivy-2.2.0.jar file in ~/.ant/lib -- if you skip \r\n   this step, the Solr build system will offer to do it for you.\r\n\r\n3. Download the Apache Solr distribution, linked from the above web site. \r\n   Unzip the distribution to a folder of your choice, e.g. C:\\solr or ~/solr\r\n   Alternately, you can obtain a copy of the latest Apache Solr source code\r\n   directly from the GIT repository:\r\n\r\n     http://lucene.apache.org/solr/versioncontrol.html\r\n\r\n4. Navigate to the \"solr\" folder and issue an \"ant\" command to see the available options\r\n   for building, testing, and packaging Solr.\r\n  \r\n   NOTE: \r\n   To see Solr in action, you may want to use the \"ant server\" command to build\r\n   and package Solr into the server directory. See also server/README.txt.\r\n\r\n\r\nExport control\r\n-------------------------------------------------\r\nThis distribution includes cryptographic software.  The country in\r\nwhich you currently reside may have restrictions on the import,\r\npossession, use, and/or re-export to another country, of\r\nencryption software.  BEFORE using any encryption software, please\r\ncheck your country's laws, regulations and policies concerning the\r\nimport, possession, or use, and re-export of encryption software, to\r\nsee if this is permitted.  See <http://www.wassenaar.org/> for more\r\ninformation.\r\n\r\nThe U.S. Government Department of Commerce, Bureau of Industry and\r\nSecurity (BIS), has classified this software as Export Commodity\r\nControl Number (ECCN) 5D002.C.1, which includes information security\r\nsoftware using or performing cryptographic functions with asymmetric\r\nalgorithms.  The form and manner of this Apache Software Foundation\r\ndistribution makes it eligible for export under the License Exception\r\nENC Technology Software Unrestricted (TSU) exception (see the BIS\r\nExport Administration Regulations, Section 740.13) for both object\r\ncode and source code.\r\n\r\nThe following provides more details on the included cryptographic\r\nsoftware:\r\n    Apache Solr uses the Apache Tika which uses the Bouncy Castle generic encryption libraries for\r\n    extracting text content and metadata from encrypted PDF files.\r\n    See http://www.bouncycastle.org/ for more details on Bouncy Castle.\r\n \n  "],
    "_version_":1576670316560646144
}
```

在此命令中，`uprefix = attr_`参数将使未在schema中定义的所有生成的字段以`attr_`作为前缀，`attr_`是存储和索引的动态字段。

此命令允许您使用属性查询文档，如：`http://localhost:8983/solr/techproducts/select?q=attr_meta:microsoft`.

## Input Parameters

下表描述了Extracting Request Handler接受的参数


|Parameter	|Description|
|-----------|------------|
|capture|Captures XHTML elements with the specified name for a supplementary addition to the Solr document. This parameter can be useful for copying chunks of the XHTML into a separate field. For instance, it could be used to grab paragraphs (<p>) and index them into a separate field. Note that content is still also captured into the overall "content" field.|
|captureAttr|Indexes attributes of the Tika XHTML elements into separate fields, named after the element. If set to true, for example, when extracting from HTML, Tika can return the href attributes in <a> tags as fields named "a". See the examples below.|
|commitWithin|Add the document within the specified number of milliseconds.|
|date.formats|Defines the date format patterns to identify in the documents.|
|defaultField|If the uprefix parameter (see below) is not specified and a field cannot be determined, the default field will be used.|
|extractOnly|Default is false. If true, returns the extracted content from Tika without indexing the document. This literally includes the extracted XHTML as a string in the response. When viewing manually, it may be useful to use a response format other than XML to aid in viewing the embedded XHTML tags.For an example, see http://wiki.apache.org/solr/TikaExtractOnlyExampleOutput.|
|extractFormat|Default is "xml", but the other option is "text". Controls the serialization format of the extract content. The xml format is actually XHTML, the same format that results from passing the -x command to the Tika command line application, while the text format is like that produced by Tika’s -t command. This parameter is valid only if extractOnly is set to true.|
|fmap.<source_field>|Maps (moves) one field name to another. The source_field must be a field in incoming documents, and the value is the Solr field to map to. Example: fmap.content=text causes the data in the content field generated by Tika to be moved to the Solr’s text field.|
|ignoreTikaException|If true, exceptions found during processing will be skipped. Any metadata available, however, will be indexed.|
|literal.<fieldname>|Populates a field with the name supplied with the specified value for each document. The data can be multivalued if the field is multivalued.|
|literalsOverride|If true (the default), literal field values will override other values with the same field name. If false, literal values defined with literal.<fieldname> will be appended to data already in the fields extracted from Tika. If setting literalsOverride to "false", the field must be multivalued.|
|lowernames|Values are "true" or "false". If true, all field names will be mapped to lowercase with underscores, if needed. For example, "Content-Type" would be mapped to "content_type."|
|multipartUploadLimitInKB|Useful if uploading very large documents, this defines the KB size of documents to allow.|
|passwordsFile|Defines a file path and name for a file of file name to password mappings.|
|resource.name|Specifies the optional name of the file. Tika can use it as a hint for detecting a file’s MIME type.|
|resource.password|Defines a password to use for a password-protected PDF or OOXML file|
|tika.config|Defines a file path and name to a customized Tika configuration file. This is only required if you have customized your Tika implementation.|
|uprefix|Prefixes all fields that are not defined in the schema with the given prefix. This is very useful when combined with dynamic field definitions. Example: uprefix=ignored_ would effectively ignore all unknown fields generated by Tika given the example schema contains <dynamicField name="ignored_*" type="ignored"/>|
|xpath|When extracting, only return Tika XHTML content that satisfies the given XPath expression. See http://tika.apache.org/1.7/index.html for details on the format of Tika XHTML. See also http://wiki.apache.org/solr/TikaExtractOnlyExampleOutput.|

## Order of Operations

以下是使用`Extracting Request Handler`和`Tika`处理其输入的Solr Cell框架的顺序。

Tika生成字段或以`literal.<fieldname>=<value>`指定的literals(字面值)形式传递它们。
如果`literalsOverride = false`，literals将作为多值附加到Tika生成的字段。

如果`lowernames = true`，Tika将字段映射为小写。

Tika应用`fmap.source=target`参数指定的映射规则。

如果指定了`uprefix`，则任何未知字段名称都以该值为前缀，否则，如果指定了`defaultField`，则将任何未知字段复制到默认字段。

## Configuring the Solr ExtractingRequestHandler

如果您不使用提供的`sample_techproducts_configs`或`data_driven_schema_configs` config set，则必须配置自己的solrconfig.xml以了解包含ExtractingRequestHandler及其依赖关系的Jar：

```xml
<lib dir="${solr.install.dir:../../..}/contrib/extraction/lib" regex=".*\.jar" />
<lib dir="${solr.install.dir:../../..}/dist/" regex="solr-cell-\d.*\.jar" />
```

然后，您可以在`solrconfig.xml`中配置`ExtractingRequestHandler`。

```xml
<requestHandler name="/update/extract" class="org.apache.solr.handler.extraction.ExtractingRequestHandler">
  <lst name="defaults">
    <str name="fmap.Last-Modified">last_modified</str>
    <!-- 如果不忽略未定义字段，使用attr_ -->
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

在默认部分，我们将Tika的`Last-Modified`元属性映射到名为`last_modified`的字段。我们也告诉它忽视未定义的字段。这些都是覆盖参数。

`tika.config`指向包含Tika配置的文件。`date.formats`允许您指定用于将提取的输入转换为Date的各种java.text.SimpleDateFormats日期格式。Solr配置了以下日期格式（请参阅Solr中的DateUtil）：

- yyyy-MM-dd’T’HH:mm:ss’Z'
- yyyy-MM-dd’T’HH:mm:ss
- yyyy-MM-dd
- yyyy-MM-dd hh:mm:ss
- yyyy-MM-dd HH:mm:ss
- EEE MMM d hh:mm:ss z yyyy
- EEE, dd MMM yyyy HH:mm:ss zzz
- EEEE, dd-MMM-yy HH:mm:ss zzz
- EEE MMM d HH:mm:ss yyyy

如果要提交非常大的文档，您还可能需要调整`multipartUploadLimitInKB`属性，如下所示。

```xml
<requestDispatcher handleSelect="true" >
  <requestParsers enableRemoteStreaming="false" multipartUploadLimitInKB="20480" />
```

### Parser specific properties

Tika使用的解析器可能具有特定属性来控制数据的提取方式。
例如，当Java程序使用Tika库时，PDFParserConfig类具有可以提取垂直定向文本的方法`setSortByPosition(boolean)`。
要使用`ExtractingRequestHandler`通过配置访问该方法，可以将`parseContext.config`属性添加到`solrconfig.xml`文件（参见上文），
然后在Tika的`PDFParserConfig`中设置属性，如下所示。 
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

### Multi-Core Configuration

对于多core配置，您可以在`solr.xml`的`<solr />`部分中指定`sharedLib ='lib'`，并将必要的jar文件放在那里。
有关Solr内核的更多信息，请参阅[配置良好的Solr实例](https://lucene.apache.org/solr/guide/6_6/the-well-configured-solr-instance.html#the-well-configured-solr-instance)。

## Indexing Encrypted Documents with the ExtractingUpdateRequestHandler

如果在请求中的`resource.password`或`passwordFile`文件中提供密码，`ExtractingRequestHandler`会解密加密文件并对其内容进行索引。

在`passwordFile`的情况下，提供的文件必须格式化，每个规则一行。每个规则包含文件名正则表达式，后跟"="，然后以明文形式显示的密码。 因为密码是明文的，所以文件应该有严格的访问限制。

```text
# This is a comment
myFileName = myPassword
.*\.docx$ = myWordPassword
.*\.pdf$ = myPdfPassword
```

## Examples

### Metadata

如前所述，Tika生成关于该文档的元数据。元数据描述文档的不同方面，例如作者的名称，页数，文件大小等。生成的元数据取决于提交的文档的类型。例如，PDF具有不同于Word文档的元数据。

除了Tika的元数据，Solr还添加了以下元数据（在ExtractingMetadataConstants中定义）：

|Solr Metadata	|Description|
|----------------|-----------|
|stream_name|The name of the Content Stream as uploaded to Solr. Depending on how the file is uploaded, this may or may not be set|
|stream_source_info|Any source info about the stream. (See the section on Content Streams later in this section.)|
|stream_size|The size of the stream in bytes.|
|stream_content_type|The content type of the stream, if available.|

>我们建议您尝试使用`extractOnly`选项来查看Solr为这些元数据元素设置的值。

extractOnly：默认值为false。
如果为true，则从Tika返回提取的内容，而无需索引文档。
这个字面上包含了提取的XHTML作为响应中的一个字符串。
当手动查看时，使用XML之外的响应格式可能有助于查看嵌入的XHTML标签。
有关示例，请参阅http://wiki.apache.org/solr/TikaExtractOnlyExampleOutput。

### Examples of Uploads Using the Extracting Request Handler

#### Capture and Mapping

以下命令分别捕获`<div>`标签，然后将该字段的所有实例映射到名为foo_t的动态字段。

```text
bin/post -c techproducts example/exampledocs/sample.html -params "literal.id=doc2&captureAttr=true&defaultField=_text_&fmap.div=foo_t&capture=div"
```

#### Using Literals to Define Your Own Metadata

要添加自己的元数据，请传入literal参数以及文件：

```text
bin/post -c techproducts -params "literal.id=doc4&captureAttr=true&defaultField=text&capture=div&fmap.div=foo_t&literal.blah_s=Bah" example/exampledocs/sample.html
```

#### XPath

下面的示例传递一个XPath表达式来限制Tika返回的XHTML：

```text
bin/post -c techproducts -params "literal.id=doc5&captureAttr=true&defaultField=text&capture=div&fmap.div=foo_t&xpath=/xhtml:html/xhtml:body/xhtml:div//node()" example/exampledocs/sample.html
```

### Extracting Data without Indexing It

Solr允许您提取数据而不进行索引。如果您将Solr仅用作提取服务器，或者您有兴趣测试Solr提取，则可能需要执行此操作。

下面的示例设置`extractOnly = true`参数以提取数据而不对其进行索引。

```text
curl "http://localhost:8983/solr/techproducts/update/extract?&extractOnly=true" --data-binary @example/exampledocs/sample.html -H 'Content-type:text/html'
```

输出包括由Tika生成的XML（并由Solr的XML进一步转义），使用不同的输出格式使其更易读（`-out yes`指示工具将Solr的输出回显到控制台）：

```text
bin/post -c techproducts -params "extractOnly=true&wt=ruby&indent=true" -out yes example/exampledocs/sample.html
```

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