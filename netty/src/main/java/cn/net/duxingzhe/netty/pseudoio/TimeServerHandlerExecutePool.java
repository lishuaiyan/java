package cn.net.duxingzhe.netty.pseudoio;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author luke yan
 * @Description
 * @CreateDate 2020/11/06
 */
public class TimeServerHandlerExecutePool {
    private ExecutorService executor;
    public TimeServerHandlerExecutePool(int maxPoolSize, int queueSize) {
        // 由于线程和队列都是有界的，因此无论客户端并发连接数多大，都不会导致线程个数过于膨胀或者内存溢出Time
        executor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), maxPoolSize, 120L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(queueSize));
    }
    public void execute(Runnable task) {
        executor.execute(task);
    }
}
