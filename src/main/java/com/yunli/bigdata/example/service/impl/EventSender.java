package com.yunli.bigdata.example.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.yunli.bigdata.eventbus.sdk.AccessCredential;
import com.yunli.bigdata.eventbus.sdk.Event;
import com.yunli.bigdata.eventbus.sdk.Producer;
import com.yunli.bigdata.example.config.EventBusConfiguration;
import com.yunli.bigdata.util.DateUtil;

/**
 * @author david
 * @date 2020/8/28 11:32 上午
 */
@Service
public class EventSender {

  private final Logger logger = LoggerFactory.getLogger(EventSender.class);

  private AccessCredential accessCredential;

  private CountDownLatch latch = new CountDownLatch(1);

  /**
   * 用于消息发送行号计数器
   */
  private AtomicInteger index = new AtomicInteger(0);

  public AccessCredential getAccessCredential() {
    return accessCredential;
  }

  public void setAccessCredential(AccessCredential accessCredential) {
    this.accessCredential = accessCredential;
  }

  private Producer producer;

  public void sendEvent(EventBusConfiguration eventBusConfiguration)
      throws IOException, InterruptedException {

    if (producer == null) {
      initProducer(eventBusConfiguration);

      Thread watchThread = new Thread(() -> {
        /**
         * 异常时重建producer
         */
        while (true) {
          try {
            latch.await();
            latch = new CountDownLatch(1);
            producer.close();
          } catch (Exception e) {
            e.printStackTrace();
          }
          initProducer(eventBusConfiguration);
        }
      });
      watchThread.start();
    }

    for (int i = 0; i < eventBusConfiguration.getSendTimes(); i++) {
      String msg = getRandomMessage(eventBusConfiguration);
      try {
        producer.sendEventAsync(
            new Event(eventBusConfiguration.getSourceTopic(), msg, msg.getBytes(StandardCharsets.UTF_8)));
      } catch (Exception e) {
        logger.warn("消息提交异常");
        logger.warn(e.getMessage(), e);
      }
    }
    logger.info("消息提交成功");
    index.set(0);
    Thread.sleep(500);
  }

  private void initProducer(EventBusConfiguration eventBusConfiguration) {
    producer = new Producer(eventBusConfiguration.getServer(), eventBusConfiguration.getProducerPort().intValue(),
        getAccessCredential()) {
      @Override
      protected void onError(Throwable throwable) {
        logger.warn(throwable.getMessage(), throwable);
        latch.countDown();
      }
    };
  }

  /**
   * 生成随机消息
   * @param eventBusConfiguration
   * @return
   */
  private String getRandomMessage(EventBusConfiguration eventBusConfiguration) {
    if (!StringUtils.isEmpty(eventBusConfiguration.getColumns())) {
      String[] listColumn = eventBusConfiguration.getColumns().split(",");
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
      return String.format("\"%s\"", "张三" + index.incrementAndGet());
    }
  }
}
