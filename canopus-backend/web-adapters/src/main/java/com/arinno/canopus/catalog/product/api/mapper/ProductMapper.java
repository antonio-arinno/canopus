package com.arinno.canopus.catalog.product.api.mapper;



import org.springframework.stereotype.Component;

import com.arinno.canopus.catalog.product.domain.Product;
import com.arinno.canopus.catalog.product.contract.ProductResponse;
import com.arinno.canopus.catalog.technology.contract.TechnologyResponse;
import com.arinno.canopus.organization.user.domain.User;
import com.arinno.canopus.organization.user.contract.UserResponse;
import com.arinno.canopus.organization.user.api.mapper.UserMapper;
import com.arinno.canopus.catalog.technology.api.mapper.TechnologyMapper;
import com.arinno.canopus.reporting.contract.ProductMetrics;

@Component
public class ProductMapper {

    private final UserMapper userMapper;
    private final TechnologyMapper technologyMapper;

        public ProductMapper(UserMapper userMapper, TechnologyMapper technologyMapper) {
        this.userMapper = userMapper;
        this.technologyMapper = technologyMapper;
    }

    public ProductResponse toIdName(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .technology(technologyMapper.toIdName(product.getTechnology()))
                .build();
    }

        public ProductResponse toSummary(Product product, ProductMetrics metrics) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .technology(technologyMapper.toIdName(product.getTechnology()))
                .responsible(userMapper.toSummary(product.getResponsible()))
                .backup(userMapper.toSummary(product.getBackup()))
                .time(metrics.time())
                .build();
    }

        public ProductResponse toDetail(Product product, ProductMetrics metrics) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .technology(technologyMapper.toIdName(product.getTechnology()))
                .responsible(userMapper.toSummary(product.getResponsible()))
                .backup(userMapper.toSummary(product.getBackup()))
                .countProjects(metrics.countProjects())
                .countContributors(metrics.countContributors())
                .time(metrics.time())
                .avgTime(metrics.avgTime())
                .avgDuration(metrics.avgDuration())
                .build();
    }

        public ProductResponse toResponseForUser(Product product, User user, ProductMetrics metrics) {
        return ProductResponse.builder()
                .name(product.getName())
                .description(product.getDescription())
                .countProjects(metrics.countProjects())
                .countContributors(metrics.countContributors())
                .technology(TechnologyResponse.builder()
                        .name(product.getTechnology().getName())
                        .build())
                .responsible(UserResponse.builder()
                        .name(product.getResponsible().getName())
                        .build())
                .time(metrics.time())
                .build();
    }
}
