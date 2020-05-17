# retry

A simple retry template.

```java
try {
    Integer result = Retry.<Integer>submit(() -> {return 1;})
            .interval(1, TimeUnit.SECONDS)
            .maxAttempts(1)
            .policy(RetryPolicyProvider.CONSTANT_POLICY)
            .start();
} catch (Exception ex) { // this is the exception of the last attempt }
```
