# sop-common

- sop-bridge-nacos：注册中心桥接器，接入nacos
- sop-bridge-eureka：注册中心桥接器，接入eureka
- sop-gateway-common：提供给网关使用
- sop-service-common：提供给微服务端使用，需要打成jar

正式开发请将这些模块上传的maven私服

- 打包成jar：`mvn clean package`
- 上传到本机仓库：`mvn clean install`
- 上传到maven私服：`mvn clean deploy`