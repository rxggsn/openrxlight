package cn.ggsn.rxlight.ai.claw;

import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;

import cn.ggsn.openrxlight.request.chat.UserMessageType;
import cn.ggsn.rxlight.ai.claw.vo.ClawContext;
import cn.ggsn.rxlight.ai.domain.RxLightChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public abstract class ClawBot {
    protected final Queue<RxLightChatMessage> recvQueue = new java.util.concurrent.ConcurrentLinkedQueue<>();
    protected final ClawContext context;

    public Stream<RxLightChatMessage> recv() {
        return Stream.generate(() -> {
            while (!this.context.hasRemaingMessages) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    log.debug("wait-loop interrupted {}", e);
                    break;
                }
            }
            RxLightChatMessage msg = recvQueue.poll();
            if (StringUtils.equals(msg.getMessageType(), UserMessageType.LOCATION.getName())) {
                this.context.refreshLocation(msg.getUserId(), Optional.ofNullable(msg.getExtra())
                        .map(RxLightChatMessage.ExtraInfo::getLocation)
                        .orElse(""));
            } else {
                this.context.getLocation(msg.getUserId()).ifPresent(location -> {
                    if (StringUtils.isNotBlank(location)) {
                        msg.setLocation(location);
                    }
                });
            }

            this.context.hasRemaingMessages = msg != null && !recvQueue.isEmpty();
            return msg;
        }).filter(Objects::nonNull);
    }

    protected void push(RxLightChatMessage msg) {
        if (msg != null) {
            msg.setAppId(this.context.getAgentId());
            this.recvQueue.offer(msg);
            this.context.hasRemaingMessages = true;
            log.debug("Received message: {} from user: {}", msg.getAppMessageId(),
                    msg.getUserId());
        }
    }

    public abstract void send(RxLightChatMessage msg) throws Exception;
}
