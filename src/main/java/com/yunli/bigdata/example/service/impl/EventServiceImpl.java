package com.yunli.bigdata.example.service.impl;

import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.yunli.bigdata.common.CommonMessageCode;
import com.yunli.bigdata.eventbus.sdk.AccessCredential;
import com.yunli.bigdata.eventbus.sdk.AccessCredential.AccessCredentialData;
import com.yunli.bigdata.eventbus.sdk.AccessCredential.Privilege;
import com.yunli.bigdata.example.config.EventBusConfiguration;
import com.yunli.bigdata.example.dto.CreateCredentialMsgRequest;
import com.yunli.bigdata.example.dto.CreateMessageRequest;
import com.yunli.bigdata.example.exception.SdkException;
import com.yunli.bigdata.example.service.EventService;
import com.yunli.bigdata.util.DateUtil;

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

  /**
   * 使用带证书的请求发送数据
   * @param createMessageRequest
   */
  @Override
  public void createMessageService(CreateCredentialMsgRequest createMessageRequest) {
    if (StringUtils.isEmpty(createMessageRequest.getAppId())
        || StringUtils.isEmpty(createMessageRequest.getExpiredDate())
        || StringUtils.isEmpty(createMessageRequest.getSignature())
        || StringUtils.isEmpty(createMessageRequest.getKey())) {
      throw new SdkException(CommonMessageCode.ERROR_1001, "证书参数");
    }
    // 此处只校验证书内容是否齐全，证书是否合法会由总线服务器校验
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

    // String key, Date expiredDate, String appId, Set<AccessCredential.Privilege> privileges, String signature
    Date dtExpired = null;
    try {
      dtExpired = DateUtil.fromFullString(createMessageRequest.getExpiredDate());
    } catch (ParseException e) {
      e.printStackTrace();
      throw new SdkException(CommonMessageCode.ERROR_1004, "expiredDate", createMessageRequest.getExpiredDate());
    }
    Set<Privilege> setPrivilege = new HashSet<Privilege>();
    createMessageRequest.getPrivileges()
        .forEach(o -> setPrivilege.add(new Privilege(o.getResource(), o.getAction())));
    AccessCredentialData accessCredentialData = new AccessCredentialData(
        createMessageRequest.getKey(),
        dtExpired,
        createMessageRequest.getAppId(),
        setPrivilege,
        createMessageRequest.getSignature()
    );
    AccessCredential accessCredential = new AccessCredential(accessCredentialData);

    SendWorker sendWorker = new SendWorker(configuration, accessCredential);
    fixedExecutor.execute(sendWorker);
  }
}
