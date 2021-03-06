package com.dianping.paas.core.message.nats.bus;

import com.dianping.paas.core.extension.ExtensionLoader;
import com.dianping.paas.core.message.codec.Codec;
import com.dianping.paas.core.message.nats.MessageCallBack;
import nats.client.Nats;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * chao.yu@dianping.com
 * Created by yuchao on 2015/11/05 16:05.
 */
@Component
public class DefaultMessageBus implements MessageBus {
    /**
     * 默认同步获取响应时间为5000毫秒
     */
    public static final int DEFAULT_TIMEOUT = 5000;

    @Resource
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private Nats nats;
    private Codec codec = ExtensionLoader.getExtension(Codec.class);

    public <REQUEST_T> void requestSync(String subject, REQUEST_T payload, final MessageCallBack messageCallBack, int timeout) {
        timeout = normalizeTimeout(timeout);

        beginSync(messageCallBack, timeout);
        try {
            doRequest(subject, payload, messageCallBack, timeout);
        } catch (Exception e) {
            messageCallBack.error(e);
        }
        endSync(messageCallBack);
    }

    private void beginSync(MessageCallBack messageCallBack, int timeout) {
        messageCallBack.beginSync(timeout);
    }

    private void endSync(MessageCallBack messageCallBack) {
        try {
            messageCallBack.endSync();
        } catch (InterruptedException e) {
            messageCallBack.error(e);
        } finally {
            if (!messageCallBack.isCalled()) {
                messageCallBack.timeout();
            }
        }
    }

    private int normalizeTimeout(int timeout) {
        if (timeout <= 0) {
            timeout = DEFAULT_TIMEOUT;
        }
        return timeout;
    }

    /**
     * 同步获取响应
     */
    public <REQUEST_T> void requestSync(String subject, REQUEST_T payload, MessageCallBack messageCallBack) {
        requestSync(subject, payload, messageCallBack, DEFAULT_TIMEOUT);
    }

    /**
     * 异步获取响应
     */
    public <REQUEST_T> void requestAsync(String subject, REQUEST_T payload, MessageCallBack messageCallBack) {
        doRequest(subject, payload, messageCallBack, DEFAULT_TIMEOUT);
    }


    private <REQUEST_T> void doRequest(String subject, REQUEST_T payload, MessageCallBack messageCallBack, long timeout) {
        String body;
        try {
            body = codec.encode(payload);
            nats.request(subject, body, timeout, TimeUnit.SECONDS, messageCallBack);
        } catch (Exception e) {
            messageCallBack.error(e);
        }

    }
}
