package com.fortune.project.service.impl;

import com.fortune.project.dto.CartItemResponse;
import com.fortune.project.dto.CartResponse;
import com.fortune.project.dto.response.common.ApiResponse;
import com.fortune.project.dto.response.common.PagingResponse;
import com.fortune.project.entity.CartEntity;
import com.fortune.project.entity.CartItemEntity;
import com.fortune.project.entity.ProductEntity;
import com.fortune.project.exception.ApiException;
import com.fortune.project.exception.ResourceNotFoundException;
import com.fortune.project.repository.CartItemRepository;
import com.fortune.project.repository.CartRepository;
import com.fortune.project.repository.ProductRepository;
import com.fortune.project.service.CartService;
import com.fortune.project.util.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final AuthUtil authUtil;
    private final CartItemRepository cartItemRepository;

    @Override
    public ApiResponse<?> addToCart(Long productId, Integer quantity, String email) {
        //find Product
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "Product id", productId));
        if (product.getStock() < quantity) {
            throw new ApiException("Sufficient stock");
        }

        //find cart of user by Email
        CartEntity cart = cartRepository.findByUser_email(email)
                .orElseGet(() -> {
                    CartEntity c = new CartEntity();
                    c.setUser(authUtil.loggedInUser());
                    return cartRepository.save(c);
                });

        //add product to CartItem if existed or increase the quantity
        CartItemEntity cartItem = cart.getCartItems().stream()
                .filter(cartIT -> cartIT.getProduct().getId().equals(productId))
                .findFirst().orElseGet(() -> {
                    CartItemEntity newCartItem = new CartItemEntity();
                    newCartItem.setProduct(product);
                    newCartItem.setQuantity(quantity);
                    newCartItem.setSnapshotPrice(product.getSpecialPrice());
                    newCartItem.setCart(cart);
                    return newCartItem;
                });

        cartItem.setQuantity(cartItem.getQuantity() + quantity);
        cartItemRepository.save(cartItem);

        //update cart with new total price
        cart.setTotalPrice(cart.getTotalPrice() + (product.getPrice() * quantity));
        cartRepository.save(cart);

        //response to font-end
        cart.getCartItems().add(cartItem);
        List<CartItemResponse> cartItems = cart.getCartItems().stream()
                .map(cartItemEntity -> {
                    CartItemResponse cartItemResponse = new CartItemResponse();
                    cartItemResponse.setProductId(cartItemEntity.getProduct().getId());
                    cartItemResponse.setName(cartItemEntity.getProduct().getName());
                    cartItemResponse.setImageUrl(cartItemEntity.getProduct().getImg());
                    cartItemResponse.setSnapshotPrice(cartItemEntity.getSnapshotPrice());
                    cartItemResponse.setQuantity(cartItemEntity.getQuantity());
                    cartItemResponse.setSubTotal(cartItemEntity.getSnapshotPrice() * cartItemEntity.getQuantity());
                    return cartItemResponse;
                }).toList();

        Double totalPrice = cartItems.stream()
                .mapToDouble(CartItemResponse::getSubTotal)
                .sum();

        CartResponse<List<CartItemResponse>> cartResponse = new CartResponse<>(
                cart.getId(),
                cart.getUser().getId(),
                cartItems,
                totalPrice
        );
        return new ApiResponse<>("Product added success", cartResponse, LocalDateTime.now());
    }

    @Override
    public ApiResponse<?> viewCart(Long id, Pageable pageable) {
        CartEntity cart = cartRepository.findByUser_id(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "user id", id));

        Page<CartItemEntity> pagedCartItems = cartItemRepository.findByCart_id(cart.getId(), pageable);

        List<CartItemResponse> cartItemResponses = pagedCartItems.getContent().stream()
                .map(cartItem -> {
                    ProductEntity product = cartItem.getProduct();
                    boolean deleted = (product == null);
                    Double currentPrice = deleted ? cartItem.getSnapshotPrice() :product.getSpecialPrice();
                    boolean priceChanged = !deleted && !cartItem.getSnapshotPrice().equals(currentPrice);

                    Double subTotal = cartItem.getSnapshotPrice() * cartItem.getQuantity();

                    return new CartItemResponse(
                            cartItem.getId(),
                            deleted ? cartItem.getSnapshotName() + " (Deleted)" : product.getName(),
                            deleted ? "/img/default.png" : product.getImg(),
                            cartItem.getSnapshotPrice(),
                            cartItem.getQuantity(),
                            null,
                            subTotal,
                            priceChanged,
                            deleted
                    );
                }).toList();

        Double totalPrice = cart.getCartItems().stream()
                .mapToDouble(item -> item.getSnapshotPrice() * item.getQuantity())
                .sum();

        Page<CartItemResponse> pageResult = new PageImpl<>(
                cartItemResponses,
                pageable,
                pagedCartItems.getTotalElements()
        );
        PagingResponse<CartItemResponse> pagingResponse = new PagingResponse<>(pageResult);

        CartResponse<PagingResponse<CartItemResponse>> cartResponse = new CartResponse<>(
                cart.getId(),
                cart.getUser().getId(),
                pagingResponse,
                totalPrice
        );
        return new ApiResponse<>("Fetched cart successfully", cartResponse, LocalDateTime.now());
    }

    @Override
    public ApiResponse<?> viewAllCart(Pageable pageable) {
        Page<CartEntity> carts = cartRepository.findAll(pageable);

        List<CartResponse<List<CartItemResponse>>> cartResponses = carts.getContent().stream()
                .map(cart -> {
                    List<CartItemResponse> items = cart.getCartItems().stream()
                            .map(cartItem -> {
                                ProductEntity product = cartItem.getProduct();
                                Boolean deleted = (product == null);
                                Double currentPrice = deleted ? cartItem.getSnapshotPrice() : product.getSpecialPrice();
                                boolean priceChanged = !deleted && !cartItem.getSnapshotPrice().equals(currentPrice);

                                Double subTotal = cartItem.getSnapshotPrice() * cartItem.getQuantity();

                                return new CartItemResponse(
                                        cartItem.getId(),
                                        deleted ? cartItem.getSnapshotName() : product.getName(),
                                        deleted ? "/img/default.png" : product.getImg(),
                                        cartItem.getSnapshotPrice(),
                                        cartItem.getQuantity(),
                                        null,
                                        subTotal,
                                        priceChanged,
                                        deleted
                                );
                            }).toList();

                    Double totalPrice = items.stream()
                            .mapToDouble(CartItemResponse::getSubTotal)
                            .sum();

                    return new CartResponse<>(
                            cart.getId(),
                            cart.getUser().getId(),
                            items,
                            totalPrice
                    );
                }).toList();

        Page<CartResponse<List<CartItemResponse>>> pageResult = new PageImpl<>(
                cartResponses,
                pageable,
                carts.getTotalElements()
        );

        PagingResponse<CartResponse<List<CartItemResponse>>> pagingResponse = new PagingResponse<>(pageResult);

        return new ApiResponse<>("Fetched all cart successfully", pagingResponse, LocalDateTime.now());
    }

}
