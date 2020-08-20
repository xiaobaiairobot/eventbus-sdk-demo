package com.yunli.bigdata.example.service.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.yunli.bigdata.example.config.EventBusConfiguration;
import com.yunli.bigdata.example.dto.CreateMessageRequest;
import com.yunli.bigdata.example.service.EventService;

/**
 * @author david
 * @date 2020/7/28 7:41 下午
 */
@Service
public class EventServiceImpl implements EventService {

  private final Logger logger = LoggerFactory.getLogger(EventServiceImpl.class);

  private final EventBusConfiguration eventBusConfiguration;

  private final ExecutorService fixedExecutor = new ThreadPoolExecutor(3, 3, 0L, TimeUnit.MILLISECONDS,
      new LinkedBlockingQueue<Runnable>(1024), new ThreadFactoryBuilder()
      .setNameFormat("worker_pool").build(), new ThreadPoolExecutor.AbortPolicy());


  @Autowired
  public EventServiceImpl(EventBusConfiguration eventBusConfiguration) {
    this.eventBusConfiguration = eventBusConfiguration;
  }

  @Override
  public void sendMessage() {
    SendWorker sendWorker = new SendWorker(this.eventBusConfiguration);
    fixedExecutor.execute(sendWorker);
  }

  @Override
  public void createMessageService(CreateMessageRequest createMessageRequest) {
    EventBusConfiguration configuration = new EventBusConfiguration();
    // 参数配置
    configuration.setServer(createMessageRequest.getServer() == null ? this.eventBusConfiguration.getServer()
        : createMessageRequest.getServer());
    configuration
        .setProducerPort(createMessageRequest.getProducerPort() == null ? this.eventBusConfiguration.getProducerPort()
            : createMessageRequest.getProducerPort());
    configuration.setSendTimes(createMessageRequest.getSendTimes() == null ? this.eventBusConfiguration.getSendTimes()
        : createMessageRequest.getSendTimes());
    configuration
        .setSourceTopic(createMessageRequest.getSourceTopic() == null ? this.eventBusConfiguration.getSourceTopic()
            : createMessageRequest.getSourceTopic());
    configuration.setColumns(createMessageRequest.getColumns() == null ? this.eventBusConfiguration.getColumns()
        : createMessageRequest.getColumns());

    SendWorker sendWorker = new SendWorker(configuration);
    fixedExecutor.execute(sendWorker);
  }
}
