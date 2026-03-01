package com.mnc.instagram.posts.persistence.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.io.Serializable;
import java.time.Instant;

@Setter
@Getter
@PrimaryKeyClass
public class PostByUserKey implements Serializable {

    // getters & setters
    @PrimaryKeyColumn(name = "user_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String userId;

    @PrimaryKeyColumn(name = "created_at", ordinal = 1, ordering = Ordering.DESCENDING)
    private Instant createdAt;

    public PostByUserKey() {}

    public PostByUserKey(String userId, Instant createdAt) {
        this.userId = userId;
        this.createdAt = createdAt;
    }

}