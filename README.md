JaCoCo Java Code Coverage Library
=================================

[![Build Status](https://travis-ci.org/jacoco/jacoco.svg?branch=master)](https://travis-ci.org/jacoco/jacoco)
[![Build status](https://ci.appveyor.com/api/projects/status/g28egytv4tb898d7/branch/master?svg=true)](https://ci.appveyor.com/project/JaCoCo/jacoco/branch/master)
[![Maven Central](https://img.shields.io/maven-central/v/org.jacoco/jacoco.svg)](http://search.maven.org/#search|ga|1|g%3Aorg.jacoco)

JaCoCo is a free Java code coverage library distributed under the Eclipse Public
License. Check the [project homepage](http://www.jacoco.org/jacoco)
for downloads, documentation and feedback.

Please use our [mailing list](https://groups.google.com/forum/?fromgroups=#!forum/jacoco)
for questions regarding JaCoCo which are not already covered by the
[extensive documentation](http://www.jacoco.org/jacoco/trunk/doc/).

Note: We do not answer general questions in the project's issue tracker. Please use our [mailing list](https://groups.google.com/forum/?fromgroups=#!forum/jacoco) for this.
-------------------------------------------------------------------------
# 一、打包
1. 下载代码，执行mvn clean package -Dmaven.javadoc.skip=true -Dmaven.test.skip=true
2. 进入jacoco\target目录，解压刚打的包jacoco-0.8.5.202006300741.zip
3. 使用压缩软件打开jacocoagent.jar，删除META-INF目录下*.SF,*.DSA,*.RSA文件
4. 使用压缩软件打开jacococli.jar，删除META-INF目录下*.SF,*.DSA,*.RSA文件
5. 使用压缩文件打开jacococli.jar，删除com\jcraft\jsch
6. 使用压缩文件打开jsch-0.1.55.jar，将其中\com\jcraft\jsch拷贝到jacococli.jar\com\jcraft\目录下
# 二、使用方式
支持3种方式，分别是全量覆盖、基于分支的增量覆盖、基于tag的增量覆盖
## 1、启动服务
1. jar包启动，启动命令如下
```
java -javaagent:F:\jacoco-0.8.5.20200630074\lib\jacocoagent.jar=includes=*,output=tcpserver,port=2020,address=localhost,append=true -jar zdh-0.0.1-SNAPSHOT.jar
```
2. tomcat启动
windows在catalina.bat第二行添加如下脚本
```
set JAVA_OPTS=-javaagent:F:\jacoco-0.8.5.20200630074\lib\jacocoagent.jar=includes=*,output=tcpserver,port=2020,address=localhost,append=true -Xverify:none
```
linux在catalina.sh第二行添加如下脚本
```
JAVA_OPTS="-javaagent:/home/systemtools/jacoco-0.8.5.20200630074/lib/jacocoagent.jar=includes=*,output=tcpserver,port=2020,address=localhost,append=true -Xverify:none"
```
## 2、导出覆盖率数据
```
java -jar F:\jacoco-0.8.5.20200630074\lib\jacococli.jar dump --address localhost --port 2020 --destfile zdh.exec
```
## 3、生成报告
1. 全量报告
```
java -jar F:\jacoco-0.8.5.20200630074\lib\jacococli.jar report zdh.exec --type 1 --classfiles E:\workspace\zdh\target\classes\com  --sourcefiles E:\workspace\zdh\src\main\java --html report --xml report.xml
```

2. 基于分支的增量报告
```
java -jar F:\jacoco-0.8.5.20200630074\lib\jacococli.jar report zdh.exec --gitName zhangfei --gitPassword 123456 --gitDir E:\workspace\zdh --type 2 --newBranchName dev1 --oldBranchName dev2 --classfiles E:\workspace\zdh\target\classes\com  --sourcefiles E:\workspace\zdh\src\main\java --html report --xml report.xml
```
3. 基于tag的增量报告
```
java -jar F:\jacoco-0.8.5.20200630074\lib\jacococli.jar report zdh.exec --gitName zhangfei --gitPassword 123456 --gitDir E:\workspace\zdh --branchName master --newTag 49117d1 --oldTag f1563cc --classfiles E:\workspace\zdh\target\classes\com  --sourcefiles E:\workspace\zdh\src\main\java --html report --xml report.xml
```

4. 参数说明
- classfiles，编译后的class目录
- sourcefiles，源文件目录，理论可以不传,不传的话 只能到文件级的展示,再细 需要具体代码和行数就需要这个了
**以下是新增的参数说明**
- type，分析类型，1全量，2基于分支增量，3基于tag增量，不传默认等于3
- gitDir，git本地目录，只有type=2或者3时需要此参数，不传默认当前目录
- gitName，git用户名，只有type=2或者3时需要此参数
- gitPassword，git密码，只有type=2或者3时需要此参数
- branchName，分支名称，type=3时需要此参数，不传默认master
- newTag，新tag（预发版本），type=3时需要此参数
- oldTag，基线tag（变更前的版本），type=3时需要此参数
- newBranchName，开发分支（预发分支），type=2时需要此参数
- oldBranchName，基线分支，type=2时需要此参数，不传默认master