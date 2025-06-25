package com.parkmate.batchservice.reviewsummary.infrastructure.repository;

import com.parkmate.batchservice.kafka.event.ReviewCreatedJoinUserEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Repository
public class ReviewChunkBuffer {

    private final List<ReviewCreatedJoinUserEvent> buffer = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();

    public void add(ReviewCreatedJoinUserEvent event) {
        lock.lock();
        try {
            buffer.add(event);
            log.debug("[ChunkBuffer] Event added: {}", event);
        } finally {
            lock.unlock();
        }
    }

    public List<ReviewCreatedJoinUserEvent> drain() {
        lock.lock();
        try {
            if (buffer.isEmpty()) {
                return Collections.emptyList();
            }
            List<ReviewCreatedJoinUserEvent> drained = new ArrayList<>(buffer);
            buffer.clear();
            log.info("[ChunkBuffer] Drained {} events", drained.size());
            return drained;
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return buffer.size();
        } finally {
            lock.unlock();
        }
    }
}