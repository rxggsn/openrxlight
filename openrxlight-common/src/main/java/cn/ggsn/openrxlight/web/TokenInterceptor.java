package cn.ggsn.openrxlight.web;

import java.io.IOException;
import java.lang.reflect.Method;
import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;

import cn.ggsn.openrxlight.errorx.BizException;
import cn.ggsn.openrxlight.errorx.CommonErrorCode;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
// import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@ApplicationScoped
@Slf4j
public class TokenInterceptor {

    private final BlockingTokenStore tokenStore;

    void onStartup(@Observes StartupEvent event) {
        log.info("Application StartupEvent observed, initializing TokenInterceptor listeners.");
        BeanManager beanManager = CDI.current().getBeanManager();
        for (var bean : beanManager.getBeans(Object.class)) {
            if (!bean.getScope().isAnnotationPresent(jakarta.enterprise.context.NormalScope.class)) {
                log.debug("Skipping bean {} with non-normal scope {}", bean.getBeanClass().getName(),
                        bean.getScope().getName());
                continue;
            }
            Object reference = beanManager.getReference(bean, Object.class, beanManager.createCreationalContext(bean));
            Class<?> beanClass = bean.getBeanClass();
            for (Method method : beanClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(IgnoreAuth.class)) {
                    log.info(
                            "Found method {}#{} annotated with @IgnoreAuth, checking for valid HTTP method annotations.",
                            beanClass.getName(), method.getName());
                    checkAndProcessMethod(reference, beanClass, method);
                }
            }
        }
    }

    private void checkAndProcessMethod(Object reference, Class<?> beanClass, Method method) {
        IgnoreAuth annotation = method.getAnnotation(IgnoreAuth.class);
        if (annotation != null) {
            Uri uri = new Uri();
            Path mainPath = beanClass.getAnnotation(jakarta.ws.rs.Path.class);
            Path methodPath = method.getAnnotation(jakarta.ws.rs.Path.class);
            if (method.isAnnotationPresent(jakarta.ws.rs.POST.class)) {
                uri.setMethod("POST");
            } else if (method.isAnnotationPresent(jakarta.ws.rs.GET.class)) {
                uri.setMethod("GET");
            } else if (method.isAnnotationPresent(jakarta.ws.rs.PUT.class)) {
                uri.setMethod("PUT");
            } else if (method.isAnnotationPresent(jakarta.ws.rs.DELETE.class)) {
                uri.setMethod("DELETE");
            } else {
                log.warn(
                        "Method {} in class {} is annotated with @IgnoreAuth but does not have a valid HTTP method annotation. Skipping.",
                        method.getName(), beanClass.getName());
                return;
            }

            if (mainPath != null) {
                uri.setPath(mainPath.value());
            }
            if (methodPath != null) {
                uri.addPath(methodPath.value());
            }
            AuthConstants.IGNORE_AUTH_BIZ_URI.add(uri);
            log.info("Registered IgnoreAuth for {}#{} with URI {} {}", beanClass.getName(), method.getName(),
                    uri.getMethod(), uri.getPath());
        }
    }

    @ServerRequestFilter
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String authorization = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (AuthConstants.IGNORE_AUTH_BIZ_URI.stream()
                .anyMatch(
                        uri -> uri.match(new Uri(requestContext.getMethod(), requestContext.getUriInfo().getPath())))) {
            return;
        }
        if (StringUtils.isBlank(authorization)
                || !StringUtils.startsWith(authorization, AuthorizationToken.BEARER_PREFIX)) {
            throw new BizException(CommonErrorCode.NoAuthorization, "Authorization header is missing");
        }
        String actualToken = StringUtils.removeStart(authorization, AuthorizationToken.BEARER_PREFIX);
        AuthorizationToken jwtToken = AuthorizationToken.fromJwtToken(actualToken);

        this.tokenStore
                .getTokenByAccountId(jwtToken.getAccountId().toString(), jwtToken.getAccountType())
                .map(token -> {
                    if (StringUtils.equals(actualToken, token.getJwtToken())) {
                        return true;
                    } else {
                        return false;
                    }
                })
                .ifPresent(isAuthorized -> {
                    if (!isAuthorized) {
                        throw new BizException(CommonErrorCode.NoAuthorization, "Invalid or expired token");
                    }
                    // Store the token in the request-scoped holder
                    requestContext.setSecurityContext(jwtToken);
                });

    }

}
