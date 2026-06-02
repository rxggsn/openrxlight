CREATE TABLE IF NOT EXISTS user_accounts (
    id BIGSERIAL PRIMARY KEY,
    account_id UUID NOT NULL,
    account_name VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NULL,
    avatar TEXT NULL,
    account_info JSONB NULL,
    external_accounts JSONB NULL,
    created_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    source INT NOT NULL,
    last_login_time TIMESTAMP WITHOUT TIME ZONE NULL,
    additional_info JSONB NULL
);
create sequence user_accounts_SEQ start with 1 increment by 1;
CREATE UNIQUE INDEX IF NOT EXISTS uq_user_accounts_account_id ON user_accounts(account_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_user_accounts_account_name_source ON user_accounts(account_name, source);
COMMENT ON TABLE user_accounts IS 'Table to store user account information';
COMMENT ON COLUMN user_accounts.account_id IS 'Unique identifier for the user account';
COMMENT ON COLUMN user_accounts.account_name IS 'Username of the account';
COMMENT ON COLUMN user_accounts.display_name IS 'Display name of the user';
COMMENT ON COLUMN user_accounts.avatar IS 'URL or path to the avatar image';
COMMENT ON COLUMN user_accounts.account_info IS 'Additional information about the account in JSON format';
COMMENT ON COLUMN user_accounts.external_accounts IS 'Linked external accounts in JSON format';
COMMENT ON COLUMN user_accounts.created_time IS 'Timestamp when the account was created';
COMMENT ON COLUMN user_accounts.updated_time IS 'Timestamp when the account was last updated';
COMMENT ON COLUMN user_accounts.is_active IS 'Indicates if the account is active';
COMMENT ON COLUMN user_accounts.source IS 'Source of the account (e.g., internal, external)';
COMMENT ON COLUMN user_accounts.last_login_time IS 'Timestamp of the last login';
COMMENT ON COLUMN user_accounts.additional_info IS 'Any additional information in JSON format';