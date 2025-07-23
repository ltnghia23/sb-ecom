package com.fortune.project.service.impl;

import com.fortune.project.constant.ProductConstant;
import com.fortune.project.dto.request.order.OrderItemRequest;
import com.fortune.project.dto.request.product.ProductRequest;
import com.fortune.project.dto.response.common.ApiResponse;
import com.fortune.project.dto.response.common.PagingResponse;
import com.fortune.project.dto.response.product.ProductResponse;
import com.fortune.project.entity.CategoryEntity;
import com.fortune.project.entity.OrderEntity;
import com.fortune.project.entity.OrderItemEntity;
import com.fortune.project.entity.ProductEntity;
import com.fortune.project.exception.ApiException;
import com.fortune.project.exception.ResourceNotFoundException;
import com.fortune.project.repository.CategoryRepository;
import com.fortune.project.repository.ProductRepository;
import com.fortune.project.service.ProductService;
import com.fortune.project.service.filestorage.FileStorageService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final CategoryRepository categoryRepository;
    @Value("${app.file.upload-dir}")
    private String path;
    private final FileStorageService fileStorageService;

    public ProductServiceImpl(ProductRepository repo, ModelMapper modelMapper, CategoryRepository categoryRepository, FileStorageService fileStorageService) {
        this.modelMapper = modelMapper;
        this.productRepository = repo;
        this.categoryRepository = categoryRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public ApiResponse<ProductResponse> createProduct(Long categoryId, ProductRequest request) {
        CategoryEntity category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category", "categoryId", categoryId
                ));
        boolean productExist = category.getProducts()
                .stream()
                .anyMatch(p -> p.getName().equals(request.getProductName()));

        if (productExist) {
            throw new ApiException("Product with name " + request.getProductName() + " is already exist!", "Product existed!");
        }
        ProductEntity productToCreated = modelMapper.map(request, ProductEntity.class);
        productToCreated.setImg(ProductConstant.DEFAULT_IMAGE);
        productToCreated.setCategory(category);
        productToCreated.setSpecialPrice(
                request.getProductPrice() - (request.getProductDiscount() * 0.01) * request.getProductPrice());
        ProductEntity createdProduct = productRepository.save(productToCreated);
        return new ApiResponse<>("Product created",
                modelMapper.map(createdProduct, ProductResponse.class), LocalDateTime.now()
        );

    }

    @Override
    public ApiResponse<PagingResponse<ProductResponse>> getAllProducts(Pageable pageable) {
        Page<ProductEntity> products = productRepository.findAll(pageable);
        Page<ProductResponse> productResponses = products.map(p -> modelMapper.map(p, ProductResponse.class));
        return new ApiResponse<>("All products fetched successfully",
                new PagingResponse<>(productResponses), LocalDateTime.now());
    }

    @Override
    public ApiResponse<PagingResponse<ProductResponse>> getAllProductsByCategoryId(Long categoryId, Pageable pageable) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category", "categoryId", categoryId);
        }
        Page<ProductEntity> products = productRepository.findByCategory_id(categoryId, pageable);
        Page<ProductResponse> responses = products
                .map(p -> modelMapper.map(p, ProductResponse.class));
        return new ApiResponse<>("All products with categoryId = " + categoryId + " fetched successfully",
                new PagingResponse<>(responses),
                LocalDateTime.now());
    }

    @Override
    public ApiResponse<PagingResponse<ProductResponse>> getAllProductsByKeyword(String keyword, Pageable pageable) {
        Page<ProductEntity> products = productRepository.findByNameContainingIgnoreCase(keyword, pageable);
        Page<ProductResponse> responses = products
                .map(p -> modelMapper.map(p, ProductResponse.class));
        return new ApiResponse<>("All products with keyword = " + keyword + " fetched successfully",
                new PagingResponse<>(responses),
                LocalDateTime.now());
    }

    @Override
    public ApiResponse<ProductResponse> updateProductByProductId(Long productId, ProductRequest request) {
        ProductEntity productFound = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product", "productId", productId
                ));
        productFound.setName(request.getProductName());
        productFound.setDescription(request.getProductDescription());
        productFound.setStock(request.getStock());
        productFound.setPrice(request.getProductPrice());
        productFound.setSpecialPrice(request.getProductPrice() - (request.getProductDiscount() * 0.01) * request.getProductPrice());
        ProductEntity updatedProduct = productRepository.save(productFound);
        ProductResponse responses = modelMapper.map(updatedProduct, ProductResponse.class);
        return new ApiResponse<>("Product with productId = " + productId + " updated successfully",
                responses,
                LocalDateTime.now());
    }

    @Override
    public ApiResponse<Void> deleteProductById(Long productId) {
        ProductEntity foundedProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        productRepository.delete(foundedProduct);
        return new ApiResponse<>("Product with productId = " + productId + " deleted successfully",
                null,
                LocalDateTime.now());
    }

    @Override
    public ApiResponse<ProductResponse> updateProductImage(long productId, MultipartFile image) throws IOException {
        ProductEntity foundedProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        String fileName = fileStorageService.uploadImage(path, image);
        String fullImageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/images/")
                .path(fileName)
                .toUriString();

        foundedProduct.setImg(fullImageUrl);

        ProductEntity updatedProduct = productRepository.save(foundedProduct);
        ProductResponse responses = modelMapper.map(updatedProduct, ProductResponse.class);
        return new ApiResponse<>("Product image updated successfully", responses, LocalDateTime.now());
    }

    @Override
    public ProductEntity findById(Long productId) {
        return productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product","product id", productId));
    }

    @Override
    public List<OrderItemEntity> buildOrderItems(List<OrderItemRequest> itemRequests, OrderEntity order) {
        List<OrderItemEntity> orderItems = new ArrayList<>();

        for (OrderItemRequest request : itemRequests) {
            // Tìm sản phẩm
            ProductEntity product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", request.getProductId()));

            // Validate số lượng
            if (request.getQuantity() <= 0) {
                throw new IllegalArgumentException("Số lượng phải > 0 cho sản phẩm: " + product.getName());
            }

            // Tạo OrderItemEntity
            OrderItemEntity orderItem = new OrderItemEntity();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(request.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setTotalPrice(product.getPrice() * request.getQuantity());

            orderItems.add(orderItem);
        }

        return orderItems;
    }


}
