package org.example.service;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;

class AuctionServiceLockTest {

    @Test
    void getLockReturnsSameLockForSameAuction() {
        AuctionService service = AuctionService.getInstance();

        ReentrantLock first = service.getLock(100);
        ReentrantLock second = service.getLock(100);
        ReentrantLock other = service.getLock(101);

        assertSame(first, second,
                "Cung auctionId phai dung chung mot lock de chong race condition");
        assertNotSame(first, other,
                "Khac auctionId phai co lock rieng de cac phien khong chan nhau");
    }

    @Test
    void lockProtectsCriticalSectionForSameAuction() throws Exception {
        AuctionService service = AuctionService.getInstance();
        ReentrantLock lock = service.getLock(200);
        AtomicInteger insideCriticalSection = new AtomicInteger(0);
        AtomicInteger maxInsideCriticalSection = new AtomicInteger(0);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Runnable task = () -> {
            ready.countDown();
            try {
                start.await(1, TimeUnit.SECONDS);
                lock.lock();
                try {
                    int current = insideCriticalSection.incrementAndGet();
                    maxInsideCriticalSection.updateAndGet(max -> Math.max(max, current));
                    Thread.sleep(50);
                    insideCriticalSection.decrementAndGet();
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        Thread first = new Thread(task, "bidder-1");
        Thread second = new Thread(task, "bidder-2");
        first.start();
        second.start();

        assertTrue(ready.await(1, TimeUnit.SECONDS));
        start.countDown();
        first.join(1000);
        second.join(1000);

        assertEquals(1, maxInsideCriticalSection.get(),
                "Cung mot phien thi chi mot bidder duoc vao vung cap nhat gia tai mot thoi diem");
    }
}
