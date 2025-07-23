package com.fortune.project.controller;

import com.fortune.project.constant.AppConstant;
import com.fortune.project.dto.response.common.ApiResponse;
import com.fortune.project.security.service.UserDetailsImpl;
import com.fortune.project.service.CartService;
import com.fortune.project.util.PaginationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;


    @PostMapping("carts/products/{productid}/quantity/{quantity}")
    public ResponseEntity<?> addToCart(
            @PathVariable(name = "productid") Long productId,
            @PathVariable Integer quantity,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return new ResponseEntity<ApiResponse<?>>(cartService.addToCart(productId, quantity, userDetails.getEmail()), HttpStatus.CREATED);
    }

    @GetMapping("/carts")
    public ResponseEntity<?> viewAllCart(
            @RequestParam(defaultValue = AppConstant.DEFAULT_PAGE + "", required = false) int page,
            @RequestParam(defaultValue = AppConstant.DEFAULT_SIZE + "", required = false) int size,
            @RequestParam(defaultValue = AppConstant.DEFAULT_SORT_DIR, required = false) String sortDir,
            @RequestParam(defaultValue = AppConstant.DEFAULT_SORT_BY_ID, required = false) String sortBy
    ) {
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, sortDir);
        ApiResponse<?> cartResponse = cartService.viewAllCart(pageable);
        return new ResponseEntity<>(cartResponse, HttpStatus.FOUND);
    }

    @GetMapping("/carts/users/cart")
    public ResponseEntity<?> viewCart(
            @RequestParam(defaultValue = AppConstant.DEFAULT_PAGE + "", required = false) int page,
            @RequestParam(defaultValue = AppConstant.DEFAULT_SIZE + "", required = false) int size,
            @RequestParam(defaultValue = AppConstant.DEFAULT_SORT_DIR, required = false) String sortDir,
            @RequestParam(defaultValue = AppConstant.DEFAULT_SORT_BY_ID, required = false) String sortBy,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, sortDir);
        ApiResponse<?> response = cartService.viewCart(userDetails.getId(), pageable);
        return new ResponseEntity<>(response, HttpStatus.FOUND);
    }
}
