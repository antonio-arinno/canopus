package com.arinno.canopus.reporting.application;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arinno.canopus.catalog.product.application.IProductService;
import com.arinno.canopus.catalog.product.domain.Product;
import com.arinno.canopus.catalog.technology.domain.Technology;
import com.arinno.canopus.delivery.project.application.IProjectService;
import com.arinno.canopus.delivery.project.domain.Project;
import com.arinno.canopus.organization.user.domain.User;
import com.arinno.canopus.reporting.contract.ProductMetrics;
import com.arinno.canopus.reporting.contract.ProjectMetrics;
import com.arinno.canopus.reporting.contract.TechnologyMetrics;
import com.arinno.canopus.reporting.contract.UserMetrics;
import com.arinno.canopus.reporting.infrastructure.persistence.ReportingRepository;
import com.arinno.canopus.time.imputation.application.IImputationService;

@Service
public class ReportingServiceImpl implements ReportingService {

    private final IProductService productService;
    private final IProjectService projectService;
    private final IImputationService imputationService;
    private final ReportingRepository reportingRepository;

    public ReportingServiceImpl(IProductService productService, IProjectService projectService,
            IImputationService imputationService, ReportingRepository reportingRepository) {
        this.productService = productService;
        this.projectService = projectService;
        this.imputationService = imputationService;
        this.reportingRepository = reportingRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductMetrics productMetrics(Product product) {
        return new ProductMetrics(
                projectService.countByProduct(product),
                projectService.countContributorsByProduct(product.getId()),
                zeroIfNull(reportingRepository.timeByProduct(product.getId())),
                reportingRepository.avgTimeByProduct(product.getId()),
                averageDuration(reportingRepository.findCompletedProjectDates(product.getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductMetrics productSummaryMetrics(Product product) {
        return new ProductMetrics(
                projectService.countByProduct(product),
                projectService.countContributorsByProduct(product.getId()),
                zeroIfNull(reportingRepository.timeByProduct(product.getId())),
                null,
                null);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, ProductMetrics> productSummaryMetricsByIds(List<Long> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Integer> projectCounts = toIntegerMap(reportingRepository.projectCountsByProductIds(productIds));
        Map<Long, Integer> contributorCounts = toIntegerMap(reportingRepository.contributorCountsByProductIds(productIds));
        Map<Long, Integer> times = toIntegerMap(reportingRepository.timesByProductIds(productIds));
        Map<Long, ProductMetrics> metricsByProduct = new HashMap<>();
        for (Long productId : productIds) {
            metricsByProduct.put(productId, new ProductMetrics(
                    projectCounts.getOrDefault(productId, 0),
                    contributorCounts.getOrDefault(productId, 0),
                    times.getOrDefault(productId, 0), null, null));
        }
        return metricsByProduct;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductMetrics productMetricsForUser(Product product, User user) {
        return new ProductMetrics(
                projectService.countByProduct(product),
                projectService.countContributorsByProduct(product.getId()),
                zeroIfNull(reportingRepository.timeByProductAndUser(user.getId(), product.getId())),
                null,
                null);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, ProductMetrics> productMetricsForUserByIds(List<Long> productIds, Long userId) {
        if (productIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Integer> projectCounts = toIntegerMap(reportingRepository.projectCountsByProductIds(productIds));
        Map<Long, Integer> contributorCounts = toIntegerMap(reportingRepository.contributorCountsByProductIds(productIds));
        Map<Long, Integer> times = toIntegerMap(reportingRepository.timesByProductAndUserIds(userId, productIds));
        Map<Long, ProductMetrics> metricsByProduct = new HashMap<>();
        for (Long productId : productIds) {
            metricsByProduct.put(productId, new ProductMetrics(
                    projectCounts.getOrDefault(productId, 0),
                    contributorCounts.getOrDefault(productId, 0),
                    times.getOrDefault(productId, 0), null, null));
        }
        return metricsByProduct;
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectMetrics projectMetrics(Project project) {
        return new ProjectMetrics(
                project.getContributors().size(),
                zeroIfNull(reportingRepository.timeByProject(project.getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, ProjectMetrics> projectMetricsByIds(List<Long> projectIds) {
        if (projectIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Integer> contributorCounts = new HashMap<>();
        for (Object[] result : reportingRepository.contributorCountsByProjectIds(projectIds)) {
            contributorCounts.put(((Number) result[0]).longValue(), ((Number) result[1]).intValue());
        }

        Map<Long, Integer> times = new HashMap<>();
        for (Object[] result : reportingRepository.timesByProjectIds(projectIds)) {
            times.put(((Number) result[0]).longValue(), ((Number) result[1]).intValue());
        }

        Map<Long, ProjectMetrics> metricsByProject = new HashMap<>();
        for (Long projectId : projectIds) {
            metricsByProject.put(projectId, new ProjectMetrics(
                    contributorCounts.getOrDefault(projectId, 0),
                    times.getOrDefault(projectId, 0)));
        }
        return metricsByProject;
    }

    @Override
    @Transactional(readOnly = true)
    public TechnologyMetrics technologyMetrics(Technology technology) {
        return new TechnologyMetrics(
                productService.countByTechnology(technology),
                projectService.countByTechnology(technology.getId()),
                projectService.countContributorsByTechnology(technology.getId()),
                zeroIfNull(reportingRepository.timeByTechnology(technology.getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, TechnologyMetrics> technologyMetricsByIds(List<Long> technologyIds) {
        if (technologyIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Integer> productCounts = toIntegerMap(reportingRepository.productCountsByTechnologyIds(technologyIds));
        Map<Long, Integer> projectCounts = toIntegerMap(reportingRepository.projectCountsByTechnologyIds(technologyIds));
        Map<Long, Integer> contributorCounts = toIntegerMap(reportingRepository.contributorCountsByTechnologyIds(technologyIds));
        Map<Long, Integer> times = toIntegerMap(reportingRepository.timesByTechnologyIds(technologyIds));
        Map<Long, TechnologyMetrics> metricsByTechnology = new HashMap<>();
        for (Long technologyId : technologyIds) {
            metricsByTechnology.put(technologyId, new TechnologyMetrics(
                    productCounts.getOrDefault(technologyId, 0),
                    projectCounts.getOrDefault(technologyId, 0),
                    contributorCounts.getOrDefault(technologyId, 0),
                    times.getOrDefault(technologyId, 0)));
        }
        return metricsByTechnology;
    }

    @Override
    @Transactional(readOnly = true)
    public UserMetrics userMetrics(User user) {
        return new UserMetrics(
                productService.countByResponsible(user),
                projectService.countByResponsible(user),
                imputationService.timeByUser(user));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, UserMetrics> userMetricsByIds(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Integer> productCounts = toIntegerMap(reportingRepository.productCountsByResponsibleUserIds(userIds));
        Map<Long, Integer> projectCounts = toIntegerMap(reportingRepository.projectCountsByResponsibleUserIds(userIds));
        Map<Long, Integer> times = toIntegerMap(reportingRepository.timesByUserIds(userIds));
        Map<Long, UserMetrics> metricsByUser = new HashMap<>();
        for (Long userId : userIds) {
            metricsByUser.put(userId, new UserMetrics(
                    productCounts.getOrDefault(userId, 0),
                    projectCounts.getOrDefault(userId, 0),
                    times.getOrDefault(userId, 0)));
        }
        return metricsByUser;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> findByProductAndCompany(Long productId, Long companyId) {
        return reportingRepository.findByProductAndCompany(productId, companyId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> findByProjectAndCompany(Long projectId, Long companyId) {
        return reportingRepository.findByProjectAndCompany(projectId, companyId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findProductsByContributorAndCompany(Long userId, Long companyId) {
        return reportingRepository.findProductsByContributorAndCompany(userId, companyId);
    }

    private Integer zeroIfNull(Integer value) {
        return value == null ? 0 : value;
    }

    private Map<Long, Integer> toIntegerMap(List<Object[]> rows) {
        Map<Long, Integer> values = new HashMap<>();
        for (Object[] row : rows) {
            values.put(((Number) row[0]).longValue(), ((Number) row[1]).intValue());
        }
        return values;
    }

    private Integer averageDuration(List<Object[]> completedProjectDates) {
        if (completedProjectDates.isEmpty()) {
            return null;
        }

        long totalDays = 0;
        for (Object[] dates : completedProjectDates) {
            LocalDate dateDev = (LocalDate) dates[0];
            LocalDate datePro = (LocalDate) dates[1];
            if (dateDev != null && datePro != null) {
                totalDays += ChronoUnit.DAYS.between(dateDev, datePro);
            }
        }
        return Math.toIntExact(totalDays / completedProjectDates.size());
    }
}