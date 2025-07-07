package com.parkmate.batchservice.kafka.buffer;

import com.parkmate.batchservice.kafka.event.ReservationEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Kafka에서 소비한 예약 이벤트를 임시 저장하는 버퍼
 * - Reader가 데이터를 소비할 때 사용됨
 * - 월/일 정산의 이벤트 조회 기준이 됨
 */
@Component
public class ReservationChunkBuffer {

    // 내부 이벤트 큐 (스레드 안전한 비블로킹 큐)
    private final Queue<ReservationEvent> buffer = new ConcurrentLinkedQueue<>();

    /**
     * 예약 이벤트를 버퍼에 추가합니다.
     *
     * @param event Kafka에서 수신한 예약 이벤트
     */
    public void add(ReservationEvent event) {
        buffer.offer(event);
    }

    /**
     * 예약 이벤트를 하나 꺼내고 제거합니다.
     * - 일반적으로 단건 처리용으로는 사용하지 않음
     *
     * @return ReservationEvent 또는 null
     */
    public ReservationEvent poll() {
        return buffer.poll();
    }

    /**
     * 버퍼에 있는 모든 예약 이벤트를 조회합니다.
     * - 버퍼는 그대로 유지됩니다.
     * - 주로 월 정산 등 조회 전용 처리에 사용합니다.
     *
     * @return 현재 버퍼에 있는 이벤트 목록
     */
    public List<ReservationEvent> peekAll() {
        return new ArrayList<>(buffer); // 얕은 복사
    }

    /**
     * 버퍼를 비우며 모든 이벤트를 꺼내 반환합니다.
     * - 주로 일 정산 등 소비성 처리에 사용합니다.
     *
     * @return 비운 이벤트 목록
     */
    public List<ReservationEvent> flush() {
        List<ReservationEvent> flushed = new LinkedList<>();
        ReservationEvent event;
        while ((event = buffer.poll()) != null) {
            flushed.add(event);
        }
        return flushed;
    }

    /**
     * 버퍼가 비어 있는지 여부를 반환합니다.
     */
    public boolean isEmpty() {
        return buffer.isEmpty();
    }

    /**
     * 현재 버퍼에 쌓인 이벤트 수를 반환합니다.
     */
    public int size() {
        return buffer.size();
    }
}