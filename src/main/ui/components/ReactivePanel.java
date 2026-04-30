package main.ui.components;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;
import javax.swing.JPanel;
import main.store.DataChangeBus;

/**
 * Base panel that automatically unsubscribes from shared data events when it
 * is removed from the component hierarchy.
 */
public abstract class ReactivePanel extends JPanel {

    private final List<DataChangeBus.Subscription> subscriptions =  new ArrayList<>();
    private final List<Timer> timers = new ArrayList<>();

    protected final void listen(String topic, Runnable listener) {
        subscriptions.add(DataChangeBus.subscribe(topic, listener));
    }

    protected final Timer pollEvery(int delayMillis, Runnable action) {
        Timer timer = new Timer(delayMillis, event -> action.run());
        timer.setRepeats(true);
        timer.start();
        timers.add(timer);
        return timer;
    }

    @Override
    public void removeNotify() {
        for (DataChangeBus.Subscription subscription : subscriptions) {
            subscription.unsubscribe();
        }
        subscriptions.clear();
        for (Timer timer : timers) {
            timer.stop();
        }
        timers.clear();
        super.removeNotify();
    }
}
