package com.zheng.retry.policy;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class ConstantPolicyTest {
    private TimeUnit intervalUnit = TimeUnit.SECONDS;
    private int interval = 1;
    private int expectedRate = 1; // rate of wait times between different attempts
    private double epsilon = 0.01;

    private RetryPolicy policy;

    @Before
    public void initPolicy() {
        policy = new ConstantPolicy();
    }

    @Test
    public void test() {
        int waitTimes = 5;
        long firstWaitInterval = -1;
        for (int i = 0; i < waitTimes; i++) {
            long begin = System.currentTimeMillis();
            policy.wait(interval, intervalUnit, i + 1);
            long elapsed = System.currentTimeMillis() - begin;
            firstWaitInterval = firstWaitInterval == -1 ? elapsed : firstWaitInterval;
            double rate = elapsed / (double) firstWaitInterval;
            Assert.assertTrue(Math.abs(expectedRate - rate) / expectedRate < epsilon);
        }
    }


}
