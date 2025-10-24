package com.aigreentick.services.template.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.aigreentick.services.template.model.resumablemedia.MediaResumable;




@Repository
public interface ResumableMediaRepository extends MongoRepository<MediaResumable,Long>{
    
}
