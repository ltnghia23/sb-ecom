package com.fortune.project.service.impl;

import com.fortune.project.config.VnPayConfig;
import com.fortune.project.service.VnPayService;
import com.fortune.project.util.HttpClientUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VnPayServiceImpl implements VnPayService {

    private final VnPayConfig config;
    private final HttpClientUtils httpClientUtils;


    /**
     * Tạo URL thanh toán VNPay để redirect user
     */
    public String createPaymentUrl(Long paymentId, Double amount, String clientIp) {
        Map<String, String> vnpParams = new TreeMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", config.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf((long) (amount * 100)));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", paymentId.toString());
        vnpParams.put("vnp_OrderInfo", "Thanh toán đơn #" + paymentId);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", config.getReturnUrl());
        vnpParams.put("vnp_IpAddr", clientIp);
        vnpParams.put("vnp_CreateDate", LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        String hashData = buildHashData(vnpParams);
        String secureHash = hmacSHA512(config.getHashSecret(), hashData);

        String queryString = buildQueryString(vnpParams);
        return config.getPayUrl() + "?" + queryString + "&vnp_SecureHash=" + secureHash;
    }

    /**
     * Xác minh dữ liệu callback từ VNPay
     */
    public boolean validateReturn(Map<String, String> vnpParams, String receivedHash) {
        String hashData = buildHashData(vnpParams);
        String calculatedHash = hmacSHA512(config.getHashSecret(), hashData);
        return calculatedHash.equalsIgnoreCase(receivedHash);
    }

    /**
     * Query trạng thái giao dịch từ VNPay (API QueryDR)
     */
    public String queryTransaction(String txnRef) {
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "querydr");
        params.put("vnp_TmnCode", config.getTmnCode());
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_CreateDate", LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        String hashData = buildHashData(params);
        String secureHash = hmacSHA512(config.getHashSecret(), hashData);
        params.put("vnp_SecureHash", secureHash);

        return httpClientUtils.post(config.getApiUrl(), params);
    }

    /**
     * Hoàn tiền giao dịch (API Refund)
     */
    public String refundTransaction(String txnRef, Double amount) {
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "refund");
        params.put("vnp_TmnCode", config.getTmnCode());
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_Amount", String.valueOf((long) (amount * 100)));
        params.put("vnp_CreateDate", LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        String hashData = buildHashData(params);
        String secureHash = hmacSHA512(config.getHashSecret(), hashData);
        params.put("vnp_SecureHash", secureHash);

        return httpClientUtils.post(config.getApiUrl(), params);
    }

    // Helper methods

    private String buildHashData(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
    }

    private String buildQueryString(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> urlEncode(e.getKey()) + "=" + urlEncode(e.getValue()))
                .collect(Collectors.joining("&"));
    }

    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Lỗi URL encoding", e);
        }
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] hashBytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo HMAC SHA512", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

