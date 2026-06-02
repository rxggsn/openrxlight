DO $$ 
    BEGIN 
    EXECUTE format('SET app.admin.email = %L', '${admin_email}');
END 
$$;
INSERT INTO user_accounts (
        account_id,
        account_type,
        display_name,
        role_types,
        source,
        external_accounts
    )
VALUES (
        uuid_generate_v7(),
        3,
        'Admin',
        ARRAY [99],
        4,
        json_build_array(
            json_build_object(
                'external_account_id',
                COALESCE(
                    NULLIF(current_setting('app.admin.email', true), ''),
                    ''
                ),
                'account_type',
                4
            )
        )
    );