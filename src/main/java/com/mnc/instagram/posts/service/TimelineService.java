package com.mnc.instagram.posts.service;

import com.mnc.instagram.posts.model.Post;
import com.mnc.instagram.posts.persistence.repository.PostByUserCassandraRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TimelineService {

    private final PostByUserCassandraRepository postByUserCassandraRepository;

    public TimelineService(PostByUserCassandraRepository postByUserCassandraRepository) {
        this.postByUserCassandraRepository = postByUserCassandraRepository;
    }

    public List<Post> getTimeline(String userId) {
        return postByUserCassandraRepository.findByKeyUserId(userId)
                .stream()
                .map(e -> new Post(
                        e.getPostId(),
                        e.getKey().getUserId(),
                        e.getText(),
                        e.getKey().getCreatedAt()
                ))
                .toList();
    }
}
