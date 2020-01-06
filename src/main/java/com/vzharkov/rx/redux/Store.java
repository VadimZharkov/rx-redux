package com.vzharkov.rx.redux;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The Store is the object that brings state, actions and reducer together.
 *
 * The store has the following responsibilities:
 * holds application state;
 * allows access to state;
 * allows state to be updated via dispatch(action);
 * registers, unregister listeners via Observable<State>;
 *
 * @param <State> State type. Must be immutable.
 */
public class Store<State> implements AutoCloseable {
    private final PublishSubject<State> subject;
    private final Observable<State> changes;
    private final Reducer<State> reducer;
    private final Dispatcher dispatcher;

    private volatile State state;

    public Store(@NonNull State state, @NonNull Reducer<State> reducer, List<Middleware<State>> middlewares) {
        this.subject = PublishSubject.create();
        this.changes = subject.hide();
        this.state = state;
        this.reducer = reducer;
        this.dispatcher = applyMiddleware(middlewares);
    }

    public void dispatch(@NonNull Action action) {
        dispatcher.dispatch(action);
    }

    /**
     * @param actionCreator Observable<Action> is an action creator.
     */
    public void dispatch(@NonNull Observable<Action> actionCreator) {
        actionCreator.subscribe(this::dispatch);
    }

    @NonNull
    public State getState() {
        return state;
    }

    public final Disposable subscribe(@NonNull Consumer<State> onNext) {
        return changes.subscribe(onNext);
    }

    public final Disposable subscribe(@NonNull Consumer<State> onNext, @NonNull Consumer<? super Throwable> onError) {
        return changes.subscribe(onNext, onError);
    }

    public final Disposable subscribe(@NonNull Consumer<State> onNext, @NonNull Consumer<? super Throwable> onError, @NonNull io.reactivex.functions.Action onComplete) {
        return changes.subscribe(onNext, onError, onComplete);
    }

    @Override
    public void close() {
        subject.onComplete();
    }

    /**
     * Middleware is applied in the order in which it is passed into method.
     * @param middlewares List of Middleware<State>
     * @return Dispatcher
     */
    @NonNull
    private Dispatcher applyMiddleware(List<Middleware<State>> middlewares) {
        if (middlewares == null) {
            return this::internalDispatch;
        }

        Collections.reverse(middlewares);

        return middlewares
                .stream()
                .reduce(this::internalDispatch,
                        (dispatcher, middleware) -> middleware.apply(this::dispatch, this::getState).apply(dispatcher),
                        Dispatcher::combiner);
    }
    
    private synchronized void internalDispatch(@NonNull Action action) {
        state = reducer.reduce(getState(), action);
        subject.onNext(getState());
    }
}
