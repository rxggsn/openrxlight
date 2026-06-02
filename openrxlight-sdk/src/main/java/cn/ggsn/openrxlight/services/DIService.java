package cn.ggsn.openrxlight.services;

import java.util.stream.Stream;

import cn.ggsn.openrxlight.config.Config;
import cn.ggsn.openrxlight.httpx.HttpMethod;
import cn.ggsn.openrxlight.request.chat.ChatRequest;
import cn.ggsn.openrxlight.request.chat.RecvRequest;
import cn.ggsn.openrxlight.response.chat.ChatResponse;
import cn.ggsn.openrxlight.utils.Transport;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DIService {
    private final Config config;

    public Stream<ChatResponse> chat(ChatRequest request) throws Exception {
        return Transport.sendSse(this.config, request,
                "/dhforce_intelligence/chat/completion",
                HttpMethod.POST.getName(), ChatResponse.class);
    }

    public Stream<ChatResponse> recv(RecvRequest request) throws Exception {
        return Transport.sendSse(this.config, request,
                "/dhforce_intelligence/chat/recv",
                HttpMethod.POST.getName(), ChatResponse.class);
    }
}
