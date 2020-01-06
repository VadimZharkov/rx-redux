package com.vzharkov.rx.redux;

import java.util.function.Function;
import java.util.function.Supplier;

/**
* Middleware provides a third-party extension point between dispatching an action, and the moment it reaches the reducer.
*/
@FunctionalInterface
public interface Middleware<State> {
    Function<Dispatcher, Dispatcher> apply(Dispatcher dispatcher, Supplier<State> getState);
}
