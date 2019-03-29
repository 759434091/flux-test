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
