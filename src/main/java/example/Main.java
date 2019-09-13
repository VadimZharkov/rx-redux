package example;

import com.vzharkov.rx.redux.Action;
import com.vzharkov.rx.redux.Reducer;
import com.vzharkov.rx.redux.Store;
import io.reactivex.Observable;

class Actions {
    static final Action INCREMENT = new Action() {};
    static final Action DECREMENT = new Action() {};
    static final Action START_PROCESSING = new Action() {};
    static final Action STOP_PROCESSING = new Action() {};
    static final Action PROCESSING_ERROR = new Action() {};

    /**
     *  Action creator.
     */
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


/**
 *  Must be immutable.
 */
class State {
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

public class Main {
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

    public static void main(String[] args) throws InterruptedException {
        try (final var store = new Store<>(new State(0), reducer)) {
            store.subscribe(
                    state -> System.out.println(state),
                    error -> error.printStackTrace(),
                    () -> System.out.println("Done")
            );
            store.dispatch(Actions.INCREMENT);
            store.dispatch(Actions.DECREMENT);
            store.dispatch(Actions.PROCESS);
            Thread.sleep(2000);
        }
    }
}