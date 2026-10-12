package com.arinno.canopus.reporting.application;

import java.util.List;
import java.util.Map;

import com.arinno.canopus.catalog.product.domain.Product;
import com.arinno.canopus.catalog.technology.domain.Technology;
import com.arinno.canopus.delivery.project.domain.Project;
import com.arinno.canopus.organization.user.domain.User;
import com.arinno.canopus.reporting.contract.ProductMetrics;
import com.arinno.canopus.reporting.contract.ProjectMetrics;
import com.arinno.canopus.reporting.contract.TechnologyMetrics;
import com.arinno.canopus.reporting.contract.UserMetrics;

public interface ReportingService {

    ProductMetrics productMetrics(Product product);

    ProductMetrics productSummaryMetrics(Product product);

    Map<Long, ProductMetrics> productSummaryMetricsByIds(List<Long> productIds);

    ProductMetrics productMetricsForUser(Product product, User user);

    Map<Long, ProductMetrics> productMetricsForUserByIds(List<Long> productIds, Long userId);

    ProjectMetrics projectMetrics(Project project);

    Map<Long, ProjectMetrics> projectMetricsByIds(List<Long> projectIds);

    TechnologyMetrics technologyMetrics(Technology technology);

    Map<Long, TechnologyMetrics> technologyMetricsByIds(List<Long> technologyIds);

    UserMetrics userMetrics(User user);

    Map<Long, UserMetrics> userMetricsByIds(List<Long> userIds);

    List<Map<String, Object>> findByProductAndCompany(Long productId, Long companyId);

    List<Map<String, Object>> findByProjectAndCompany(Long projectId, Long companyId);

    List<Product> findProductsByContributorAndCompany(Long userId, Long companyId);
}