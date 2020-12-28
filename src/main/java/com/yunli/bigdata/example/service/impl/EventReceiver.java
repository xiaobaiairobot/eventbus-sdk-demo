package com.yunli.bigdata.example.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.websocket.OnError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.yunli.bigdata.common.CommonMessageCode;
import com.yunli.bigdata.eventbus.sdk.AccessCredential;
import com.yunli.bigdata.eventbus.sdk.AccessCredential.AccessCredentialData;
import com.yunli.bigdata.eventbus.sdk.AccessCredential.Privilege;
import com.yunli.bigdata.eventbus.sdk.Consumer;
import com.yunli.bigdata.eventbus.sdk.ConsumerGroup;
import com.yunli.bigdata.eventbus.sdk.Event;
import com.yunli.bigdata.example.config.CredentialConfiguration;
import com.yunli.bigdata.example.config.EventBusConfiguration;
import com.yunli.bigdata.example.exception.SdkException;
import com.yunli.bigdata.util.DateUtil;

/**
 * @author david
 * @date 2020/7/28 7:54 下午
 */
@Component
public class EventReceiver {

  private final Logger logger = LoggerFactory.getLogger(EventReceiver.class);

  private final EventBusConfiguration eventBusConfiguration;

  private final CredentialConfiguration credentialConfiguration;

  private Consumer consumer;

  private List<Integer> parts;

  private AccessCredential accessCredential = null;

  private Integer port;

  private CountDownLatch latch = new CountDownLatch(1);

  @Autowired
  public EventReceiver(EventBusConfiguration eventBusConfiguration,
      CredentialConfiguration credentialConfiguration) {
    this.eventBusConfiguration = eventBusConfiguration;
    this.credentialConfiguration = credentialConfiguration;
    port = this.eventBusConfiguration.getConsumerPort().intValue();
    parts = new ArrayList<>();
    parts.add(0);

    if (!StringUtils.isEmpty(credentialConfiguration.getSignature())) {
      Date dtExpired = null;
      try {
        dtExpired = DateUtil.fromFullString(credentialConfiguration.getExpiredDate());
      } catch (ParseException e) {
        e.printStackTrace();
        throw new SdkException(CommonMessageCode.ERROR_1004, "expiredDate", credentialConfiguration.getExpiredDate());
      }
      Set<Privilege> setPrivilege = new HashSet<Privilege>();
      setPrivilege.add(new Privilege(eventBusConfiguration.getSourceTopic(), "consume"));
      accessCredential = new AccessCredential(new AccessCredentialData(
          credentialConfiguration.getKey(),
          dtExpired,
          credentialConfiguration.getAppId(),
          setPrivilege,
          credentialConfiguration.getSignature()
      ));
    }
    if (consumer == null) {
      initConsumer();
      Thread watchThread = new Thread(() -> {
        /**
         * 异常时重建consumer
         */
        while (true) {
          try {
            latch.await();
            latch = new CountDownLatch(1);
            consumer.close();
          } catch (Exception e) {
            e.printStackTrace();
          }
          initConsumer();
        }
      });
      watchThread.start();
    }
  }

  private void initConsumer() {
    this.consumer = new myConsumer(eventBusConfiguration.getServer(), port,
        new ConsumerGroup(this.eventBusConfiguration.getConsumerGroup(),
            this.eventBusConfiguration.getTargetTopic()), parts, accessCredential) {
      @Override
      protected void onError(Throwable throwable) {
        logger.warn("--------on error-------");
        logger.warn(throwable.getMessage(), throwable);
        latch.countDown();
      }
    };
  }

  private static class myConsumer extends Consumer {
    private final Logger logger = LoggerFactory.getLogger(myConsumer.class);

    public myConsumer(String host, int port, ConsumerGroup consumerGroup, List<Integer> partitions,
        AccessCredential accessCredential) {
      super(host, port, consumerGroup, partitions, accessCredential);
    }

    public myConsumer(List<String> etcdEndpoints, ConsumerGroup consumerGroup,
        List<Integer> partitions, AccessCredential accessCredential) {
      super(etcdEndpoints, consumerGroup, partitions, accessCredential);
    }

    @Override
    protected void onError(Throwable t) {
      logger.warn("运行过程出错，错误信息为： {}", t.getMessage(), t);
      t.printStackTrace();
      try {
        this.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    @Override
    protected void onEvent(Event e) {
      logger.info(new String(e.getData(), StandardCharsets.UTF_8));
    }
  }
}
