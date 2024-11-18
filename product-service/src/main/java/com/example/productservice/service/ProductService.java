package com.example.productservice.service;

import com.example.productservice.dto.request.ProcureProductRequest;
import com.example.productservice.dto.request.ProductCreateRequest;
import com.example.productservice.dto.request.ProductUpdateRequest;
import com.example.productservice.dto.request.SellProductRequest;
import com.example.productservice.dto.response.ApiResponse;
import com.example.productservice.dto.response.ProductResponse;
import com.example.productservice.entity.Product;
import com.example.productservice.entity.ProductType;
import com.example.productservice.entity.Variant;
import com.example.productservice.enums.ErrorCode;
import com.example.productservice.exception.AppException;
import com.example.productservice.mapper.ProductMapper;
import com.example.productservice.repository.ProductRepository;
import com.example.productservice.repository.ProductTypeRepository;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    ProductRepository productRepository;
    ProductMapper productMapper;
    ProductTypeRepository productTypeRepository;

    public ProductResponse createProduct(ProductCreateRequest request) {
        Product product = productMapper.toProduct(request);
        ProductType productType = productTypeRepository.findById(request.getProductType()).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        product.setProductType(productType.getId());
        return productMapper.toProductResponse(productRepository.save(product));
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream().map(productMapper::toProductResponse).toList();
    }

    public ProductResponse updateProduct(String id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        productMapper.updateProduct(request, product);
        var productType1 = productTypeRepository.findById(request.getProductType()).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        product.setProductType(productType1.getId());
        return productMapper.toProductResponse(productRepository.save(product));
    }

    public void deleteProduct(String id) {
        productRepository.deleteById(id);
    }

    public ProductResponse getProductById(String id) {
        return productMapper.toProductResponse(productRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND)));
    }

    public ProductResponse procureProduct(ProcureProductRequest request) {
        Product product = productRepository.findById(request.getId()).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        Variant variant = product.getVariants().stream().filter(v -> v.getSize().getName().equals(request.getSize().getName()) && v.getColor().equals(request.getColor())).findFirst().get();
        variant.setInventoryQuantity(request.getQuantity() + variant.getInventoryQuantity());
        return productMapper.toProductResponse(productRepository.save(product));
    }

    public ApiResponse<?> sellProduct(SellProductRequest request) {
        // Tìm sản phẩm theo ID
        Product product = productRepository.findById(request.getProductID()).orElse(null);
        if (product == null) {
            return ApiResponse.builder()
                    .code(404) // Not Found
                    .result("Product not found")
                    .build();
        }

        // Tìm variant theo size và màu sắc
        Variant variant = product.getVariants().stream()
                .filter(v -> v.getSize().getName().equals(request.getSize().getName()) &&
                        v.getColor().equals(request.getColor()))
                .findFirst()
                .orElse(null);

        if (variant == null) {
            return ApiResponse.builder()
                    .code(404) // Not Found
                    .result("Variant not found")
                    .build();
        }

        // Kiểm tra tồn kho
        if (variant.getInventoryQuantity() < request.getQuantity()) {
            return ApiResponse.builder()
                    .code(400) // Bad Request
                    .result("Out of stock")
                    .build();
        }

        // Giảm số lượng tồn kho
        variant.setInventoryQuantity(variant.getInventoryQuantity() - request.getQuantity());

        // Lưu lại sản phẩm
        product = productRepository.save(product);

        return ApiResponse.builder()
                .result(productMapper.toProductResponse(product))
                .build();
    }

    public ApiResponse<?> rollbackSellProduct(SellProductRequest request) {
        // Tìm sản phẩm theo ID
        Product product = productRepository.findById(request.getProductID()).orElse(null);
        if (product == null) {
            return ApiResponse.builder()
                    .code(404) // Not Found
                    .result("Product not found for ID: " + request.getProductID())
                    .build();
        }

        // Tìm variant theo size và màu sắc
        Variant variant = product.getVariants().stream()
                .filter(v -> v.getSize().getName().equals(request.getSize().getName()) &&
                        v.getColor().equals(request.getColor()))
                .findFirst()
                .orElse(null);

        if (variant == null) {
            return ApiResponse.builder()
                    .code(404) // Not Found
                    .result("Variant not found for size: " + request.getSize().getName() +
                            " and color: " + request.getColor())
                    .build();
        }

        // Khôi phục số lượng tồn kho
        variant.setInventoryQuantity(variant.getInventoryQuantity() + request.getQuantity());

        // Lưu lại sản phẩm
        productRepository.save(product);

        return ApiResponse.builder()
                .code(1000) // Success
                .result("Rollback successful for product ID: " + request.getProductID())
                .build();
    }




}
