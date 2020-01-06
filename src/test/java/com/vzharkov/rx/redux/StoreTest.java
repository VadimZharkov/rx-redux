package com.vzharkov.rx.redux;

import io.reactivex.Observable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StoreTest {
    static class Actions {
        static final Action INCREMENT = new Action() {};
        static final Action DECREMENT = new Action() {};
        static final Action START_PROCESSING = new Action() {};
        static final Action STOP_PROCESSING = new Action() {};
        static final Action PROCESSING_ERROR = new Action() {};

        static final Observable<Action> PROCESS = Observable.create(emitter -> {
            var thread = new Thread(() -> {
                emitter.onNext(Actions.START_PROCESSING);
                try {
                    Thread.sleep(1000);
                    emitter.onNext(Actions.STOP_PROCESSING);
                    emitter.onComplete();
                  } catch (InterruptedException e) {
                    e.printStackTrace();
                    emitter.onNext(Actions.PROCESSING_ERROR);
                    emitter.onComplete();
                }
            });
            thread.start();
        });
    }

    static final Reducer<State> reducer = (oldState, action) -> {
        State newState = null;
        if (action == Actions.INCREMENT) {
            newState = new State(oldState.counter() + 1);
        } else if (action == Actions.DECREMENT) {
            newState = new State(oldState.counter() - 1);
        } else if (action == Actions.START_PROCESSING) {
            newState = new State(oldState.counter(), true);
        } else if (action == Actions.STOP_PROCESSING) {
            newState = new State(100, false);
        }
        return newState;
    };

    static class State {
        private int counter = 0;
        private boolean inProcess = false;

        State(int counter, boolean inProcess) {
            this.counter = counter;
            this.inProcess = inProcess;
        }

        State(int counter) {
            this.counter = counter;
            this.inProcess = false;
        }

        int counter() {
            return counter;
        }

        boolean inProcess() {
            return inProcess;
        }

        @Override
        public String toString() {
            return "Counter: " + counter + ", in the process: " + inProcess;
        }
    }

    @Test
    void whenAnActionIsFiredReducerShouldUpdateTheState() {
        final var store = new Store<>(new State(0), reducer, null);
        store.dispatch(Actions.INCREMENT);

        assertEquals(1, store.getState().counter());
    }

    @Test
    void subscribersShouldBeNotifiedWhenTheStateWasChanged() {
        final var wrapper = new Object() { int counter = 0; };
        final var store = new Store<>(new State(0), reducer, null);
        store.subscribe(state -> wrapper.counter = state.counter());
        store.dispatch(Actions.INCREMENT);

        assertEquals(1, wrapper.counter);
    }

    @Test
    void actionCreatorShouldFireTwoActions() {
        final var wrapper = new Object() { int count = 0; };
        final var store = new Store<>(new State(0), reducer, null);
        store.subscribe(state -> wrapper.count++);
        store.dispatch(Actions.PROCESS);

        sleep(2000);

        assertEquals(2, wrapper.count);
    }

    private void sleep(long m) {
        try { Thread.sleep(m); } catch (Throwable ignored) {}
    }
}