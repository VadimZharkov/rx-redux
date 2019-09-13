package com.vzharkov.rx.redux;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;

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
    private final AtomicReference<State> state;
    private final Reducer<State> reducer;

    public Store(@NonNull State state, @NonNull Reducer<State> reducer) {
        this.subject = PublishSubject.create();
        this.changes = subject.hide();
        this.state = new AtomicReference<>(state);
        this.reducer = reducer;
    }

    public void dispatch(@NonNull Action action) {
        final var wrapper = new Object() { State next = null; };
        state.updateAndGet(prev -> {
            if (wrapper.next == null) {
                wrapper.next = reducer.reduce(prev, action);
            }
            return wrapper.next;
        });
        subject.onNext(wrapper.next);
    }

    /**
     * @param actionCreator Observable<Action> is an action creator.
     */
    public void dispatch(@NonNull Observable<Action> actionCreator) {
        actionCreator.subscribe(this::dispatch);
    }

    @NonNull
    public State state() {
        return state.get();
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
}
