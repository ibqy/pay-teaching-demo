package com.xb.pay.demo.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GlobalExceptionHandlerTest - 全局异常处理器 + 统一响应封装单元测试
 *
 * @author ibqy
 */
@DisplayName("全局异常处理器测试")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Nested
    @DisplayName("ApiResponse 统一响应")
    class ApiResponseTest {

        @Test
        @DisplayName("ok — 正常返回")
        void apiResponseOk() {
            ApiResponse<String> resp = ApiResponse.ok("data");
            assertEquals(0, resp.code());
            assertEquals("ok", resp.message());
            assertEquals("data", resp.data());
        }

        @Test
        @DisplayName("error — 错误返回")
        void apiResponseError() {
            ApiResponse<Void> resp = ApiResponse.error(500, "boom");
            assertEquals(500, resp.code());
            assertEquals("boom", resp.message());
            assertNull(resp.data());
        }

        @Test
        @DisplayName("ok(null) — data 为 null")
        void apiResponseOkWithNull() {
            ApiResponse<Void> resp = ApiResponse.ok(null);
            assertEquals(0, resp.code());
            assertNull(resp.data());
        }
    }

    @Nested
    @DisplayName("异常处理")
    class ExceptionHandlingTest {

        @Test
        @DisplayName("IllegalArgumentException → 400")
        void handleIllegalArgument() {
            ResponseEntity<ApiResponse<Void>> resp =
                    handler.handleIllegalArgument(new IllegalArgumentException("订单不存在: X001"));
            assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
            assertEquals(400, resp.getBody().code());
            assertTrue(resp.getBody().message().contains("订单不存在"));
        }

        @Test
        @DisplayName("未知异常 → 500 + requestId")
        void handleUnknown() {
            ResponseEntity<ApiResponse<Void>> resp =
                    handler.handleUnknown(new RuntimeException("unexpected"));
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
            assertEquals(500, resp.getBody().code());
            assertTrue(resp.getBody().message().contains("requestId="));
        }

        @Test
        @DisplayName("缺少必填参数 → 400")
        void handleMissingParam() {
            MissingServletRequestParameterException ex =
                    new MissingServletRequestParameterException("amount", "requestParam");
            ResponseEntity<ApiResponse<Void>> resp = handler.handleMissingParam(ex);
            assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
            assertTrue(resp.getBody().message().contains("amount"));
        }
    }
}
