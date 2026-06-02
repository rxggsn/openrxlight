package cn.ggsn.openrxlight.web;

import java.util.Set;

import com.google.common.collect.Sets;

import cn.ggsn.openrxlight.httpx.HttpMethod;

public class AuthConstants {
        public static final String RXDOMAIN_ACCOUNT_ID_HEADER = "X-RxDomain-Account-Id";
        public static final String RXDOMAIN_ACCOUNT_NAME_HEADER = "X-RxDomain-Account-Name";
        public static final String RXDOMAIN_SOURCE_TYPE_HEADER = "X-RxDomain-Source-Type";
        public static final String RXDOMAIN_ACCOUNT_TYPE_HEADER = "X-RxDomain-Account-Type";
        public static final String RXDOMAIN_ROLE_TYPES_HEADER = "X-RxDomain-Role-Types";
        public static final String RXDOMAIN_TOKEN_EXPIRED_HEADER = "X-RxDomain-Token-Expired";
        public static final String RXDOMAIN_ENCRYPTED_PASSWORD_HEADER = "X-RxDomain-Encrypted-Password";
        public static final Set<Uri> IGNORE_AUTH_BIZ_URI = Sets.newHashSet(
                        new Uri(HttpMethod.POST.getName(), "/authorization/login"),
                        new Uri(HttpMethod.POST.getName(), "/authorization/authorize"),
                        new Uri(HttpMethod.POST.getName(), "/authorization/verification-code"),
                        new Uri(HttpMethod.GET.getName(), "/healthcheck"));
}
