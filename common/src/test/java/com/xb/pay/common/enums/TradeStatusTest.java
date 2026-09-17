package com.xb.pay.common.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TradeStatus 状态机流转测试
 *
 * <p>作者：ibqy | 日期：2026-09-17</p>
 *
 * <p>验证支付状态机的合法/非法流转，确保资金安全守卫有效。</p>
 */
@DisplayName("TradeStatus 状态机流转测试")
class TradeStatusTest {

    @Nested
    @DisplayName("WAITING → 合法流转")
    class WaitingTransitions {

        @Test
        @DisplayName("WAITING → SUCCESS（支付成功）")
        void waitingToSuccess() {
            assertTrue(TradeStatus.WAITING.canTransitionTo(TradeStatus.SUCCESS));
        }

        @Test
        @DisplayName("WAITING → FAILED（支付失败）")
        void waitingToFailed() {
            assertTrue(TradeStatus.WAITING.canTransitionTo(TradeStatus.FAILED));
        }

        @Test
        @DisplayName("WAITING → CLOSED（超时关单）")
        void waitingToClosed() {
            assertTrue(TradeStatus.WAITING.canTransitionTo(TradeStatus.CLOSED));
        }

        @Test
        @DisplayName("WAITING → REFUNDING 非法（未支付不能退款）")
        void waitingCannotRefund() {
            assertFalse(TradeStatus.WAITING.canTransitionTo(TradeStatus.REFUNDING));
        }
    }

    @Nested
    @DisplayName("SUCCESS → 合法流转")
    class SuccessTransitions {

        @Test
        @DisplayName("SUCCESS → REFUNDING（申请退款）")
        void successToRefunding() {
            assertTrue(TradeStatus.SUCCESS.canTransitionTo(TradeStatus.REFUNDING));
        }

        @Test
        @DisplayName("SUCCESS → CLOSED 非法（已支付不能关单）")
        void successCannotClose() {
            assertFalse(TradeStatus.SUCCESS.canTransitionTo(TradeStatus.CLOSED));
        }

        @Test
        @DisplayName("SUCCESS → WAITING 非法（不可回退）")
        void successCannotWait() {
            assertFalse(TradeStatus.SUCCESS.canTransitionTo(TradeStatus.WAITING));
        }
    }

    @Nested
    @DisplayName("终态不可变")
    class TerminalStates {

        @Test
        @DisplayName("FAILED → 任何状态均非法")
        void failedIsTerminal() {
            for (TradeStatus target : TradeStatus.values()) {
                assertFalse(TradeStatus.FAILED.canTransitionTo(target),
                        "FAILED → " + target + " should be false");
            }
        }

        @Test
        @DisplayName("REFUNDED → 任何状态均非法")
        void refundedIsTerminal() {
            for (TradeStatus target : TradeStatus.values()) {
                assertFalse(TradeStatus.REFUNDED.canTransitionTo(target),
                        "REFUNDED → " + target + " should be false");
            }
        }

        @Test
        @DisplayName("CLOSED → 任何状态均非法")
        void closedIsTerminal() {
            for (TradeStatus target : TradeStatus.values()) {
                assertFalse(TradeStatus.CLOSED.canTransitionTo(target),
                        "CLOSED → " + target + " should be false");
            }
        }
    }

    @Nested
    @DisplayName("REFUNDING → 合法流转")
    class RefundingTransitions {

        @Test
        @DisplayName("REFUNDING → REFUNDED（退款完成）")
        void refundingToRefunded() {
            assertTrue(TradeStatus.REFUNDING.canTransitionTo(TradeStatus.REFUNDED));
        }

        @Test
        @DisplayName("REFUNDING → SUCCESS 非法（不可回退）")
        void refundingCannotGoBack() {
            assertFalse(TradeStatus.REFUNDING.canTransitionTo(TradeStatus.SUCCESS));
        }
    }
}
