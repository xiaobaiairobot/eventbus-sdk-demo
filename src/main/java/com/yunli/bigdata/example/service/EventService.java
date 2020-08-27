package com.yunli.bigdata.example.service;

import com.yunli.bigdata.example.dto.CreateCredentialMsgRequest;
import com.yunli.bigdata.example.dto.CreateMessageRequest;

/**
 * @author david
 * @date 2020/7/28 7:40 下午
 */
public interface EventService {
  void sendMessage();

  void createMessageService(CreateMessageRequest createMessageRequest);

  void createMessageService(CreateCredentialMsgRequest createCredentialMsgRequest);
}
