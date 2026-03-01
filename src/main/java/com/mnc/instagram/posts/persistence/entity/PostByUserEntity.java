package com.mnc.instagram.posts.persistence.entity;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("posts_by_user")
public class PostByUserEntity {

    @PrimaryKey
    private PostByUserKey key;

    @Column("post_id")
    private String postId;

    @Column("text")
    private String text;

    public PostByUserEntity() {}

    public PostByUserEntity(PostByUserKey key, String postId, String text) {
        this.key = key;
        this.postId = postId;
        this.text = text;
    }

    public PostByUserKey getKey() {
        return key;
    }

    public void setKey(PostByUserKey key) {
        this.key = key;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}