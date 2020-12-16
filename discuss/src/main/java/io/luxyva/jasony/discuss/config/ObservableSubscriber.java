package io.luxyva.jasony.discuss.config;

import com.mongodb.MongoTimeoutException;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ObservableSubscriber<T> implements Subscriber<T> {
    
    private final List<T> received;
    
    private final List<Throwable> errors;
    
    private final CountDownLatch latch;
    
    private volatile Subscription subscription;
    
    private volatile boolean completed;
    
    public ObservableSubscriber() {
        this.received = new ArrayList<>();
        this.errors = new ArrayList<>();
        this.latch = new CountDownLatch(1);
    }
    
    @Override
    public void onSubscribe(Subscription s) {
        subscription = s;
    }
    
    @Override
    public void onNext(final T o) {
        received.add(o);
    }
    
    @Override
    public void onError(Throwable throwable) {
        errors.add(throwable);
        onComplete();
    }
    
    @Override
    public void onComplete() {
        completed = true;
        latch.countDown();
    }
    
    public List<T> getReceived() {
        return received;
    }
    
    public List<Throwable> getErrors() {
        return errors;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public List<T> get(final long timeout, final TimeUnit unit) throws Throwable {
        return await(timeout, unit).getReceived();
    }
    
    public ObservableSubscriber await() throws Throwable {
        return await(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }
    
    public ObservableSubscriber await(final long timeout, final TimeUnit unit) throws Throwable {
        subscription.request(Integer.MAX_VALUE);
        if (!latch.await(timeout, unit)) {
            throw new MongoTimeoutException("Publisher onComplete timed out");
        }
        if (!errors.isEmpty()) {
            throw errors.get(0);
        }
        return this;
    }
}
