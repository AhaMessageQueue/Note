```
＄Git stash
```
可用来**暂存当前正在进行的工作**， 比如想`pull`最新代码，又不想加新`commit`， 或者另外一种情况，为了fix一个紧急的bug, 
先`stash`, 使**返回到自己上一个commit**, 改完bug之后再`stash pop`, 继续原来的工作。

基础命令：
```
$git stash

//do some work

$git stash pop
```

进阶：
当你多次使用`git stash`命令后，你的栈里将充满了未提交的代码，这时候你会对将哪个版本应用回来有些困惑，
`git stash list`命令可以将当前的Git栈信息打印出来，你只需要将找到对应的版本号，例如使用`git stash apply stash@{1}`
就可以将你指定版本号为stash@{1}的工作取出来，当你将所有的栈都应用回来的时候，可以使用`git stash clear`来将栈清空