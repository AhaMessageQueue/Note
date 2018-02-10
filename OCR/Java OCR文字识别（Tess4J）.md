  OCR （Optical Character Recognition，[光学字符识别](https://baike.baidu.com/item/%E5%85%89%E5%AD%A6%E5%AD%97%E7%AC%A6%E8%AF%86%E5%88%AB)）是指电子设备（例如扫描仪或数码相机）检查纸上打印的字符，通过检测暗、亮的模式确定其形状，然后用字符识别方法将形状翻译成计算机文字的过程；即，针对印刷体字符，采用光学的方式将纸质文档中的文字转换成为黑白点阵的图像文件，并通过识别软件将图像中的文字转换成文本格式，供文字处理软件进一步编辑加工的技术。如何除错或利用辅助信息提高识别正确率，是OCR最重要的课题，ICR（Intelligent Character Recognition）的名词也因此而产生。衡量一个OCR系统性能好坏的主要指标有：拒识率、误识率、识别速度、用户界面的友好性，产品的稳定性，易用性及可行性等。

       `Tess4J`是对`google tesseract ocr`的java库的一种实现

## Tess4J

Tesseract OCR API的Java JNA（JNA（Java Native Access ）提供一组Java工具类用于在运行期动态访问系统本地库（native library：如Window的dll）而不需要编写任何Native/JNI代码。）封装。

### 特点

该库提供光学字符识别（OCR）支持：

- TIFF，JPEG，GIF，PNG和BMP图像格式
- 多页TIFF图像
- PDF文档格式

### 教程

http://tess4j.sourceforge.net/tutorial/

#### Development with Tess4J

Tesseract, Ghostscript 和 Leptonica 的 Windows 32和64位DLL分别内嵌在tess4j.jar和lept4j.jar中。它们将在运行时自动提取和加载。对于其他平台，请务必先安装或构建Tesseract。

##### MAC

```
brew install tesseract
```

默认安装目录：`/usr/local/Cellar/tesseract`

##### NetBeans

创建一个Java项目。在`Projects`视图中，点击`Libraries`节点，再选择`Add JAR/Folder.... Find`，然后添加所有必需的JAR文件，包括jai_imageio.jar, jna.jar, commons-io-2.4.jar, lept4j.jar 和 tess4j.jar。

当然你也可以添加如下依赖：

```xml
<!-- https://mvnrepository.com/artifact/net.sourceforge.tess4j/tess4j -->
<dependency>
    <groupId>net.sourceforge.tess4j</groupId>
    <artifactId>tess4j</artifactId>
    <version>3.4.2</version>
</dependency>
```

创建一个名为`TesseractExample`的Java类

![image](http://upload-images.jianshu.io/upload_images/9800139-ced20e71b9bfac09.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

将tessdata和示例图像放在项目的根目录下。如果您选择文件视图，它应该显示如下：

![image](http://upload-images.jianshu.io/upload_images/9800139-4deccc83cbdc1583.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

您可以将NetBeans配置为使用JDK 64位启动以运行该示例; 这可以通过调整`NetBeans\etc\netbeans.conf`文件中的`netbeans_jdkhome`值。

##### Command-line on Windows 7 64-bit

创建一个工作目录，其内容和结构如下。将所有依赖的JAR文件放在lib子目录中。

![image](http://upload-images.jianshu.io/upload_images/9800139-4493ce5e3eaf09bd.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

在相应的目录下创建一个Java源文件`TesseractExample.java`。

![image](http://upload-images.jianshu.io/upload_images/9800139-26e57860751edbfc.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

在"开始搜索"框中输入`cmd`以打开一个32位命令提示符。导航到项目目录并执行以下命令：

```
C:\Projects\TessExample>javac -cp lib\* tess4j\example\TesseractExample.java

C:\Projects\TessExample>java -cp lib\*;. tess4j.example.TesseractExample
The (quick) [brown] {fox} jumps!
Over the $43,456.78 #90 dog
& duck/goose, as 12.5% of E-mail
from aspammer@website.com is spam.
...
```

>`-cp` <目录和 zip/jar 文件的类搜索路径>
>`-classpath` <目录和 zip/jar 文件的类搜索路径>
>用于搜索类文件。
>linux 用 : 分隔的目录, JAR 档案和 ZIP 档案列表
>windows 用 ; 分隔的目录, JAR 档案和 ZIP 档案列表

如果要使用自定义版本的DLL，则需要通过在启动VM之前设置`jna.library.path`系统属性或更改相应的库访问环境变量，使目标库可用于Java程序（请参阅 [Getting Started with JNA](https://github.com/twall/jna/blob/master/www/GettingStarted.md)）。

```
package tess4j.example;

import java.io.File;
import net.sourceforge.tess4j.*;

public class TesseractExample {
    public static void main(String[] args) {
        // System.setProperty("jna.library.path", "32".equals(System.getProperty("sun.arch.data.model")) ? "lib/win32-x86" : "lib/win32-x86-64");

        File imageFile = new File("eurotext.tif");
        ITesseract instance = new Tesseract();  // JNA Interface Mapping
        // ITesseract instance = new Tesseract1(); // JNA Direct Mapping
        // File tessDataFolder = LoadLibs.extractTessResources("tessdata"); // Maven build bundles English data
        // instance.setDatapath(tessDataFolder.getParent());

        try {
            String result = instance.doOCR(imageFile);
            System.out.println(result);
        } catch (TesseractException e) {
            System.err.println(e.getMessage());
        }
    }
}
```

## 常见问题

运行报错：

```
Failed loading language 'chi_sim'
Tesseract couldn't load any languages!
#
# A fatal error has been detected by the Java Runtime Environment:
#
#  SIGSEGV (0xb) at pc=0x00000001260c9ea8, pid=2235, tid=0x0000000000002603
#
# JRE version: Java(TM) SE Runtime Environment (8.0_131-b11) (build 1.8.0_131-b11)
# Java VM: Java HotSpot(TM) 64-Bit Server VM (25.131-b11 mixed mode bsd-amd64 compressed oops)
# Problematic frame:
# C  [libtesseract.dylib+0x12ea8]  tesseract::Tesseract::recog_all_words(PAGE_RES*, ETEXT_DESC*, TBOX const*, char const*, int)+0xaa
#
# Failed to write core dump. Core dumps have been disabled. To enable core dumping, try "ulimit -c unlimited" before starting Java again
#
# An error report file with more information is saved as:
# /Users/liuchunlong/IdeaProjects/gotop/hs_err_pid2235.log
#
# If you would like to submit a bug report, please visit:
#   http://bugreport.java.com/bugreport/crash.jsp
# The crash happened outside the Java Virtual Machine in native code.
# See problematic frame for where to report the bug.
#
Disconnected from the target VM, address: '127.0.0.1:53687', transport: 'socket'

Process finished with exit code 134 (interrupted by signal 6: SIGABRT)
```

Mac存在该问题，尝试Windows。


## 参考

https://github.com/tesseract-ocr/tesseract

https://github.com/tesseract-ocr/tessdata

https://github.com/nguyenq/tess4j