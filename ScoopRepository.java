package com.scoopsoup.repository;

import com.scoopsoup.model.Scoops;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ScoopRepository extends MongoRepository<Scoops,String> {
    Scoops findBy_id(ObjectId _id);
}
