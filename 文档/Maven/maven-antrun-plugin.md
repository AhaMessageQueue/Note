ant是一个老牌的项目打包管理系统了，目前虽然已经慢慢被maven取代，但其功能的强大仍然是很多场合下的首选，尤其是众多的task可以基本满足任何需求。其实在maven中也有使用ant的需求，比如不同环境打包编译时使用不同的配置信息等，或者是说做一些文件删除、复制之类的事情，这有些是maven做不来的，而ant就可以了，况且maven中已经有了maven-antrun-plugin插件，专门为在maven中运行ant做好了准备。 

使用这个插件，只需要在项目的pom文件中定义如下插件片段： 

```
    <plugin>
       <groupId>org.apache.maven.plugins</groupId>
       <artifactId>maven-antrun-plugin</artifactId>    
         <executions>       
            <execution>         
                 <phase>compile</phase>        
                       <goals>            
                             <goal>run</goal>        
                       </goals>             
                       <configuration>       
                             <tasks>          
                                 <delete file="${project.build.directory}/classes/abc.properties" />        
                             </tasks>          
                       </configuration>        
            </execution>    
         </executions>  
    </plugin>  
```

这里，我们在maven的编译阶段执行一些文件的删除操作，比如将测试环境的配置文件删除，复制生产环境的配置文件等等，我们都可以使用ant的task来定义。通过ant的maven插件，可以将ant的强大功能也都引入到maven中来，实现二者的强强结合。

参考文档：

1、maven-antrun-plugin插件：http://maven.apache.org/plugins/maven-antrun-plugin/

2、ant的task列表总览：http://ant.apache.org/manual/tasksoverview.html 