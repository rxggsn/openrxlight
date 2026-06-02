package cn.ggsn.openrxlight.web;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.ggsn.openrxlight.errorx.BizException;
import cn.ggsn.openrxlight.errorx.CommonErrorCode;
import cn.ggsn.openrxlight.lang.Maps2;
import cn.ggsn.openrxlight.request.Validate;
import cn.ggsn.openrxlight.utils.JsonUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public abstract class DispatchCommand<T> implements Command<T> {
    @SuppressWarnings("rawtypes")
    private static final Map<Integer, Class<? extends Command>> COMMAND_MAP = Maps2.empty();

    @Setter
    @JsonIgnore
    public String commandKey = "type";

    @Getter
    @JsonAnySetter
    @JsonAnyGetter
    private Map<String, Object> command = Maps2.empty();

    public DispatchCommand() {

    }

    public DispatchCommand(Map<String, Object> command) {
        this();
        this.command = command;
    }

    public static void register(Integer type, @SuppressWarnings("rawtypes") Class<? extends Command> command) {
        COMMAND_MAP.put(type, command);
    }

    public Integer getCommandKey() {
        return Optional.ofNullable(this.command.get(this.commandKey))
                .map(Object::toString)
                .map(Integer::valueOf)
                .orElse(null);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public T toResource() {
        Object commandType = this.command.get(this.commandKey);
        if (commandType == null) {
            throw new BizException(CommonErrorCode.CommandKeyIsRequired, this.commandKey);
        }

        if (commandType instanceof Integer) {
            Integer type = (Integer) commandType;
            Class<? extends Command> commandClass = COMMAND_MAP.get(type);
            if (commandClass == null) {
                return null;
            }

            Command tCommand = JsonUtils.fromJson(
                    JsonUtils.toJson(this.command), commandClass);
            if (tCommand == null) {
                return null;
            }

            if (commandClass.isAssignableFrom(Validate.class)) {
                ((Validate) tCommand).validate();
            }
            return (T) tCommand.toResource();

        } else {
            throw new BizException(CommonErrorCode.CommandKeyIsRequired, commandType.toString());
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public T toResourceWithId(String id) {
        Object commandType = this.command.get(this.commandKey);
        if (commandType == null) {
            throw new BizException(CommonErrorCode.CommandKeyIsRequired, this.commandKey);
        }

        if (commandType instanceof Integer) {
            Integer type = (Integer) commandType;
            Class<? extends Command> commandClass = COMMAND_MAP.get(type);
            return Optional.ofNullable(commandClass)
                    .map(clazz -> {
                        Command tCommand = JsonUtils.fromJson(
                                JsonUtils.toJson(this.command), commandClass);

                        return Optional.ofNullable(tCommand)
                                .map(cmd -> {
                                    if (commandClass.isAssignableFrom(Validate.class)) {
                                        ((Validate) tCommand).validate();
                                    }
                                    return (T) cmd.toResourceWithId(id);
                                }).orElse(null);
                    })
                    .orElse(null);

        } else {
            throw new BizException(CommonErrorCode.CommandKeyIsRequired, commandType.toString());
        }
    }

    public T toResourceWithCommandKey(Integer commandKey) {
        this.command.put(this.commandKey, commandKey);
        return toResource();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public T toResourceWithUuId(UUID id) {
        Object commandType = this.command.get(this.commandKey);
        if (commandType == null) {
            throw new BizException(CommonErrorCode.CommandKeyIsRequired, this.commandKey);
        }

        if (commandType instanceof Integer) {
            Integer type = (Integer) commandType;
            Class<? extends Command> commandClass = COMMAND_MAP.get(type);
            return Optional.ofNullable(commandClass)
                    .map(clazz -> {
                        Command tCommand = JsonUtils.fromJson(
                                JsonUtils.toJson(this.command), commandClass);

                        return Optional.ofNullable(tCommand)
                                .map(cmd -> {
                                    if (commandClass.isAssignableFrom(Validate.class)) {
                                        ((Validate) tCommand).validate();
                                    }
                                    return (T) cmd.toResourceWithUuId(id);
                                }).orElse(null);
                    })
                    .orElse(null);

        } else {
            throw new BizException(CommonErrorCode.CommandKeyIsRequired, commandType.toString());
        }
    }
}
