package com.mnc.instagram.posts.event;

import com.mnc.instagram.posts.model.Post;
import lombok.Getter;

@Getter
public class PostCreatedEvent implements PostEvent {

    private final Post post;

    public PostCreatedEvent(Post post) {
        this.post = post;
    }

}
