package com.system.design.Week1;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class ConcurrentQueue {
    private static LinkedList<Integer> queue;
    private static int n = (int)1e6;

    private static synchronized void enque(int value){
        queue.add(value);
    }

    private static synchronized int deque(){
        if(queue.isEmpty()) {
            throw new RuntimeException("Queue is empty");
        }
        return queue.remove();
    }

    private static synchronized int size(){
        return queue.size();
    }
    public static void main(String[] args) throws InterruptedException {
        queue = new LinkedList<>();

        ExecutorService executorService = Executors.newFixedThreadPool(1000);
        List<Future<?>> futures = new ArrayList<>();

        for(int i = 0;i < n;i++){
            Future<?> future = executorService.submit(() -> enque(1));
            futures.add(future);
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                executorService.shutdownNow();
                throw new RuntimeException(e);
            }
        }

        futures = new ArrayList<>();

        for(int i = 0;i < n;i++){
            Future<?> future = executorService.submit(() -> deque());
            futures.add(future);
        }
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                executorService.shutdownNow();
                throw new RuntimeException(e);
            }
        }
        executorService.shutdown();

        System.out.println("Size of queue: " + size());
    }
}
