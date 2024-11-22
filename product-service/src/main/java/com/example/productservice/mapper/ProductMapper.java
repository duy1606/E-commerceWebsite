package com.example.productservice.mapper;

import com.example.productservice.dto.request.ProductCreateRequest;
import com.example.productservice.dto.request.ProductUpdateRequest;
import com.example.productservice.dto.request.SellProductRequest;
import com.example.productservice.dto.response.ProductResponse;
import com.example.productservice.entity.Product;
import com.example.productservice.enums.Size;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "productType",ignore = true)
    Product toProduct(ProductCreateRequest request);
    ProductResponse toProductResponse(Product product);
    @Mapping(target = "productType",ignore = true)
    void updateProduct(ProductUpdateRequest request,@MappingTarget Product product);

}
