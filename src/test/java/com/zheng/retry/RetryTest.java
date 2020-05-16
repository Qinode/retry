package com.zheng.retry;

import com.zheng.retry.policy.RetryPolicyProvider;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

public class RetryTest {

    private String mockExceptionMessage = "Mock Exception";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void invalidTask() throws Exception {
        exception.expect(NullPointerException.class);
        Retry.<Void>submit(null)
                .interval(1, TimeUnit.SECONDS)
                .maxAttempts(1)
                .policy(RetryPolicyProvider.CONSTANT_POLICY)
                .start();
    }

    @Test
    public void invalidPolicy() throws Exception {
         exception.expect(NullPointerException.class);
         Retry.<Object>submit(() -> {return new Object();})
                .interval(1, TimeUnit.SECONDS)
                .maxAttempts(1)
                .policy(null)
                .start();
    }

    @Test
    public void invalidInterval() throws Exception{
        int invalid = -1;
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("[interval] must be non-negative. Current input: " + invalid);

        Retry.<Object>submit(() -> {return new Object();})
                .interval(invalid, TimeUnit.SECONDS)
                .maxAttempts(1)
                .policy(RetryPolicyProvider.CONSTANT_POLICY)
                .start();
    }

    @Test
    public void invalidAttempts() throws Exception {
        int invalid = 0;
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("[maxAttempts] must be greater than 0. Current input: " + invalid);

        Retry.<Object>submit(() -> {return new Object();})
                .interval(0, TimeUnit.SECONDS)
                .maxAttempts(invalid)
                .policy(RetryPolicyProvider.CONSTANT_POLICY)
                .start();
    }

    @Test
    public void successNoFailure() throws Exception{
        Integer result = Retry.<Integer>submit(() -> {return 1;})
                .interval(0, TimeUnit.SECONDS)
                .maxAttempts(1)
                .policy(RetryPolicyProvider.CONSTANT_POLICY)
                .start();

        Assert.assertEquals(1, (int) result);
    }

    @Test
    public void successWithFailure() throws Exception{
        Callable<Integer> task = mock(Callable.class);
        when(task.call())
                .thenThrow(new Exception(mockExceptionMessage))
                .thenReturn(1);

        Integer result = Retry.<Integer>submit(task)
                .interval(0, TimeUnit.SECONDS)
                .maxAttempts(2)
                .policy(RetryPolicyProvider.CONSTANT_POLICY)
                .start();

        verify(task, times(2)).call();
        Assert.assertEquals(1, (int) result);
    }

    @Test
    public void failOneAttempt() throws Exception{
        Callable<Integer> task = mock(Callable.class);
        when(task.call()).thenThrow(new Exception(mockExceptionMessage));

        try {
            Integer result = Retry.<Integer>submit(task)
                    .interval(0, TimeUnit.SECONDS)
                    .maxAttempts(1)
                    .policy(RetryPolicyProvider.CONSTANT_POLICY)
                    .start();
        } catch (Exception ex) { Assert.assertEquals(mockExceptionMessage, ex.getMessage()); }
        verify(task, times(1)).call();
    }

    @Test
    public void failMultipleAttempts1() throws Exception{
        Callable<Integer> task = mock(Callable.class);
        when(task.call())
                .thenThrow(new Exception(mockExceptionMessage))
                .thenThrow(new Exception(mockExceptionMessage))
                .thenThrow(new Exception(mockExceptionMessage))
                .thenThrow(new Exception(mockExceptionMessage));

        try{
            Integer result = Retry.<Integer>submit(task)
                    .interval(0, TimeUnit.SECONDS)
                    .maxAttempts(4)
                    .policy(RetryPolicyProvider.CONSTANT_POLICY)
                    .start();
        } catch (Exception ex) { Assert.assertEquals(mockExceptionMessage, ex.getMessage()); }
        verify(task, times(4)).call();
    }

    @Test
    public void failMultipleAttempts2() throws Exception {
        Callable<Integer> task = mock(Callable.class);
        when(task.call())
                .thenThrow(new Exception(mockExceptionMessage))
                .thenThrow(new Exception(mockExceptionMessage))
                .thenThrow(new Exception(mockExceptionMessage))
                .thenReturn(1);

        try {
            Integer result = Retry.<Integer>submit(task)
                    .interval(0, TimeUnit.SECONDS)
                    .maxAttempts(3) // The last successful mock function is not reachable
                    .policy(RetryPolicyProvider.CONSTANT_POLICY)
                    .start();
        } catch(Exception ex) { Assert.assertEquals(mockExceptionMessage, ex.getMessage()); }
        verify(task, times(3)).call();
    }

    @Test
    public void testFailWaiting() throws Exception{
        Callable<Integer> task = mock(Callable.class);
        when(task.call())
                .thenThrow(new Exception(mockExceptionMessage))
                .thenThrow(new Exception(mockExceptionMessage))
                .thenThrow(new Exception(mockExceptionMessage))
                .thenReturn(1);

        long begin = System.currentTimeMillis();
        try {
            Integer result = Retry.<Integer>submit(task)
                    .interval(1, TimeUnit.SECONDS)
                    .maxAttempts(3) // The last successful mock function is not reachable
                    .policy(RetryPolicyProvider.CONSTANT_POLICY)
                    .start();
        } catch (Exception e) { Assert.assertEquals(mockExceptionMessage, e.getMessage()); }
        long elapsed = TimeUnit.SECONDS.convert(System.currentTimeMillis() - begin, TimeUnit.MILLISECONDS);
        verify(task, times(3)).call();
        Assert.assertTrue(elapsed >= 3);
        Assert.assertTrue(elapsed <= 4);
    }

    @Test
    public void testSuccessWaiting() throws Exception{
        Callable<Integer> task = mock(Callable.class);
        when(task.call())
                .thenThrow(new Exception(mockExceptionMessage))
                .thenThrow(new Exception(mockExceptionMessage))
                .thenThrow(new Exception(mockExceptionMessage))
                .thenReturn(1);

        int maxAttempts = 4;
        int interval = 1; // second
        long begin = System.currentTimeMillis();
        Integer result = null;
        try {
            result = Retry.<Integer>submit(task)
                    .interval(interval, TimeUnit.SECONDS)
                    .maxAttempts(maxAttempts)
                    .policy(RetryPolicyProvider.CONSTANT_POLICY)
                    .start();
        } catch (Exception e) {}
        long elapsed = TimeUnit.SECONDS.convert(System.currentTimeMillis() - begin, TimeUnit.MILLISECONDS);
        verify(task, times(maxAttempts)).call();
        Assert.assertTrue(elapsed >= (maxAttempts - 1) * interval );
        Assert.assertTrue(elapsed <= maxAttempts * interval);
        Assert.assertEquals(1, result.intValue());
    }
}
