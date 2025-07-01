package com.parkmate.batchservice.reviewsummary.infrastructure.reader;

import com.parkmate.batchservice.kafka.event.ReviewCreatedJoinUserEvent;
import com.parkmate.batchservice.reviewsummary.infrastructure.repository.ReviewChunkBuffer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewSummaryReader implements ItemReader<ReviewCreatedJoinUserEvent> {
    private final ReviewChunkBuffer reviewChunkBuffer;
    private Iterator<ReviewCreatedJoinUserEvent> iterator;

    @Override
    public ReviewCreatedJoinUserEvent read() {
        if (iterator == null || !iterator.hasNext()) {
            List<ReviewCreatedJoinUserEvent> events = reviewChunkBuffer.drain();

            // ✅ 버퍼가 비었을 경우도 명시적으로 empty iterator 할당
            if (events.isEmpty()) {
                iterator = List.<ReviewCreatedJoinUserEvent>of().iterator();
            } else {
                iterator = events.iterator();
            }
        }

        return iterator.hasNext() ? iterator.next() : null;
    }
}