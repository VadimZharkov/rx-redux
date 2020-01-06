package com.vzharkov.rx.redux;

/**
 * Dispatcher represents store dispatch function.
 */
@FunctionalInterface
public interface Dispatcher {
    void dispatch(Action action);

    /**
    * Combiner stub.
    * Does nothing.
    */
    static Dispatcher combiner(Dispatcher a, Dispatcher b) {
        return (action) -> {};
    }
}
