package com.example.productservice.service;

import com.example.productservice.dto.request.ProcureProductRequest;
import com.example.productservice.dto.request.ProductCreateRequest;
import com.example.productservice.dto.request.ProductUpdateRequest;
import com.example.productservice.dto.request.SellProductRequest;
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
    MongoOperations mongoOperations;
    MongoClient mongoClient;

    public ProductResponse createProduct(ProductCreateRequest request) {
        Product product = productMapper.toProduct(request);
        ProductType productType = productTypeRepository.findById(request.getProductType()).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        product.setProductType(productType);
        return productMapper.toProductResponse(productRepository.save(product));
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream().map(productMapper::toProductResponse).toList();
    }

    public ProductResponse updateProduct(String id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        productMapper.updateProduct(request, product);
        product.setProductType(productTypeRepository.findById(request.getProductType()).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND)));
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
        Variant variant = product.getVariants().stream().filter(v->v.getSize().getName().equals(request.getSize().getName())&&v.getColor().equals(request.getColor())).findFirst().get();
        variant.setInventoryQuantity(request.getQuantity()+variant.getInventoryQuantity());
        return productMapper.toProductResponse(productRepository.save(product));
    }
    public ProductResponse sellProduct(SellProductRequest request) {
        try (ClientSession session = mongoClient.startSession()) { // Use the injected MongoClient to create a session
            session.startTransaction();
            try {
                MongoOperations sessionMongoTemplate = mongoOperations.withSession(session);
                Product product = sessionMongoTemplate.findById(request.getId(), Product.class);
                if (product== null) {
                    throw new AppException(ErrorCode.NOT_FOUND);
                }
                Variant variant = product.getVariants().stream().filter(v->v.getSize().getName().equals(request.getSize().getName())).findFirst().get();
                if (variant.getInventoryQuantity() < request.getQuantity()) {
                    throw new AppException(ErrorCode.OUT_OF_STOCK);
                }
                variant.setInventoryQuantity(variant.getInventoryQuantity() - request.getQuantity());
                sessionMongoTemplate.save(product);
                session.commitTransaction();
                log.info("Sell successfully");
                return productMapper.toProductResponse(product);
            } catch (Exception e) {
                session.abortTransaction();
                throw new AppException(ErrorCode.OUT_OF_STOCK);
            }
        }
    }
}
