package com.zheng.retry.policy;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class ExpPolicyTest {
    private TimeUnit intervalUnit = TimeUnit.SECONDS;
    private int interval = 1;
    private double epsilon = 0.05;

    private RetryPolicy policy;

    @Before
    public void initPolicy() {
        policy = new ExponentialPolicy();
    }

    @Test
    public void test(){
        int waitTimes = 5;
        long firstWaitInterval = -1;
        for(int i = 0; i < waitTimes; i++){
            long begin = System.currentTimeMillis();
            policy.wait(interval, intervalUnit, i+1);
            long elapsed = System.currentTimeMillis() - begin;
            firstWaitInterval = firstWaitInterval == -1 ? elapsed : firstWaitInterval;
            double rate = elapsed/(double)firstWaitInterval;
            double expectedRate = Math.pow(2.0, i);
            Assert.assertTrue(Math.abs( expectedRate - rate) / expectedRate < epsilon);
        }
    }
}
