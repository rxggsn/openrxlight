package cn.ggsn.rxlight.ai.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventV1<T extends EventBody> {
    private EventHeader header;
    private T body;
}
