# 云粒智慧数据总线SDK使用说明

## 打包编译方法
```shell script
# mvn clean package -DskipTests
# ./build-images.sh eventbus_demo
# ./push-images-RC.sh eventbus_demo
```

## 启动
```shell script
# docker run -p 50002:50002 -d --restart always --name eventbus_demo registry.cn-beijing.aliyuncs.com/yunli-com.yunli.bigdata/eventbus-demo:RC-1.0.0
```

## 发送
参数中server和producerPort可不配，使用系统配置文件的即可，参数有值会优先使用参数中的
如果模拟多个column，使用英文逗号分隔即可，数据会自动随机生成，如：int,string,long,double,string这样即发送5列的数据
sendTimes表示一次请求发送多少条数据（连续发送中间不停顿）
```shell script
# curl --location --request POST 'http://172.30.13.177:50002/eb/test' \
  --header 'Content-Type: application/json' \
  --data-raw '{
  	"server":"172.30.13.177",
  	"producerPort":"31236",
  	"sourceTopic":"autotestSQ",
  	"sendTimes":100,
  	"columns":"string"
  }'
```