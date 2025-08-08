package com.fortune.project.controller;

import com.fortune.project.dto.request.order.PaymentResponse;
import com.fortune.project.service.PaymentService;
import com.fortune.project.service.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final VnPayService vnPayService;

    /**
     * API xử lý callback từ Payment Gateway
     */
//    @PostMapping("/callback")
//    public ResponseEntity<Void> handleCallback(@RequestBody PaymentCallbackRequest request) {
//        paymentService.handleGatewayCallback(request.getTransactionId(), request.isSuccess());
//        return ResponseEntity.ok().build();
//    }
    @GetMapping("/vnpay-return")
    public ResponseEntity<String> handleVnpayReturn(HttpServletRequest request) {
        Map<String, String> vnpParams = extractVnpParams(request);
        String receivedHash = vnpParams.remove("vnp_SecureHash");

        if (vnPayService.validateReturn(vnpParams, receivedHash)) {
            Long paymentId = Long.valueOf(vnpParams.get("vnp_TxnRef"));
            String txnStatus = vnpParams.get("vnp_TransactionStatus");

            if ("00".equals(txnStatus)) {
                paymentService.handleGatewayCallback(paymentId, true);
                return ResponseEntity.ok("Payment success");
            } else {
                paymentService.handleGatewayCallback(paymentId, false);
                return ResponseEntity.ok("Payment failed");
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Checksum không hợp lệ");
    }

    private Map<String, String> extractVnpParams(HttpServletRequest request) {
        return request.getParameterMap().entrySet().stream()
                .filter(e -> e.getKey().startsWith("vnp_"))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()[0]));
    }

    /**
     * API refund một payment
     */
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable Long paymentId) {
        PaymentResponse response = paymentService.refundPayment(paymentId);
        return ResponseEntity.ok(response);
    }
}

