>原文：<https://docs.travis-ci.com/user/getting-started/>

### 本指南涵盖的内容
使用Travis CI与您的GitHub托管代码库的简短指南。 对这些一无所知？ 尝试阅读完整初学者的Travis CI。

或者，如果您想要一个更完整的特定语言指南，请选择以下其中一个：

...

### 开始使用Travis CI
- 使用您的GitHub帐户登录Travis CI，接受GitHub访问权限确认。

- 登录后，我们已从GitHub同步您的代码库，请转到您的个人资料页面，并启用要构建的代码库。 

    >注意：您只能为具有管理员权限的代码库启用Travis CI构建。
    
- 将`.travis.yml`文件添加到您的代码库以告知Travis CI要构建的内容：
    ```
    language: ruby
    rvm:
     - 2.2
     - jruby
     - rbx-2
    # uncomment and edit the following line if your project needs to run something other than `rake`:
    # script: bundle exec rspec spec
    ```
    这个例子告诉Travis CI，这是一个用Ruby编写的项目，用rake构建。 Travis CI针对Ruby 2.2和最新版本的JRuby和Rubinius来测试这个项目。

- 将`.travis.yml`文件添加到git，commit和push，以触发Travis CI构建： 
    
    >Travis只能在将代码库添加到Travis之后对您推送的提交进行构建。 

- 检查构建状态页面以查看构建是否通过或失败。

在自定义构建，安装依赖关系或设置数据库时，请确保您了解安全性最佳实践。 或者您可能只想要有关测试环境的更多信息？