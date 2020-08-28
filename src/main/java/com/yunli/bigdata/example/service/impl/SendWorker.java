package com.yunli.bigdata.example.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.yunli.bigdata.eventbus.sdk.AccessCredential;
import com.yunli.bigdata.eventbus.sdk.Event;
import com.yunli.bigdata.example.config.EventBusConfiguration;
import com.yunli.bigdata.util.DateUtil;

/**
 * @author david
 * @date 2020/7/29 10:44 上午
 */
public class SendWorker implements Runnable {

  private final Logger logger = LoggerFactory.getLogger(SendWorker.class);

  private final EventBusConfiguration eventBusConfiguration;

  private final com.yunli.bigdata.eventbus.sdk.Producer producer;

  private AtomicInteger index = new AtomicInteger(0);

  public SendWorker(EventBusConfiguration eventBusConfiguration) {
    this.eventBusConfiguration = eventBusConfiguration;
    String host = this.eventBusConfiguration.getServer();
    int port = this.eventBusConfiguration.getProducerPort().intValue();
    producer = new myProducer(host, port, null);
  }


  public SendWorker(EventBusConfiguration eventBusConfiguration, AccessCredential accessCredential) {
    this.eventBusConfiguration = eventBusConfiguration;
    String host = this.eventBusConfiguration.getServer();
    int port = this.eventBusConfiguration.getProducerPort().intValue();
    producer = new myProducer(host, port, accessCredential);
  }

  @Override
  public void run() {
    for (int i = 0; i < eventBusConfiguration.getSendTimes(); i++) {
      String msg = getRandomMessage();
      try {
        producer.sendEventAsync(
            new Event(eventBusConfiguration.getSourceTopic(), msg, msg.getBytes(StandardCharsets.UTF_8)));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    logger.info("消息发送请求已提交，数量：" + eventBusConfiguration.getSendTimes());
    try {
      producer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private class myProducer extends com.yunli.bigdata.eventbus.sdk.Producer {

    Logger logger2 = LoggerFactory.getLogger(myProducer.class);

    public myProducer(String host, int port, AccessCredential accessCredential) {
      super(host, port, accessCredential);
    }

    @Override
    protected void onError(Throwable t) {
      logger2.warn(t.getMessage());
      t.printStackTrace();
      // System.out.println("消息发送异常");
    }
  }

  private String getRandomMessage2() {
    // id name sex age birthday
    Double dInfo = Math.floor(Math.random() * (100 - 1)) + 1;
    String strMessage = String.format("%d,\"%s\",\"%s\",%d,\"%s\"", index.incrementAndGet(), "张三" + index.get(), "男",
        dInfo.intValue(), DateUtil.toSimpleString(new Date()));
    // System.out.println(strMessage);
    return strMessage;
  }

  private String getRandomMessage() {
    if (!StringUtils.isEmpty(this.eventBusConfiguration.getColumns())) {
      String[] listColumn = this.eventBusConfiguration.getColumns().split(",");
      StringBuilder sb = new StringBuilder();
      int myIndex = index.incrementAndGet();
      Double dInfo = Math.floor(Math.random() * (100 - 1)) + 1;
      for (String column : listColumn) {
        if ("int".equals(column.toLowerCase())) {
          sb.append(myIndex).append(",");
        } else if ("string".equals(column.toLowerCase())) {
          String strMessage = String.format("\"%s\"", "张三" + myIndex);
          sb.append(strMessage).append(",");
        } else if ("long".equals(column.toLowerCase())) {
          sb.append(myIndex).append(",");
        } else if ("date".equals(column.toLowerCase())) {
          String strMessage = String.format("\"%s\"", DateUtil.toSimpleString(new Date()));
          sb.append(strMessage).append(",");
        } else if ("double".equals(column.toLowerCase())) {
          sb.append(dInfo).append(",");
        } else if ("float".equals(column.toLowerCase())) {
          sb.append(dInfo).append(",");
        }
      }
      return sb.delete(sb.length() - 1, sb.length()).toString();
    } else {
      // id name sex age birthday
      String strMessage = String.format("\"%s\"", "张三" + index.incrementAndGet());
      // System.out.println(strMessage);
      return strMessage;
    }
  }
}
