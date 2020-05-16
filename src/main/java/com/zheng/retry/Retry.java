package com.zheng.retry;

import com.zheng.retry.policy.RetryPolicy;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class Retry<T>{
    private int interval;
    private TimeUnit intervalUnit;

    private int maxAttempts;
    private RetryPolicy policy;

    private Callable<T> task;

    private Retry(Callable<T> task, int interval, TimeUnit intervalUnit, int maxAttempts, RetryPolicy policy){
        this.task = task;
        this.interval = interval;
        this.intervalUnit = intervalUnit;
        this.maxAttempts = maxAttempts;
        this.policy = policy;
    }

    private T start() throws Exception {
        int currentAttempt = 0;
        Exception lastError = null;
        T result = null;
        boolean isFinish = false;
        do{
            try{
                result = task.call();
                isFinish = true;
            }catch (Exception e){
                lastError = e;
                policy.wait(interval, intervalUnit, ++currentAttempt);
            }
        }while(currentAttempt < maxAttempts && !isFinish);

        if(isFinish) return result;
        throw lastError;
    }

    public static <T> IntervalStage<T> submit(Callable<T> task){
        return new RetryBuilder<T>(task);
    }

    public interface FinalBuilder<T> { T start() throws Exception; }
    public interface PolicyStage<T> { FinalBuilder<T> policy(RetryPolicy p); }
    public interface MaxAttemptsStage<T> { PolicyStage<T> maxAttempts(int attempts); };
    public interface IntervalStage<T> { MaxAttemptsStage<T> interval(int interval, TimeUnit unit); }

    public static final class RetryBuilder<T> implements IntervalStage<T>, MaxAttemptsStage<T>, PolicyStage<T>,FinalBuilder<T>{
        private Callable<T> task;
        private int interval;
        private TimeUnit intervalUnit;

        private int maxAttempts;
        private RetryPolicy policy;

        private RetryBuilder(Callable<T> task) {
            Objects.requireNonNull(task);
            this.task = task;
        }

        public PolicyStage<T> maxAttempts(int attempts) {
            if(attempts < 1) throw new IllegalArgumentException("[maxAttempts] must be greater than 0. Current input: " + attempts);
            this.maxAttempts = attempts;
            return this;
        }

        public MaxAttemptsStage<T> interval(int interval, TimeUnit unit){
            if(interval < 0) throw new IllegalArgumentException("[interval] must be non-negative. Current input: " + interval);
            this.interval = interval;
            this.intervalUnit= unit;
            return this;
        }

        public FinalBuilder<T> policy(RetryPolicy policy) {
            Objects.requireNonNull(policy);
            this.policy = policy;
            return this;
        }

        public T start() throws Exception{
            return new Retry<T>(this.task, this.interval, this.intervalUnit, this.maxAttempts, this.policy).start();
        }
    }
}
