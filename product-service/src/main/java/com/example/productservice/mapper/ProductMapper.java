package com.example.productservice.mapper;

import com.example.dtocommon.kafka.Order_Product.InventoryCheckEvent;
import com.example.productservice.dto.request.ProductCreateRequest;
import com.example.productservice.dto.request.ProductUpdateRequest;
import com.example.productservice.dto.request.SellProductRequest;
import com.example.productservice.dto.response.ProductResponse;
import com.example.productservice.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product toProduct(ProductCreateRequest request);

    ProductResponse toProductResponse(Product product);

    void updateProduct(ProductUpdateRequest request, @MappingTarget Product product);

    SellProductRequest toSellProductRequest(InventoryCheckEvent inventoryCheckEvent);

}
