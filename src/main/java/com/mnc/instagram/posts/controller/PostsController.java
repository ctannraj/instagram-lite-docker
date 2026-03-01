package com.mnc.instagram.posts.controller;

import com.mnc.instagram.posts.api.CreatePostRequest;
import com.mnc.instagram.posts.api.UpdatePostRequest;
import com.mnc.instagram.posts.model.Post;
import com.mnc.instagram.posts.service.PostsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/posts")
public class PostsController {

    private final PostsService postsService;

    public PostsController(PostsService postsService) {
        this.postsService = postsService;
    }

    @PostMapping("")
    public ResponseEntity<Post> createPost(
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @Valid @RequestBody CreatePostRequest request) {

        Post created = postsService.createPost(
                userId,
                request.getText()
        );

        return ResponseEntity
                .created(URI.create("/posts/" + created.getPostId()))
                .body(created);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<Post> updatePost(
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @PathVariable String postId,
            @Valid @RequestBody UpdatePostRequest request) {

        Post updated = postsService.updatePost(postId, request.getText());

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @PathVariable String postId) {

        postsService.deletePost(postId);

        return ResponseEntity.noContent().build();
    }
}
