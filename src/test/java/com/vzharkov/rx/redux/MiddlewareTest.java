package com.vzharkov.rx.redux;

import org.junit.jupiter.api.Test;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class MiddlewareTest {
    static class StringState {
        private final String value;

        public StringState(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    static class SetStringAction implements Action {
        private String value;

        public SetStringAction(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    static final Middleware<StringState> firstMiddleware = (dispatcher, getState) -> (next) -> (action) -> {
        var setStringAction = (SetStringAction) action;
        setStringAction.setValue(setStringAction.getValue() + " First Middleware");
        next.dispatch(setStringAction);
    };

    static final Middleware<StringState> secondMiddleware = (dispatcher, getState) -> (next) -> (action) -> {
        var setStringAction = (SetStringAction) action;
        setStringAction.setValue(setStringAction.getValue() + " Second Middleware");
        next.dispatch(setStringAction);
    };

    static final Reducer<StringState> reducer = (oldState, action) -> {
        StringState newState = null;

        if(action instanceof SetStringAction) {
            var setStringAction = (SetStringAction) action;
            newState = new StringState(setStringAction.getValue());
        }

        return newState;
    };

    @Test
    void middlewaresShouldBeInvokedBeforeStoreDispatch() {
        final var middlewares = Arrays.asList(firstMiddleware, secondMiddleware);
        final var store = new Store<StringState>(new StringState("Initial"), reducer, middlewares);
        store.subscribe(state -> {});
        store.dispatch(new SetStringAction("OK"));

        assertEquals("OK First Middleware Second Middleware", store.getState().getValue());
    }
}