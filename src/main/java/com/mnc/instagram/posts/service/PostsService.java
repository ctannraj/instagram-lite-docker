package com.mnc.instagram.posts.service;

import com.mnc.instagram.posts.event.PostCreatedEvent;
import com.mnc.instagram.posts.event.PostDeletedEvent;
import com.mnc.instagram.posts.event.PostEvent;
import com.mnc.instagram.posts.event.PostUpdatedEvent;
import com.mnc.instagram.posts.exception.PostNotFoundException;
import com.mnc.instagram.posts.infrastructure.messaging.KafkaBroker;
import com.mnc.instagram.posts.model.Post;
import com.mnc.instagram.posts.persistence.entity.PostByIdEntity;
import com.mnc.instagram.posts.persistence.repository.PostByIdCassandraRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
public class PostsService {

    private final PostByIdCassandraRepository postByIdCassandraRepository;

    public PostsService(PostByIdCassandraRepository postByIdCassandraRepository) {
        this.postByIdCassandraRepository = postByIdCassandraRepository;
    }

    private void postToTopic(PostEvent postEvent) {
        KafkaBroker.POSTS_EVENTS_TOPIC.offer(postEvent);
    }

    public Post createPost(String userId, String text) {
        Post post = new Post(
                UUID.randomUUID().toString().split("-")[0],
                userId,
                text,
                Instant.now()
        );

        PostByIdEntity entity =
                new PostByIdEntity(post.getPostId(), userId, text, post.getCreatedAt());

        postByIdCassandraRepository.save(entity);

        log.info("Saved post {} to Cassandra", post.getPostId());

        postToTopic(new PostCreatedEvent(post));

        return post;
    }

    public Post updatePost(String postId, String newText) {

        PostByIdEntity entity = postByIdCassandraRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found"));

        entity.setText(newText);

        postByIdCassandraRepository.save(entity);

        Post existing = new Post(
                entity.getPostId(),
                entity.getUserId(),
                entity.getText(),
                entity.getCreatedAt()
        );

        postToTopic(new PostUpdatedEvent(existing));

        return existing;
    }

    public void deletePost(String postId) {

        PostByIdEntity existing = postByIdCassandraRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found"));

        postByIdCassandraRepository.deleteById(postId);

        postToTopic(new PostDeletedEvent(existing.getPostId(), existing.getUserId(), existing.getCreatedAt()));
    }
}
