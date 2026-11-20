package com.example.payment.payment.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class UserIdentityFeignInterceptor implements RequestInterceptor {

    public static final String USER_ID_HEADER = "X-User-Id";

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return;
        }

        String userId = attributes.getRequest().getHeader(USER_ID_HEADER);
        if (userId != null && !userId.isBlank()) {
            template.header(USER_ID_HEADER, userId);
        }
    }
}
