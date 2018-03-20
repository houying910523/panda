package com.mv.data.panda.common.shell;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.*;

/**
 * author: houying
 * date  : 17-6-30
 * desc  :
 */
public class Shell {
    private static final Logger logger = LoggerFactory.getLogger(Shell.class);
    private String cmd;
    private String dir;
    private String output;
    private Process process;
    private volatile boolean stop = false;
    private int processId;
    private int exitCode;
    private CountDownLatch completeLatch;
    private CountDownLatch startLatch;
    private CountDownLatch logLatch;
    private Map<String, String> env;
    private Exception exception;

    public Shell(String cmd, String dir, String output) {
        this.cmd = cmd;
        this.dir = dir;
        this.output = output;
        this.exitCode = -1;
        this.completeLatch = new CountDownLatch(1);
        this.startLatch = new CountDownLatch(1);
        this.logLatch = new CountDownLatch(1);
    }

    public void start(ThreadPoolExecutor pool) {
        pool.execute(this::logInternal);
        pool.execute(this::executeInternal);
    }

    public void start() {
        new Thread(this::logInternal).start();
        new Thread(this::executeInternal).start();
    }

    public void kill() {
        if (process != null && processId != 0) {
            try {
                synchronized (this) {
                    stop = true;
                }
                Runtime.getRuntime().exec(String.format("kill -15 -%s", processId));
                logger.info("kill -15 -{}", processId);
            } catch (IOException e) {
                logger.error("Kill attempt failed.", e);
                e.printStackTrace();
            }
            process.destroy();
        }
    }

    public boolean awaitCompletion(long timeout) throws InterruptedException {
        return completeLatch.await(timeout, TimeUnit.MILLISECONDS);
    }

    public void awaitCompletion() throws InterruptedException {
        completeLatch.await();
    }

    public synchronized Map<String, String> env() {
        if (env == null) {
            env = new ConcurrentHashMap<>();
        }
        return env;
    }

    public Shell setEnv(String key, String value) {
        env().put(key, value);
        return this;
    }

    private int processId(final Process process) {
        int processId = 0;
        try {
            Field f = process.getClass().getDeclaredField("pid");
            f.setAccessible(true);
            processId = f.getInt(process);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return processId;
    }

    private void executeInternal() {
        ProcessBuilder builder = new ProcessBuilder(cmd.split(" "))
                .redirectErrorStream(true)
                .directory(new File(dir));
        if (env != null) {
            builder.environment().putAll(env);
        }
        try {
            process = builder.start();
            processId = processId(process);
            logger.info("process {} start", processId);
            startLatch.countDown();
            exitCode = process.waitFor();
            logger.info("process {} end, exit code: {}", processId, exitCode);
        } catch (IOException e) {
            exception = e;
        } catch (InterruptedException e) {
            logger.warn("process was interrupted");
        } finally {
            stop = true;
            try {
                logLatch.await();
            } catch (InterruptedException e) {
                //nothing
            }
            if (process != null) {
                process.destroy();
            }
            completeLatch.countDown();
            logger.info("shell exit");
        }
    }

    private void logInternal() {
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            startLatch.await();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            if (output != null) {
                writer = new BufferedWriter(new FileWriter(output));
            } else {
                writer = new BufferedWriter(new OutputStreamWriter(System.err));
            }
            writeLog(reader, writer);
        } catch (InterruptedException e) {
            logger.error("log thread interrupted");
        } catch (IOException e) {
            logger.error("日志输出异常", e);
            exception = e;
            kill();
        } finally {
            logLatch.countDown();
            IOUtils.closeQuietly(writer);
            logger.info("log thread exit");
        }
    }

    protected void writeLog(BufferedReader reader, BufferedWriter writer) {
        try {
            String line = null;
            while(true) {
                synchronized (this) {
                    if (stop || (line = reader.readLine()) == null) {
                        break;
                    }
                }
                writer.write(line);
                logger.info("[shell log] {}", line);
                writer.newLine();
                writer.flush();
                process(line);
            }
        } catch (IOException e) {
            logger.error("写日志异常退出", e);
        }
    }

    protected void process(String line) {}

    public boolean isFailed() throws InterruptedException {
        awaitCompletion();
        return exception != null || (exitCode != 143 && exitCode != 0);
    }

    public boolean isSuccess() throws InterruptedException {
        awaitCompletion();
        return exception == null && (exitCode == 143 || exitCode == 0);
    }
}
