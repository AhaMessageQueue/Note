一般来说一个开源项目有好几种启动方式——比如可以从命令行启动，也可以从web端启动。今天就看看如何设计命令行启动...
# Apache Commons CLI
Apache Commons CLI是开源的命令行解析工具，它可以帮助开发者快速构建启动命令，并且帮助你组织命令的参数、以及输出列表等。

## Maven

```
<dependency>
    <groupId>commons-cli</groupId>
    <artifactId>commons-cli</artifactId>
    <version>1.4</version>
</dependency>
```

CLI分为三个过程：

- 定义阶段：在Java代码中定义Options参数，定义参数、是否需要输入值、简单的描述等
- 解析阶段：应用程序传入参数后，CLI进行解析
- 询问阶段：通过查询CommandLine询问进入到哪个程序分支中

## 举个栗子

### 定义阶段:

```
Options options = new Options();

Option opt = new Option("h", "help", false, "Print help");
opt.setRequired(false);
options.addOption(opt);

opt = new Option("c", "configFile", true, "Name server config properties file");
opt.setRequired(false);
options.addOption(opt);

opt = new Option("p", "printConfigItem", false, "Print all config item");
opt.setRequired(false);
options.addOption(opt);
```

其中Option的参数：
- 第一个参数：参数的简单形式
- 第二个参数：参数的复杂形式
- 第三个参数：是否需要额外的输入
- 第四个参数：对参数的描述信息

### 解析阶段

通过解析器解析参数

```
CommandLine commandLine = null;
CommandLineParser parser = new PosixParser();
try {
    commandLine = parser.parse(options, args);
}catch(Exception e){
    // TODO
}
```

### 询问阶段

根据commandLine查询参数，提供服务

```
HelpFormatter hf = new HelpFormatter();
hf.setWidth(110);

if (commandLine.hasOption('h')) {
    // 打印使用帮助
    hf.printHelp("testApp", options, true);
}
```

##  全部代码样例

```
package com.fnpac.gotop.helper.cmd;

import org.apache.commons.cli.*;

/**
 * Created by liuchunlong on 2018/1/14.
 */
public class CLITest {

    public static void main(String[] args) {
        args = new String[]{"-h", "-c", "config.xml"};
        testOptions(args);
    }

    private static void testOptions(String[] args) {
        Options options = new Options();
        Option opt = new Option("h", "help", false, "Print help");
        // 设置此选项是否是强制性的
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("c", "configFile", true, "Name server config properties file");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("p", "printConfigItem", false, "Print all config item");
        opt.setRequired(false);
        options.addOption(opt);

        HelpFormatter hf = new HelpFormatter();
        hf.setWidth(110);

        CommandLine commandLine = null;
        CommandLineParser parser = new DefaultParser();

        try {
            commandLine = parser.parse(options, args);
            if (commandLine.hasOption('h')) {
                /*
                    printHelp方法:
                        用指定的命令行语法打印帮助。此方法将帮助信息打印到System.out。
                        Parameters:
                            cmdLineSyntax - 应用程序的语法
                            options - Options 实例
                            autoUsage - 是否打印自动生成的使用说明
                 */
                hf.printHelp("CliTest", options, true);

                // 打印opts的名称和值
                System.out.println("==========================================================");
                Option[] opts = commandLine.getOptions();
                if (opts != null) {
                    for (Option c_opt : opts) {
                        String name = c_opt.getLongOpt();
                        String value = commandLine.getOptionValue(name);
                        System.out.println(name + " -> " + value);
                    }
                }
            }
        } catch (ParseException e) {
            hf.printHelp("CliTest", options, true);
        }

    }
}
```

### 运行结果

```
usage: CliTest [-c <arg>] [-h] [-p]
 -c,--configFile <arg>   Name server config properties file
 -h,--help               Print help
 -p,--printConfigItem    Print all config item
==========================================================
help -> null
configFile -> config.xml
```
