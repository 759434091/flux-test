# Spring Boot + WebFlux + ReactiveData 组合实践

本文使用有支持非阻塞驱动的MongoDB作为数据库, 并且以MongoDB初始模式运行, 没有任何认证

使用此组合你需要或者最好需要以下技能知识

- Spring 框架
- Java 8 Stream API
- MongoDB
- Project Reactor 响应式框架

本文代码将会从上层开始到下层开始

## POM

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-mongodb-reactive</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
```

## Properties

配置一下 MongoDB 数据源

```properties
spring.data.mongodb.database=test
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.repositories.type=auto
```

## DEMO POJO

Lombok 注解可以省不小事情

`@Document` 标注 Text 是 MongoDB 的一个文件对象 将会自动生成

`@JsonSerialize(using = ObjectIdSerializer.class)` 是为了处理mongoDB 的 ID 序列化问题, 输出JSON时候将其转成`String`

```java
package pers.a9043.demo.fluxtest.po;

import java.io.IOException;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author luxueneng
 * @since 2019-03-29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
public class Text {
    @Id
    @JsonSerialize(using = ObjectIdSerializer.class)
    ObjectId id;
    String text;

    public static class ObjectIdSerializer extends JsonSerializer<ObjectId> {
        @Override
        public void serialize(ObjectId objectId, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
                throws IOException {
            jsonGenerator.writeString(objectId.toString());
        }
    }
}

```



## Web Configuration

仅仅使用`RouterFunction`简单注册了路由

```java
package pers.a9043.demo.fluxtest.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import pers.a9043.demo.fluxtest.web.TestHandler;

/**
 * @author luxueneng
 * @since 2019-03-29
 */
@Configuration
public class WebConfig {
    @Bean
    public RouterFunction<ServerResponse> route(@Autowired TestHandler testHandler) {
        return RouterFunctions.route()
                .GET("/texts/{id}", testHandler.get)
                // .GET("/texts/{id}", testHandler::get2)
                .GET("/texts", testHandler.getAll)
                .POST("/texts", testHandler.insert)
                .PUT("/texts/{id}",testHandler.update)
                .build();
    }
}

```



## Web

你一样可以使用SpringMVC方式(Annotated Controllers)书写你的V层, 也可以使用新的`HandlerFunction`&`RouterFunction`



Example.

```java
package pers.a9043.demo.fluxtest.web;

import javax.annotation.Resource;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import pers.a9043.demo.fluxtest.po.Text;
import pers.a9043.demo.fluxtest.service.TestService;
import reactor.core.publisher.Mono;

/**
 * @author luxueneng
 * @since 2019-03-29
 */
@Component
public class TestHandler {
    @Resource
    private TestService testService;

    /**
     * 插入
     */
    public HandlerFunction<ServerResponse> insert = serverRequest -> serverRequest
            .queryParam("text")
            .map(str -> Text.builder().text(str).build())
            .map(testService::insert)
            .map(textMono -> ServerResponse.ok().body(textMono, Text.class))
            .orElse(ServerResponse.badRequest().build());

    /**
     * 获取一条
     */
    public HandlerFunction<ServerResponse> get = serverRequest -> Mono
            .just(serverRequest.pathVariable("id"))
            .map(ObjectId::new)
            .map(testService::get)
            .flatMap(textMono -> ServerResponse.ok().body(textMono, Text.class));

    /**
     * 获取所有
     */
    public HandlerFunction<ServerResponse> getAll = request -> ServerResponse.ok().body(testService.get(), Text.class);

    /**
     * 更新
     */
    public HandlerFunction<ServerResponse> update = request -> request
            .bodyToMono(Text.class)
            .map(text -> testService.update(text))
            .flatMap(textMono -> ServerResponse.ok().body(textMono, Text.class));
}

```

当然 你也可以写成这样, 只需要能够符合`HandlerFunction`接口即可

```java
    public Mono<ServerResponse> get(ServerRequest serverRequest) {
        return Mono
                .just(serverRequest.pathVariable("id"))
                .map(ObjectId::new)
                .map(testService::get)
                .flatMap(textMono -> ServerResponse.ok().body(textMono, Text.class));
    }
```



## Service

由于只是一个DEMO, Service层其实没有存在的必要. 不过为了规范, 还是写一个Service空业务包装一下

```java
package pers.a9043.demo.fluxtest.service;

import javax.annotation.Resource;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import pers.a9043.demo.fluxtest.po.Text;
import pers.a9043.demo.fluxtest.repository.TestRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author luxueneng
 * @since 2019-03-29
 */
@Service
public class TestService {
    @Resource
    private TestRepository testRepository;

    /**
     * 插入
     *
     * @param text Text
     *
     * @return Mono
     */
    public Mono<Text> insert(Text text) {
        return testRepository.save(text);
    }

    /**
     * 获取一条
     *
     * @param id 主键
     *
     * @return Mono
     */
    public Mono<Text> get(ObjectId id) {
        return testRepository.findById(id);
    }

    /**
     * 获取所有
     *
     * @return Flux
     */
    public Flux<Text> get() {
        return testRepository.findAll();
    }

    /**
     * 更新一条
     *
     * @param text text Text
     *
     * @return Mono
     */
    public Mono<Text> update(Text text) {
        return testRepository
                .findById(text.getId())
                .doOnNext(dbText -> dbText.setText(text.getText()))
                .flatMap(testRepository::save);
    }
}

```

## Repository

这里的Repo其实和JPA基本一样, 不过返回的是一个`Publisher`对象

JPA和Mybatis都是非常不错的ORM, 只不过这里Mybatis没有很好的支持. Reactive Data几乎目前最好选择



你可以选择继承`ReactiveCrudRepository`类以及其任意子类,继承`ReactiveMongoRepository能够获得更多的默认方法

```java
package pers.a9043.demo.fluxtest.repository;

import org.bson.types.ObjectId;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import pers.a9043.demo.fluxtest.po.Text;

/**
 * @author luxueneng
 * @since 2019-03-29
 */
@Repository
public interface TestRepository extends ReactiveCrudRepository<Text, ObjectId> {
}

```



## 测试

启动Spring Boot, 使用一个合适的REST Client测试

### 测试插入

`POST http://localhost:8080/texts?test=33333`

得到 response

```json
{"id":"5c9de1c0418146123e7b28ae","text":"33333"}
```

### 测试查询

`GET http://localhost:8080/texts/5c9de1c0418146123e7b28ae`

得到 response

```json
{"id":"5c9de1c0418146123e7b28ae","text":"33333"}
```

### 测试查询全部

`GET http://localhost:8080/texts`

得到 response

```json
[{"id":"5c9dbf7c4181468603b08562","text":"mod"},{"id":"5c9dc0f141814600963ca1cf","text":"text1"},{"id":"5c9dc190418146010bc5f33d","text":"text3"},{"id":"5c9de1c0418146123e7b28ae","text":"33333"}]
```

### 测试更新

`PUT http://localhost:8080/texts/5c9dbf7c4181468603b08562`

得到 response

```json
{"id":"5c9dbf7c4181468603b08562","text":"mod"}
```

### 

## 结束

完毕