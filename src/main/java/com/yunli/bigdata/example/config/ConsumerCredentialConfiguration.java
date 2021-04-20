package com.yunli.bigdata.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author david
 * @date 2021/4/20 10:30 上午
 */
@Configuration
public class ConsumerCredentialConfiguration {

  @Value("${spring.consumer_credential.key}")
  private String key;

  @Value("${spring.consumer_credential.signature}")
  private String signature;

  @Value("${spring.consumer_credential.expiredDate}")
  private String expiredDate;

  @Value("${spring.consumer_credential.appId}")
  private String appId;

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getSignature() {
    return signature;
  }

  public void setSignature(String signature) {
    this.signature = signature;
  }

  public String getExpiredDate() {
    return expiredDate;
  }

  public void setExpiredDate(String expiredDate) {
    this.expiredDate = expiredDate;
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }
}
