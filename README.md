# atluofu-dynamic-config-center

`atluofu-dynamic-config-center` 是一个基于 Redis + Redisson Pub/Sub 的分布式动态配置中心 SDK。

目标不是做一个完整 Nacos，而是做一个轻量级 Starter：

- Bean 初始化时扫描 `@DdcValue` 注解字段；
- 首次从 Redis 读取配置值，不存在则写入注解默认值；
- 本地字段持有配置值，业务读取字段即可，不需要每次读 Redis；
- 配置变更通过 Redis 发布/订阅推送；
- Listener 收到消息后反射刷新本地 Bean 字段；
- 支持 `String / Integer / Long / Boolean / BigDecimal / Duration / Enum` 等 Spring ConversionService 可转换类型；
- 支持同一个配置 key 绑定多个 Bean 字段；
- 变更消息透传 `traceId/requestId`，方便日志追踪。

## 一、Maven 坐标

父工程：

```xml
<groupId>top.atluofu</groupId>
<artifactId>atluofu-dynamic-config-center</artifactId>
<version>1.0-SNAPSHOT</version>
<packaging>pom</packaging>
```

业务工程引入 SDK：

```xml
<dependency>
    <groupId>top.atluofu</groupId>
    <artifactId>atluofu-dynamic-config-center-sdk</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## 二、包名

核心包名：

```text
top.atluofu.middleware.dynamic.config.center.sdk
```

核心结构：

```text
top.atluofu.middleware.dynamic.config.center.sdk
├── annotation
│   └── DdcValue.java
├── config
│   ├── DdcAutoConfiguration.java
│   └── DdcProperties.java
├── domain
│   ├── model
│   │   ├── DdcChangeMessage.java
│   │   ├── DdcFieldBinding.java
│   │   └── DdcValueDefinition.java
│   └── service
│       ├── IDdcConfigCenterService.java
│       └── DdcConfigCenterService.java
├── infrastructure
│   └── redis
│       └── RedisDdcConfigRepository.java
├── listener
│   └── DdcRedisChangeListener.java
├── processor
│   └── DdcBeanPostProcessor.java
├── repository
│   └── DdcConfigRepository.java
├── template
│   └── DdcConfigTemplate.java
└── types
    └── common
        ├── DdcConstants.java
        └── DdcException.java
```

## 三、Spring Boot 自动装配

Spring Boot 3 使用：

```text
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

内容：

```text
top.atluofu.middleware.dynamic.config.center.sdk.config.DdcAutoConfiguration
```

同时保留了 `META-INF/spring.factories`，方便旧项目迁移。

## 四、配置示例

```yaml
atluofu:
  dynamic:
    config:
      center:
        enabled: true
        system: test-system
        namespace: dev
        key-prefix: atluofu:ddc
        create-missing: true
        fail-fast: true
        redis:
          address: redis://127.0.0.1:6379
          database: 0
          connection-pool-size: 16
          connection-minimum-idle-size: 4
          timeout: 3000
```

Redis Key 格式：

```text
{key-prefix}:{system}:{namespace}:{attribute}
```

例如：

```text
atluofu:ddc:test-system:dev:downgradeSwitch
```

Redis Topic 默认格式：

```text
{key-prefix}:{system}:{namespace}:topic
```

例如：

```text
atluofu:ddc:test-system:dev:topic
```

## 五、业务使用

```java
@Service
public class DdcDemoService {

    @DdcValue("downgradeSwitch:0")
    private volatile Integer downgradeSwitch;

    @DdcValue("grayRate:0.01")
    private volatile BigDecimal grayRate;

    public boolean downgraded() {
        return downgradeSwitch != null && downgradeSwitch == 1;
    }
}
```

字段建议加 `volatile`，因为 DDC 通过监听线程反射修改值，而业务线程会直接读取字段。别小看这个点，JMM 不讲武德。

## 六、发布配置变更

```java
@RestController
@RequestMapping("/ddc")
public class DdcDemoController {

    private final DdcConfigTemplate ddcConfigTemplate;

    public DdcDemoController(DdcConfigTemplate ddcConfigTemplate) {
        this.ddcConfigTemplate = ddcConfigTemplate;
    }

    @PostMapping("/publish")
    public Map<String, Object> publish(@RequestParam String attribute, @RequestParam String value) {
        long receivers = ddcConfigTemplate.publish(attribute, value);
        return Map.of("attribute", attribute, "value", value, "receivers", receivers);
    }
}
```

批量发布：

```java
ddcConfigTemplate.publishAll(Map.of(
        "downgradeSwitch", 1,
        "grayRate", "0.25"
));
```

当前批量发布采用多条消息逐个发布，够轻量、够直接。如果后续要强一致批量事务，再单独抽 `DdcBatchChangeMessage`，不要一开始就把小工具写成宇宙飞船。

## 七、启动测试

启动 Redis：

```bash
docker run -d --name redis-ddc -p 6379:6379 redis:7
```

启动测试工程：

```bash
mvn -pl atluofu-dynamic-config-center-test spring-boot:run
```

查看当前值：

```bash
curl http://localhost:8080/ddc/values
```

发布变更：

```bash
curl -X POST 'http://localhost:8080/ddc/publish?attribute=downgradeSwitch&value=4'
```

再次查看：

```bash
curl http://localhost:8080/ddc/values
```

## 八、设计边界

这个 SDK 适合：

- 降级开关；
- 灰度比例；
- 限流阈值；
- 小规模业务动态参数；
- 组件内部运行时参数。

暂时不建议拿它替代完整配置中心：

- 不做配置版本审计；
- 不做权限审批；
- 不做配置历史回滚；
- 不做跨机房强一致；
- 不做复杂表达式规则。

后续可以扩展 admin-server、审计表、审批流、配置历史版本、Webhook、企业微信/公众号通知。
