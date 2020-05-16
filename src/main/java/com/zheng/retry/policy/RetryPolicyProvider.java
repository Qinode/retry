package com.zheng.retry.policy;

public class RetryPolicyProvider {
    private RetryPolicyProvider() {}

    public final static RetryPolicy CONSTANT_POLICY = new ConstantPolicy();
    public final static RetryPolicy LINEAR_POLICY = new LinearPolicy();
    public final static RetryPolicy EXP_POLICY = new ExponentialPolicy();
}
