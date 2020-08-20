package com.yunli.bigdata.example.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yunli.bigdata.eventbus.sdk.AccessCredential;
import com.yunli.bigdata.eventbus.sdk.Consumer;
import com.yunli.bigdata.eventbus.sdk.ConsumerGroup;
import com.yunli.bigdata.eventbus.sdk.Event;
import com.yunli.bigdata.example.config.EventBusConfiguration;

/**
 * @author david
 * @date 2020/7/28 7:54 下午
 */
@Component
public class EventReceiver {

  private final Logger logger = LoggerFactory.getLogger(EventReceiver.class);

  private final EventBusConfiguration eventBusConfiguration;

  private final Consumer consumer;


  @Autowired
  public EventReceiver(EventBusConfiguration eventBusConfiguration) {
    this.eventBusConfiguration = eventBusConfiguration;
    int port = this.eventBusConfiguration.getConsumerPort().intValue();
    List<Integer> parts = new ArrayList<>();
    parts.add(0);
    this.consumer = new myConsumer(eventBusConfiguration.getServer(), port,
        new ConsumerGroup(this.eventBusConfiguration.getConsumerGroup(),
            this.eventBusConfiguration.getTargetTopic()), parts, null);
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
