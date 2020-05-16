package com.zheng.retry.policy;

import java.util.concurrent.TimeUnit;

public class LinearPolicy implements RetryPolicy{
    @Override
    public void wait(int interval, TimeUnit intervalUnit, int currentAttempts) {
        try { Thread.sleep(currentAttempts * TimeUnit.MILLISECONDS.convert(interval, intervalUnit)); }
        catch (InterruptedException e) { }
    }
}
