package com.mnc.instagram.posts.persistence.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;

@Getter
@Setter
@Table("posts_by_id")
public class PostByIdEntity {

    @PrimaryKey("post_id")
    private String postId;
    @Column("user_id")
    private String userId;
    @Column("text")
    private String text;
    @Column("created_at")
    private Instant createdAt;

    public PostByIdEntity() {}

    public PostByIdEntity(String postId, String userId, String text, Instant createdAt) {
        this.postId = postId;
        this.userId = userId;
        this.text = text;
        this.createdAt = createdAt;
    }

}