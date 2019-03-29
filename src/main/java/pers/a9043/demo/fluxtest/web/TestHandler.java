package pers.a9043.demo.fluxtest.web;

import javax.annotation.Resource;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
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
     * 获取一条
     */
    @SuppressWarnings("unused")
    public Mono<ServerResponse> get2(ServerRequest serverRequest) {
        return Mono
                .just(serverRequest.pathVariable("id"))
                .map(ObjectId::new)
                .map(testService::get)
                .flatMap(textMono -> ServerResponse.ok().body(textMono, Text.class));
    }

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
