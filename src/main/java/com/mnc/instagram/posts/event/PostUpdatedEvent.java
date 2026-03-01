package com.mnc.instagram.posts.event;

import com.mnc.instagram.posts.model.Post;
import lombok.Getter;

@Getter
public class PostUpdatedEvent implements PostEvent {

    private final Post post;

    public PostUpdatedEvent(Post post) {
        this.post = post;
    }

}
