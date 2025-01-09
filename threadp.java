import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class CustomThreadPool {
    private final int maxThreads;
    private final BlockingQueue<Runnable> taskQueue;
    private final WorkerThread[] workerThreads;
    private final AtomicBoolean isShutdown;

    public CustomThreadPool(int maxThreads) {
        this.maxThreads = maxThreads;
        this.taskQueue = new LinkedBlockingQueue<>();
        this.workerThreads = new WorkerThread[maxThreads];
        this.isShutdown = new AtomicBoolean(false);

        for (int i = 0; i < maxThreads; i++) {
            workerThreads[i] = new WorkerThread(taskQueue);
            workerThreads[i].start();
        }
    }

    public void submitTask(Runnable task) {
        if (isShutdown.get()) {
            throw new IllegalStateException("ThreadPool has been shut down.");
        }
        taskQueue.offer(task);
    }

    public void shutdown() {
        isShutdown.set(true);
        for (WorkerThread worker : workerThreads) {
            worker.interrupt();
        }
    }

    private static class WorkerThread extends Thread {
        private final BlockingQueue<Runnable> taskQueue;

        public WorkerThread(BlockingQueue<Runnable> taskQueue) {
            this.taskQueue = taskQueue;
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    Runnable task = taskQueue.take();
                    task.run();
                } catch (InterruptedException e) {
                    interrupt();
                }
            }
        }
    }

    public static void main(String[] args) {
        CustomThreadPool threadPool = new CustomThreadPool(5);
        for (int i = 0; i < 10; i++) {
            final int taskId = i;
            threadPool.submitTask(() -> {
                System.out.println("Executing Task: " + taskId + " by " + Thread.currentThread().getName());
            });
        }
        threadPool.shutdown();
    }
}
