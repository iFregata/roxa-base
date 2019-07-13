# Eclipse Vert.x live reload

New run configuration

The main class: `io.vertx.core.Launcher`

The arguments

```
run <full-class-name-of-boot-verticle> --redeploy=**/*.java --launcher-class=io.vertx.core.Launcher -Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory
```

The Env: `APP_LOG_LEVEL=debug`

# 部署说明