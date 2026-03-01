package com.mnc.instagram.posts.infrastructure.messaging;

import com.mnc.instagram.posts.event.PostEvent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class KafkaBroker {
    public static final BlockingQueue<PostEvent> POSTS_EVENTS_TOPIC = new LinkedBlockingQueue<>();
}