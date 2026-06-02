package cn.ggsn.openrxlight.web;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommandInjector<T> {
    private Integer commandKey;
    private Class<? extends Command<T>> commandClass;

    public <V extends DispatchCommand<T>> void inject(V request) {
        V.register(this.commandKey, this.commandClass);
    }
}
