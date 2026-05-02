package main.store;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Small in-process event bus used to keep Swing panels in sync after data
 * changes. Listeners stay lightweight and execute on the calling thread.
 */
public final class DataChangeBus {

    private static final Map<String, CopyOnWriteArrayList<Runnable>> LISTENERS =
            new ConcurrentHashMap<String, CopyOnWriteArrayList<Runnable>>();

    private DataChangeBus() {
    }

    public static Subscription subscribe(String topic, Runnable listener) {
        LISTENERS.computeIfAbsent(topic, key -> new CopyOnWriteArrayList<>()).add(listener);
        return () -> unsubscribe(topic, listener);
    }

    public static void publish(String... topics) {
        if (topics == null) {
            return;
        }

        for (String topic : topics) {
            List<Runnable> topicListeners = LISTENERS.get(topic);
            if (topicListeners == null) {
                continue;
            }

            for (Runnable listener : topicListeners) {
                try {
                    listener.run();
                } catch (RuntimeException ignored) {
                    // One failed listener should not block the rest of the UI.
                }
            }
        }
    }

    private static void unsubscribe(String topic, Runnable listener) {
        List<Runnable> topicListeners = LISTENERS.get(topic);
        if (topicListeners == null) {
            return;
        }

        topicListeners.remove(listener);
        if (topicListeners.isEmpty()) {
            LISTENERS.remove(topic);
        }
    }

    public interface Subscription {
        void unsubscribe();
    }
}
