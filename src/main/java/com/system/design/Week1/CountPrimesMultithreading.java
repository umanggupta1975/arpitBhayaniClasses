package com.system.design.Week1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CountPrimesMultithreading {
    private static boolean isPrime(int num) {
        if (num <= 1) return false;

        for (int i = 2; i * i <= num; i++) {
            if (num % i == 0) {
                return false;
            }
        }
        return true;
    }

    private static int countPrime(int start, int end) {
        int count = 0;
        for (int i = start; i <= end; i++) {
            if (isPrime(i)) {
                count++;
            }
        }
        return count;
    }

    private static void countPrimesSingleThread(int n) {
        long start = System.currentTimeMillis();
        int count = 0;
        for (int i = 2; i <= n; i++) {
            if (isPrime(i)) {
                count++;
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("Found " + count + " primes till " + n + " in " + (end - start) + " ms");
    }

    private static void countPrimesMultiThread1(int n, int numThreads) throws InterruptedException {
        long startTime = System.currentTimeMillis();

        int[][] ranges = new int[numThreads][2];
        int start = 1;
        int batchSize = n / numThreads;
        for (int i = 0; i < numThreads; i++) {
            ranges[i][0] = start;

            ranges[i][1] = Math.min(start + batchSize, n);
            if (i == numThreads - 1) ranges[i][1] = n;

            start = ranges[i][1] + 1;
        }

        AtomicInteger count = new AtomicInteger();
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        List<Future<?>> futures = new ArrayList<>();

        for (int[] range : ranges) {
            Future<?> future = executorService.submit(() -> {
                count.addAndGet(countPrime(range[0], range[1]));
            });
            futures.add(future);
        }

        int i = 0;
        for (Future<?> future : futures) {
            try {
                future.get();
                int[] range = ranges[i++];
                long localEndTime = System.currentTimeMillis();
                System.out.println("Thread " + i + " completed processing primes in range " + range[0] + " to " + range[1] + " in " + (localEndTime - startTime) + " ms");
            } catch (ExecutionException e) {
                executorService.shutdownNow();
                throw new RuntimeException(e);
            }
        }
        executorService.shutdown();
        long endTime = System.currentTimeMillis();
        System.out.println("Found " + count + " primes till " + n + " in " + (endTime - startTime) + " ms");
    }

    private static AtomicInteger currentNum = new AtomicInteger(2);
    private static AtomicInteger primeCount = new AtomicInteger(0);
    private static void doWork(int total) {
        long startTime = System.currentTimeMillis();
        while(true){
            int num = currentNum.getAndIncrement();
            if(num > total){
                break;
            }
            if(isPrime(num)) primeCount.incrementAndGet();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Thread " + Thread.currentThread().getName() + " completed processing primes" + " in " + (endTime - startTime) + " ms");
    }
    private static void countPrimesMultiThread2(int n, int numThreads) throws InterruptedException {
        long startTime = System.currentTimeMillis();

        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 1;i <= numThreads;i++) {
            Future<?> future = executorService.submit(() -> {
                doWork(n);
            });
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
        long endTime = System.currentTimeMillis();
        System.out.println("Found " + primeCount + " primes till " + n + " in " + (endTime - startTime) + " ms");
    }

    public static void main(String[] args) throws InterruptedException {
        int n = (int)1e8;
        // n=1e8
        // primeCount=5761455
        // SingleThread -> 56433 ms
        // MultiThread1 -> 11110 ms
        // MultiThread2 -> 9868 ms


//        countPrimesSingleThread(n);
//        countPrimesMultiThread1(n, 10);
        countPrimesMultiThread2(n, 10);
    }
}
