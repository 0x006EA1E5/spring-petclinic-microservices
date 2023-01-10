package org.springframework.boot.actuate.tracing;

import io.micrometer.context.ContextRegistry;
import io.micrometer.context.ContextSnapshot;
import io.micrometer.context.ThreadLocalAccessor;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import org.springframework.lang.Nullable;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Operators;
import reactor.util.context.Context;

import java.util.function.Function;

public class ObservationContextPropagationDecorator<T> implements CoreSubscriber<T> {
	public static Function<? super Publisher<Object>, ? extends Publisher<Object>> decortator() {
		return Operators.lift((scannable, coreSubscriber) -> new ObservationContextPropagationDecorator<>(coreSubscriber));
	}

    private final CoreSubscriber<? super T> delegate;

    @Nullable
    private final ObservationThreadLocalAccessor observationThreadLocalAccessor;

    private ObservationContextPropagationDecorator(CoreSubscriber<? super T> delegate) {
        this.delegate = delegate;
        observationThreadLocalAccessor = findObservationThreadLocalAccessor();
    }

    @Nullable
    private static ObservationThreadLocalAccessor findObservationThreadLocalAccessor() {
         for (ThreadLocalAccessor<?> threadLocalAccessor : ContextRegistry.getInstance().getThreadLocalAccessors()) {
             if (ObservationThreadLocalAccessor.KEY.equals(threadLocalAccessor.key())
                    && threadLocalAccessor instanceof ObservationThreadLocalAccessor observationThreadLocalAccessor) {
                 return observationThreadLocalAccessor;
             }
         }
         return null;
     }

    @Override
    public Context currentContext() {
        return delegate.currentContext();
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        delegate.onSubscribe(subscription);
    }

    @Override
    public void onNext(T t) {
        if (isObservationThreadLocalUnset()) {
            try (ContextSnapshot.Scope scope = ContextSnapshot.setThreadLocalsFrom(currentContext(), ObservationThreadLocalAccessor.KEY)) {
                delegate.onNext(t);
            }
        } else {
            delegate.onNext(t);
        }
    }

    @Override
    public void onError(Throwable t) {
        if (isObservationThreadLocalUnset()) {
            try (ContextSnapshot.Scope scope = ContextSnapshot.setThreadLocalsFrom(currentContext(), ObservationThreadLocalAccessor.KEY)) {
                delegate.onError(t);
            }
        } else {
            delegate.onError(t);
        }
    }

    @Override
    public void onComplete() {
        if (isObservationThreadLocalUnset()) {
            try (ContextSnapshot.Scope scope = ContextSnapshot.setThreadLocalsFrom(currentContext(), ObservationThreadLocalAccessor.KEY)) {
                delegate.onComplete();
            }
        } else {
            delegate.onComplete();
        }
    }

    private boolean isObservationThreadLocalUnset() {
        return observationThreadLocalAccessor != null && observationThreadLocalAccessor.getValue() == null;
    }
}
