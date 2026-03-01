package com.mnc.instagram.posts.controller;

import com.mnc.instagram.posts.model.Post;
import com.mnc.instagram.posts.service.TimelineService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UsersController {

    private final TimelineService timelineService;

    public UsersController(TimelineService timelineService) {
        this.timelineService = timelineService;
    }

    @GetMapping("/{userId}/timeline")
    public List<Post> getTimeline(@PathVariable String userId) {
        return timelineService.getTimeline(userId);
    }
}