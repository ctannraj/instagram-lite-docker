package com.mnc.instagram.posts.infrastructure.messaging;

import com.mnc.instagram.posts.event.PostCreatedEvent;
import com.mnc.instagram.posts.event.PostDeletedEvent;
import com.mnc.instagram.posts.event.PostEvent;
import com.mnc.instagram.posts.event.PostUpdatedEvent;
import com.mnc.instagram.posts.model.Post;
import com.mnc.instagram.posts.persistence.entity.PostByUserEntity;
import com.mnc.instagram.posts.persistence.entity.PostByUserKey;
import com.mnc.instagram.posts.persistence.repository.PostByUserCassandraRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PostsKafkaConsumer {

    private final PostByUserCassandraRepository postByUserCassandraRepository;

    public PostsKafkaConsumer(PostByUserCassandraRepository postByUserCassandraRepository) {
        this.postByUserCassandraRepository = postByUserCassandraRepository;
        start();
    }

    private void start() {
        new Thread(() -> {
            while (true) {
                try {
                    PostEvent postEvent = KafkaBroker.POSTS_EVENTS_TOPIC.take();
                    if (postEvent instanceof PostCreatedEvent created) {
                        Post post = created.getPost();
                        log.info("Consuming PostCreatedEvent, id={}", post.getPostId());
                        PostByUserKey key =
                                new PostByUserKey(post.getUserId(), post.getCreatedAt());

                        PostByUserEntity entity =
                                new PostByUserEntity(key, post.getPostId(), post.getText());

                        postByUserCassandraRepository.save(entity);
                    }

                    if (postEvent instanceof PostUpdatedEvent updated) {
                        Post post = updated.getPost();
                        log.info("Consuming PostUpdatedEvent, id={}", updated.getPost().getPostId());
                        PostByUserKey key =
                                new PostByUserKey(post.getUserId(), post.getCreatedAt());

                        PostByUserEntity entity =
                                new PostByUserEntity(key, post.getPostId(), post.getText());

                        postByUserCassandraRepository.save(entity);
                    }

                    if (postEvent instanceof PostDeletedEvent deleted) {
                        log.info("Consuming PostDeletedEvent, id={}", deleted.getPostId());
                        PostByUserKey key =
                                new PostByUserKey(deleted.getUserId(), deleted.getCreatedAt());

                        postByUserCassandraRepository.deleteById(key);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }
}
