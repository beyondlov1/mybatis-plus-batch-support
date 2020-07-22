# mybatis-plus-batch-support
增强 mybatis-plus 批量插入/更新功能, 生成批量插入/更新sql

# 配合spring-boot-starter使用
https://github.com/beyondlov1/mybatis-plus-batch-spring-boot-starter

### 发布到maven私服
pom.xml 添加
```xml
    <distributionManagement>
        <repository>
            <id>nexus</id>
            <name>Nexus Repository</name>
            <url>http://xxx.xxx.com:8081/repository/maven-releases/</url>
        </repository>
    </distributionManagement>
```