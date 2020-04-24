## 山商论坛
学习 spring boot
## 部署
### 依赖
- Git
- JDK
- Maven
- MySQL
### 步骤
- yum update
- yum intstall git
- mkdir App
- cd App
- git clone https://github.com/Yangyang176/community.git
- yum install maven
- mvn -v
- mvn compile package
- cp src/main/resources/application.properties src/main/resources/application-production.properties
- vim src/main/resources/application-production.properties
- mvn package
- java -jar -Dspring.profiles.active=production target/community-0.0.1-SNAPSHOT.jar
- mvn clean compile flyway:migrate
- vim /usr/lib/jvm/java-1.8.0-openjdk-1.8.0.242.b08-0.el7_7.x86_64/jre/lib/security/java.security
## 资料
- [mybatis依赖配置](https://mybatis.org/mybatis-3/configuration.html#settings)
- [mybatis spring-boot 自动配置](http://mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/)
- [Spring Boot参考指南](https://docs.spring.io/spring-boot/docs/2.0.0.RC1/reference/htmlsingle/)
- [Markdown插件](https://pandao.github.io/editor.md/)
- [Spring Boot日志](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-logging)
- [Spring Boot定时器](https://spring.io/guides/gs/scheduling-tasks/)
## 脚本
```bash
mvn flyway:migrate
mvn -Dmybatis.generator.overwrite=true mybatis-generator:generate
```

