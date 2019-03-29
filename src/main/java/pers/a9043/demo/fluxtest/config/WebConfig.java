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
