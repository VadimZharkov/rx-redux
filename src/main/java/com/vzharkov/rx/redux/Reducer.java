package com.vzharkov.rx.redux;

/**
 * Reducers specify how the state changes in response to actions sent to the store.
 */
@FunctionalInterface
public interface Reducer<State> {
    State reduce(State state, Action action);
}
