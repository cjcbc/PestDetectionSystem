package com.gzy.pestdetectionsystem.interceptor;

import com.gzy.pestdetectionsystem.annotation.RateLimit;
import com.gzy.pestdetectionsystem.utils.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RateLimitInterceptorTest {

    private RedisUtil redisUtil;
    private RateLimitInterceptor interceptor;
    private TestController controller;

    @BeforeEach
    void setUp() {
        redisUtil = mock(RedisUtil.class);
        interceptor = new RateLimitInterceptor(redisUtil);
        controller = new TestController();
    }

    @Test
    void rateLimitAnnotationShouldExposeDefaultRule() throws Exception {
        Method method = TestController.class.getMethod("defaultLimited");
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        assertEquals(10, rateLimit.limit());
        assertEquals(60, rateLimit.window());
        assertEquals(TimeUnit.SECONDS, rateLimit.timeUnit());
        assertEquals("请求过于频繁，请稍后再试", rateLimit.message());
    }

    @Test
    void unannotatedHandlerShouldPassWithoutRedis() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handler = handlerMethod("unlimited");

        boolean allowed = interceptor.preHandle(request, response, handler);

        assertTrue(allowed);
        verify(redisUtil, never()).incr(eq("unused"), anyLong());
    }

    @Test
    void sameIpShouldBeBlockedAfterLimit() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handler = handlerMethod("limitedTwice");
        String key = "rate_limit:" + TestController.class.getName() + ".limitedTwice:ip:127.0.0.1";

        when(redisUtil.incr(key, 1)).thenReturn(3L);

        boolean allowed = interceptor.preHandle(request, response, handler);

        assertFalse(allowed);
        assertEquals(429, response.getStatus());
        assertEquals("application/json;charset=UTF-8", response.getContentType());
        assertEquals("UTF-8", response.getCharacterEncoding());
        assertTrue(response.getContentAsString().contains("请求太频繁"));
    }

    @Test
    void firstHitShouldSetWindowExpire() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handler = handlerMethod("limitedTwice");
        String key = "rate_limit:" + TestController.class.getName() + ".limitedTwice:ip:127.0.0.1";

        when(redisUtil.incr(key, 1)).thenReturn(1L);

        boolean allowed = interceptor.preHandle(request, response, handler);

        assertTrue(allowed);
        verify(redisUtil).expire(key, 60);
    }

    @Test
    void requestWithUserIdShouldUseUserIdentity() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.setAttribute("userId", 42L);
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handler = handlerMethod("limitedTwice");
        String userKey = "rate_limit:" + TestController.class.getName() + ".limitedTwice:user:42";
        String ipKey = "rate_limit:" + TestController.class.getName() + ".limitedTwice:ip:127.0.0.1";

        when(redisUtil.incr(userKey, 1)).thenReturn(1L);

        boolean allowed = interceptor.preHandle(request, response, handler);

        assertTrue(allowed);
        verify(redisUtil).incr(userKey, 1);
        verify(redisUtil, never()).incr(ipKey, 1);
    }

    @Test
    void forwardedForHeaderShouldProvideClientIp() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("X-Forwarded-For", "203.0.113.8, 10.0.0.2");
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handler = handlerMethod("limitedTwice");
        String key = "rate_limit:" + TestController.class.getName() + ".limitedTwice:ip:203.0.113.8";

        when(redisUtil.incr(key, 1)).thenReturn(1L);

        boolean allowed = interceptor.preHandle(request, response, handler);

        assertTrue(allowed);
        verify(redisUtil).incr(key, 1);
    }

    private HandlerMethod handlerMethod(String methodName) throws NoSuchMethodException {
        return new HandlerMethod(controller, TestController.class.getMethod(methodName));
    }

    static class TestController {
        @RateLimit
        public void defaultLimited() {
        }

        @RateLimit(limit = 2, message = "请求太频繁")
        public void limitedTwice() {
        }

        public void unlimited() {
        }
    }
}
