# logback-appender
this library has 2 features:
1. console appender
2. kafka appender

# usage

## example
1. add this lib to project
```xml
		<dependency>
			<groupId>com.github.amsatrio</groupId>
			<artifactId>logback-appender</artifactId>
			<version>0.0.2</version>
		</dependency>
```

if you need kafka appender, add this dependency:
```xml
		<dependency>
			<groupId>org.springframework.kafka</groupId>
			<artifactId>spring-kafka</artifactId>
		</dependency>
```

2. configure logback-spring.xml

- for console
 ```xml
...
<configuration>
    ...
    <property name="logPattern" value="%d | %-5level | %logger{35} | %msg%n" />
    <property name="logEncoding" value="UTF-8" />
     <appender name="APP-CONSOLE" class="com.github.amsatrio.ConsoleAppender">
        <encoder class="PatternLayoutEncoder">
            <charset>${logEncoding}</charset>
            <pattern>${logPattern}</pattern>
        </encoder>
    </appender>

    <appender name="ASYNC-APP-CONSOLE" class="AsyncAppender">
        <appender-ref ref="APP-CONSOLE" />
        <queueSize>500</queueSize>
        <maxFlushTime>1000</maxFlushTime>
        <neverBlock>false</neverBlock>
    </appender>

    <root>
        <appender-ref ref="ASYNC-APP-CONSOLE" />
    </root>
    ...
</configuration>
```

- for kafka

```xml
...
<configuration>
    ...
    <property name="logServiceName" value="spring-boot-container-demo" />

    <appender name="APP-KAFKA" class="com.github.amsatrio.KafkaAppender">
        <encoder class="LoggingEventCompositeJsonEncoder">
            <providers>
                <mdc />
                <context />
                <logLevel />
                <pattern>
                    <pattern>
                        {
                        "app": "${logServiceName}"
                        }
                    </pattern>
                </pattern>
                <threadName />
                <message />
                <logstashMarkers />
                <stackTrace />
            </providers>
        </encoder>
        <topic>demo-project-log</topic>
        <producerConfig>bootstrap.servers=localhost:9092</producerConfig>
        <producerConfig>retries=2</producerConfig>
        <producerConfig>batch.size=16384</producerConfig>
    </appender>

    <appender name="ASYNC-APP-KAFKA" class="AsyncAppender">
        <appender-ref ref="APP-KAFKA" />
        <queueSize>500</queueSize>
        <maxFlushTime>1000</maxFlushTime>
        <neverBlock>false</neverBlock>
    </appender>

    <root>
        <appender-ref ref="ASYNC-APP-KAFKA" />
    </root>
    ...
</configuration>
```