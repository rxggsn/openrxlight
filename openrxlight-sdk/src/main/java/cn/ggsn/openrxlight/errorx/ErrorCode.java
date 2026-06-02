package cn.ggsn.openrxlight.errorx;

public interface ErrorCode {

    int getValue();

    String getMessage();

    default String getFullMessage(Object... args) {
        if (args == null || args.length == 0) {
            return getMessage();
        }
        return String.format(getMessage(), args);
    }
}
