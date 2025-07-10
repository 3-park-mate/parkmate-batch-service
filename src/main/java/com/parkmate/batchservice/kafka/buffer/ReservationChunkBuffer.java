package com.parkmate.batchservice.kafka.buffer;

import com.parkmate.batchservice.kafka.event.ReservationEvent;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class ReservationChunkBuffer {

    private final Queue<ReservationEvent> buffer = new ConcurrentLinkedQueue<>();

    public void add(ReservationEvent event) {
        buffer.offer(event);
    }

    public ReservationEvent poll() {
        return buffer.poll();
    }

    public List<ReservationEvent> peekAll() {
        return new ArrayList<>(buffer);
    }

    public List<ReservationEvent> flush() {
        List<ReservationEvent> flushed = new LinkedList<>();
        ReservationEvent event;
        while ((event = buffer.poll()) != null) {
            flushed.add(event);
        }
        return flushed;
    }

    public boolean isEmpty() {
        return buffer.isEmpty();
    }

    public int size() {
        return buffer.size();
    }
}