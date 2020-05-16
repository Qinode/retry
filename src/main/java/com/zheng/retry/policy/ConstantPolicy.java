package com.zheng.retry.policy;

import com.zheng.retry.Retry;

import java.util.concurrent.TimeUnit;

public class ConstantPolicy implements RetryPolicy{
    @Override
    public void wait(int interval, TimeUnit intervalUnit, int currentAttempts) {
        try{ Thread.sleep(TimeUnit.MILLISECONDS.convert(interval, intervalUnit)); }
        catch (Exception ignored){};
    }
}
