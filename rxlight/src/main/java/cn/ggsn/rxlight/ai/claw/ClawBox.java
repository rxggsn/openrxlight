package cn.ggsn.rxlight.ai.claw;

import java.util.List;
import java.util.concurrent.ExecutorService;

import cn.ggsn.openrxlight.audio.AudioRecognizer;
import cn.ggsn.openrxlight.event.EventBusPublisher;
import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.translator.Translator;
import cn.ggsn.rxlight.ai.domain.AgentApp;
import cn.ggsn.rxlight.ai.domain.AppType;
import io.quarkus.runtime.Startup;
import io.vertx.redis.client.RedisAPI;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Startup
@Slf4j
public class ClawBox {
  private final List<AgentApp> bot;
  private final ExecutorService executor;

  public ClawBox(RedisAPI redis, EventBusPublisher eventBusPublisher, Translator translator,
      AudioRecognizer audioRecognizer) {
    this.executor = java.util.concurrent.Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    this.bot = AgentApp.findApps(Lists2.of(AppType.FEISHU));
    this.bot.forEach(agent -> {
      try {
        this.executor.submit(agent.startClaw(redis, eventBusPublisher, translator, audioRecognizer));
      } catch (Exception e) {
        log.error("Failed to start claw bot for app {}", agent.getAppId(), e);
        throw new RuntimeException(e);
      }
    });
  }
}
