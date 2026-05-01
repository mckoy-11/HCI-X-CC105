package main.ui.components;

import javax.swing.SwingWorker;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * AsyncUploader - Utility class for executing database operations in background
 * threads to prevent UI freezing on the Event Dispatch Thread (EDT).
 * 
 * Usage:
 *   AsyncUploader.execute(
 *       () -> service.getAllData(),  // Background work
 *       data -> ui.setData(data)     // UI update on EDT
 *   );
 */
public class AsyncUploader {

    private AsyncUploader() {
    }

    /**
     * Execute a background task and update UI when complete.
     *
     * @param backgroundTask The task to execute in background thread
     * @param onSuccess Consumer to run on EDT with result
     * @param <T> Result type
     * @return SwingWorker for cancellation/status
     */
    public static <T> SwingWorker<T, Void> execute(
            Callable<T> backgroundTask,
            Consumer<T> onSuccess) {
        return execute(backgroundTask, onSuccess, null);
    }

    /**
     * Execute a background task with error handling.
     *
     * @param backgroundTask The task to execute in background thread
     * @param onSuccess Consumer to run on EDT with result
     * @param onError Consumer to run on EDT with exception
     * @param <T> Result type
     * @return SwingWorker for cancellation/status
     */
    public static <T> SwingWorker<T, Void> execute(
            Callable<T> backgroundTask,
            Consumer<T> onSuccess,
            Consumer<Exception> onError) {

        return new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return backgroundTask.call();
            }

            @Override
            protected void done() {
                try {
                    T result = get();
                    if (onSuccess != null) {
                        onSuccess.accept(result);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (onError != null) {
                        onError.accept(e);
                    }
                }
            }
        };
    }

    /**
     * Execute a simple runnable in background.
     *
     * @param backgroundTask The runnable to execute
     * @param onComplete Optional callback when complete
     * @return SwingWorker for status
     */
    public static SwingWorker<Void, Void> execute(
            Runnable backgroundTask,
            Runnable onComplete) {

        return new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                backgroundTask.run();
                return null;
            }

            @Override
            protected void done() {
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        };
    }
}
