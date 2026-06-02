DO $$
BEGIN
    EXECUTE format('SET app.lark.agent_app.app_id = %L', '${lark_app_id}');
    EXECUTE format('SET app.lark.agent_app.app_secret = %L', '${lark_app_secret}');
    EXECUTE format('SET app.lark.agent_app.encrypt_key = %L', '${lark_app_encrypt_key}');
    EXECUTE format('SET app.lark.agent_app.verification_token = %L', '${lark_app_verification_token}');
    EXECUTE format('SET app.openrxlight.client_id = %L', '${openrxlight_client_id}');
    EXECUTE format('SET app.openrxlight.client_secret = %L', '${openrxlight_client_secret}');
    EXECUTE format('SET app.openrxlight.signature_priv_key = %L', '${openrxlight_signature_priv_key}');
    EXECUTE format('SET app.openrxlight.signature_pub_key = %L', '${openrxlight_signature_pub_key}');

END
$$;

INSERT INTO agent_app (
    app_id,
    app_secret,
    app_type,
    credential,
    scenario,
    enabled,
    openrxlight
) VALUES (
    COALESCE(
        NULLIF(current_setting('app.lark.agent_app.app_id', true), ''),
        ''
    ),
    COALESCE(
        NULLIF(current_setting('app.lark.agent_app.app_secret', true), ''),
        ''
    ),
    1,
    json_build_object(
        'encrypt_key', COALESCE(
            NULLIF(current_setting('app.lark.agent_app.encrypt_key', true), ''),
            ''
        ),
        'verification_token', COALESCE(
            NULLIF(current_setting('app.lark.agent_app.verification_token', true), ''),
            ''
        ),
        'type', 'cn.ggsn.rxlight.ai.domain.credentials.LarkCredential'
    ),
    2,
    true,
    json_build_object(
        'client_id', COALESCE(
            NULLIF(current_setting('app.openrxlight.client_id', true), ''),
            ''
        ),
        'client_secret', COALESCE(
            NULLIF(current_setting('app.openrxlight.client_secret', true), ''),
            ''
        ),
        'signature_priv_key', COALESCE(
            NULLIF(current_setting('app.openrxlight.signature_priv_key', true), ''),
            ''
        ),
        'signature_pub_key', COALESCE(
            NULLIF(current_setting('app.openrxlight.signature_pub_key', true), ''),
            ''
        ),
        'type', 'cn.ggsn.rxlight.ai.domain.credentials.OpenRxLightCredential'
    )
);