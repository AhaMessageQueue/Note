把本地MongoDB中的数据导入(批量插入)到服务器172.16.16.90的数据库中

### 1.导出数据:
```
mongoexport -d report -c exceptions -o outdatafile.dat
```
选项解释:

- -d 指明使用的库, 本例中为” report”
- -c 指明要导出的集合, 本例中为”exceptions”
- -o 指明要导出的文件名, 本例中为”outdatafile.dat”

不指明地址一般会保存在MongoDB数据目录下

### 2.连接远程数据库并导入
```
mongoimport -h 172.16.16.90 -d report -c exceptions --file ./outdatafile.dat --upsert
```
选项解释:

- -d    指定把数据导入到哪一个数据库中
- -c    指定把数据导入到哪一个集合中
- --type    指定导入的数据类型
- --file       指定从哪一个文件中导入数据
- --headerline    仅适用于导入csv,tsv格式的数据，表示文件中的第一行作为数据头
- --upsert  以新增或者更新的方式来导入数据