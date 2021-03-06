package com.dianping.paas.core.message.nats;

import com.dianping.paas.core.extension.ExtensionLoader;
import com.dianping.paas.core.message.codec.Codec;
import lombok.Data;
import nats.client.Message;
import nats.client.MessageHandler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * chao.yu@dianping.com
 * Created by yuchao on 2015/11/05 16:13.
 */
@Data
public abstract class MessageCallBack<Res> implements MessageHandler {
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    private CountDownLatch countDownLatch;

    private Class<Res> responseType;

    private Res response;

    private boolean called = false;

    private int timeout;

    private Codec codec = ExtensionLoader.getExtension(Codec.class);

    public MessageCallBack(Class<Res> responseType) {
        this.responseType = responseType;
    }

    public void onMessage(final Message message) {
        try {
            called = true;
            response = codec.decode(message.getBody(), responseType);
            success(response);
        } catch (Exception e) {
            error(e);
        } finally {
            tryCountDown();
        }
    }

    public void success(Res res) {
        // ignore
    }

    public abstract void error(Throwable throwable);

    public abstract void timeout();

    private void tryCountDown() {
        if (countDownLatch != null) {
            countDownLatch.countDown();
        }
    }

    public void beginSync(int timeout) {
        countDownLatch = new CountDownLatch(1);
        this.timeout = timeout;
    }

    public void endSync() throws InterruptedException {
        countDownLatch.await(timeout, TimeUnit.MILLISECONDS);
    }

}
