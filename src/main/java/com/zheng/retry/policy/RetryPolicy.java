package com.zheng.retry.policy;

import com.zheng.retry.Retry;

import java.util.concurrent.TimeUnit;

public interface RetryPolicy {
    void wait(int interval, TimeUnit intervalUnit, int currentAttempts);
}
