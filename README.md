# 文件共享平台项目启动指南

## 克隆代码到本地

1. 使用以下命令克隆代码仓库到本地：

   ```bash
   git clone https://github.com/w626w/java_fileshare.git 
   ```

或者直接下载项目压缩包并解压。

## 连接数据库

打开项目文件，找到并修改数据库连接配置文件。

编辑 `src/main/resources/application.yml` 文件中的以下内容：

```yaml
username: root
password: 123456
```

将其修改为你自己的数据库用户名和密码。

## 创建数据库

以mysql运行 `src/main/resources/schema.sql` 文件以创建所需的数据库和表结构。

## 启动项目

运行 `src/main/java/com/fileshare/FileShareApplication.java` 文件以启动项目。

## 访问应用

打开浏览器，访问以下地址：

```
http://localhost:8080/
```

## 注意事项

项目功能存在较多错误，但可以勉强运行。 启动程序后，建议首先注册一个用户名为 admin 的用户，系统会默认该用户为管理员。
