package cn.ggsn.openrxlight.services;

import cn.ggsn.openrxlight.config.Config;
import cn.ggsn.openrxlight.httpx.HttpMethod;
import cn.ggsn.openrxlight.request.chat.ChatRequest;
import cn.ggsn.openrxlight.request.chat.RecvRequest;
import cn.ggsn.openrxlight.response.chat.ChatResponse;
import cn.ggsn.openrxlight.utils.Transport;
import io.reactivex.Flowable;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DIService {
    private final Config config;

    public Flowable<ChatResponse> chat(ChatRequest request) throws Exception {
        return Transport.sendSse(this.config, request,
                "/dhforce_intelligence/chat/completion",
                HttpMethod.POST.getName(), ChatResponse.class);
    }

    public Flowable<ChatResponse> recv(RecvRequest request) throws Exception {
        return Transport.sendSse(this.config, request,
                "/dhforce_intelligence/chat/recv",
                HttpMethod.POST.getName(), ChatResponse.class);
    }
}
