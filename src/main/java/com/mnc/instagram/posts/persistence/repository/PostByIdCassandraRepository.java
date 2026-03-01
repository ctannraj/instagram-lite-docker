package com.mnc.instagram.posts.persistence.repository;

import com.mnc.instagram.posts.persistence.entity.PostByIdEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface PostByIdCassandraRepository
        extends CassandraRepository<PostByIdEntity, String> {
}