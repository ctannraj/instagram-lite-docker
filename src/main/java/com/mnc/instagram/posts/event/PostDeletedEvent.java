package com.mnc.instagram.posts.event;

import lombok.Getter;

import java.time.Instant;

@Getter
public class PostDeletedEvent implements PostEvent {

    private final String postId;
    private final String userId;
    private final Instant createdAt;

    public PostDeletedEvent(String postId, String userId, Instant createdAt) {
        this.postId = postId;
        this.userId = userId;
        this.createdAt = createdAt;
    }

}
