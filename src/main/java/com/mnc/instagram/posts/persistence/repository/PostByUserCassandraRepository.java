package com.mnc.instagram.posts.persistence.repository;

import com.mnc.instagram.posts.persistence.entity.PostByUserEntity;
import com.mnc.instagram.posts.persistence.entity.PostByUserKey;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.List;

public interface PostByUserCassandraRepository
        extends CassandraRepository<PostByUserEntity, PostByUserKey> {

    List<PostByUserEntity> findByKeyUserId(String userId);
}