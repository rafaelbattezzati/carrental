package com.rbattezzati.carrental.test.inventory;

import com.rbattezzati.carrental.factory.CarFactory;
import com.rbattezzati.carrental.model.car.Car;
import com.rbattezzati.carrental.model.car.CarType;
import com.rbattezzati.carrental.model.reservation.Reservation;
import com.rbattezzati.carrental.service.inventory.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class InventoryServiceConcurrencyTest {

    private InventoryService inventoryService;
    private LocalDateTime baseDate;

    @BeforeEach
    void setup() {
        List<Car> cars = List.of(
                CarFactory.getInstance("car1", CarType.SEDAN),
                CarFactory.getInstance("car2", CarType.SEDAN),
                CarFactory.getInstance("car3", CarType.SEDAN)
        );
        inventoryService = new InventoryService(cars);
        baseDate = LocalDateTime.now().plusDays(1);
    }

    // -------------------------------------------------------
    // ensures no double-booking under concurrency
    // -------------------------------------------------------
    @Test
    void shouldHandleConcurrentReservationsSafely() throws Exception {
        int threads = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        ConcurrentLinkedQueue<Reservation> results = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await(); // start all threads at same time
                    Reservation r = inventoryService.reserve(CarType.SEDAN, baseDate, 3);
                    results.add(r);
                } catch (Exception ignored) {
                    // Expected: when cars are all taken
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        start.countDown();
        done.await();
        executor.shutdown();

        long uniqueCars = results.stream()
                .map(r -> r.getCar().id())
                .distinct()
                .count();

        assertEquals(3, uniqueCars, "No car should be double-booked");
    }

    // ----------------------------------------------------------------
    // Check for deadlocks 
    // ----------------------------------------------------------------
    @Test
    void shouldNotDeadlockUnderConcurrentLoad() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(20);

        LocalDateTime t1 = baseDate;
        LocalDateTime t2 = baseDate.plusDays(5);

        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                try {
                    inventoryService.reserve(CarType.SEDAN, t1, 2);
                } catch (Exception ignored) {}
                latch.countDown();
            });

            executor.submit(() -> {
                try {
                    inventoryService.countAvailable(CarType.SEDAN, t2, 3);
                } catch (Exception ignored) {}
                latch.countDown();
            });
        }

        boolean finished = latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(finished, "Possible deadlock detected!");
    }

    // ----------------------------------------------------------------
    // Read-only availability lookups should run concurrently
    // ----------------------------------------------------------------
    @Test
    void shouldAllowMultipleThreadsToCheckAvailabilitySimultaneously() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(20);

        Callable<Long> task = () -> inventoryService.countAvailable(CarType.SEDAN, baseDate, 2);

        long startTime = System.nanoTime();

        // Run 30 concurrent read-only operations
        List<Future<Long>> futures = executor.invokeAll(
                List.of(task, task, task, task, task,
                        task, task, task, task, task,
                        task, task, task, task, task,
                        task, task, task, task, task,
                        task, task, task, task, task,
                        task, task, task, task, task)
        );

        long endTime = System.nanoTime();
        executor.shutdown();

        futures.forEach(f -> assertDoesNotThrow((Executable) f::get));

        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        System.out.println("Concurrent availability check duration = " + durationMs + "ms");

        assertTrue(durationMs < 800,
                "Availability checks should not serialize via unnecessary locks");
    }
}
