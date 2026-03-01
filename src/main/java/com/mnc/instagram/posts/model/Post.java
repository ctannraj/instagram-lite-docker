package com.mnc.instagram.posts.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class Post {
    private final String postId;
    private final String userId;
    private String text;
    private final Instant createdAt;

    public Post(String postId, String userId, String text, Instant createdAt) {
        this.postId = postId;
        this.userId = userId;
        this.text = text;
        this.createdAt = createdAt;
    }

}