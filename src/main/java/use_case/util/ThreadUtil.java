package use_case.util;

import java.util.concurrent.*;

public class ThreadUtil {

    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int MAX_POOL_SIZE = CORE_POOL_SIZE * 2;

    private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.CallerRunsPolicy() // 队列满时由调用者执行，防止丢弃任务
    );

    public static ExecutorService getExecutor() {
        return EXECUTOR;
    }

    public static void shutdown() {
        EXECUTOR.shutdown();
    }
}
