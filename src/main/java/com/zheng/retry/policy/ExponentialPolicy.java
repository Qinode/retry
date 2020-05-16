package com.zheng.retry.policy;

import java.util.concurrent.TimeUnit;

public class ExponentialPolicy implements RetryPolicy{
    @Override
    public void wait(int interval, TimeUnit intervalUnit, int currentAttempts) {
        try { Thread.sleep((long)Math.pow(2, currentAttempts - 1) * TimeUnit.MILLISECONDS.convert(interval, intervalUnit)); }
        catch (InterruptedException e) { }    }
}
