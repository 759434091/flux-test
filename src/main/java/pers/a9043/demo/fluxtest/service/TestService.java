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
