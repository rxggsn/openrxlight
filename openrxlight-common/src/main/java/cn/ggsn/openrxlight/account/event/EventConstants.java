package cn.ggsn.openrxlight.account.event;

public class EventConstants {
    public interface AccountEvent {
        public static final String ACCOUNT_TOPIC = "openrxlight.accounts";
        public static final String ACCOUNT_CREATED = "openrxlight.accounts.created";
        public static final String ACCOUNT_DELETED = "openrxlight.accounts.deleted";
    }
}
