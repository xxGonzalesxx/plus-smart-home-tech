package ru.yandex.practicum.shoppingstore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.shoppingstore.dto.ProductDto;
import ru.yandex.practicum.shoppingstore.dto.SetProductQuantityStateRequest;
import ru.yandex.practicum.shoppingstore.enums.ProductCategory;
import ru.yandex.practicum.shoppingstore.enums.ProductState;
import ru.yandex.practicum.shoppingstore.enums.QuantityState;
import ru.yandex.practicum.shoppingstore.exception.ProductNotFoundException;
import ru.yandex.practicum.shoppingstore.mapper.ProductMapper;
import ru.yandex.practicum.shoppingstore.model.Product;
import ru.yandex.practicum.shoppingstore.repository.ProductRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public Page<ProductDto> getProducts(ProductCategory category, Pageable pageable) {
        return productRepository.findByCategory(category, pageable)
                .map(productMapper::toDto);
    }

    public ProductDto getProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productId));
        return productMapper.toDto(product);
    }

    @Transactional
    public ProductDto createProduct(ProductDto productDto) {
        Product product = productMapper.toEntity(productDto);
        product.setState(ProductState.ACTIVE);
        product.setQuantityState(QuantityState.ENOUGH);
        Product saved = productRepository.save(product);
        log.info("Created product: {}", saved.getId());
        return productMapper.toDto(saved);
    }

    @Transactional
    public ProductDto updateProduct(ProductDto productDto) {
        Product existing = productRepository.findById(productDto.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productDto.getProductId()));

        existing.setName(productDto.getProductName());
        existing.setDescription(productDto.getDescription());
        existing.setImageSrc(productDto.getImageSrc());
        existing.setCategory(productDto.getProductCategory());
        existing.setPrice(productDto.getPrice());

        Product saved = productRepository.save(existing);
        log.info("Updated product: {}", saved.getId());
        return productMapper.toDto(saved);
    }

    @Transactional
    public boolean removeProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productId));
        product.setState(ProductState.DEACTIVATE);
        productRepository.save(product);
        log.info("Deactivated product: {}", productId);
        return true;
    }

    @Transactional
    public boolean setProductQuantityState(SetProductQuantityStateRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + request.getProductId()));
        product.setQuantityState(request.getQuantityState());
        productRepository.save(product);
        log.info("Updated quantity state for product: {} -> {}", request.getProductId(), request.getQuantityState());
        return true;
    }
}