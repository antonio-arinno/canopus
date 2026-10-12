package com.arinno.canopus.reporting.infrastructure.persistence;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import com.arinno.canopus.time.imputation.domain.Imputation;
import com.arinno.canopus.catalog.product.domain.Product;

public interface ReportingRepository extends Repository<Imputation, Long> {

    @Query("select distinct p from Product p left join fetch p.technology left join fetch p.responsible left join fetch p.backup left join Project project on p.id = project.product.id left join project.contributors contributor where (contributor.id = ?1 or p.responsible.id = ?1) and p.company.id = ?2")
    List<Product> findProductsByContributorAndCompany(Long userId, Long companyId);

    @Query("select sum(time) from ImputationItem where project.id = ?1")
    Integer timeByProject(Long projectId);

    @Query("select project.id, count(contributor.id) from Project project left join project.contributors contributor where project.id in ?1 group by project.id")
    List<Object[]> contributorCountsByProjectIds(List<Long> projectIds);

    @Query("select item.project.id, sum(item.time) from ImputationItem item where item.project.id in ?1 group by item.project.id")
    List<Object[]> timesByProjectIds(List<Long> projectIds);

    @Query("select project.product.id, count(project.id) from Project project where project.product.id in ?1 group by project.product.id")
    List<Object[]> projectCountsByProductIds(List<Long> productIds);

    @Query("select project.product.id, count(contributor.id) from Project project left join project.contributors contributor where project.product.id in ?1 group by project.product.id")
    List<Object[]> contributorCountsByProductIds(List<Long> productIds);

    @Query("select item.project.product.id, sum(item.time) from ImputationItem item where item.project.product.id in ?1 group by item.project.product.id")
    List<Object[]> timesByProductIds(List<Long> productIds);

    @Query("select imputationItem.project.product.id, sum(imputationItem.time) from Imputation imputation join imputation.items imputationItem where imputation.user.id = ?1 and imputationItem.project.product.id in ?2 group by imputationItem.project.product.id")
    List<Object[]> timesByProductAndUserIds(Long userId, List<Long> productIds);

    @Query("select product.technology.id, count(product.id) from Product product where product.technology.id in ?1 group by product.technology.id")
    List<Object[]> productCountsByTechnologyIds(List<Long> technologyIds);

    @Query("select project.product.technology.id, count(project.id) from Project project where project.product.technology.id in ?1 group by project.product.technology.id")
    List<Object[]> projectCountsByTechnologyIds(List<Long> technologyIds);

    @Query("select project.product.technology.id, count(contributor.id) from Project project left join project.contributors contributor where project.product.technology.id in ?1 group by project.product.technology.id")
    List<Object[]> contributorCountsByTechnologyIds(List<Long> technologyIds);

    @Query("select item.project.product.technology.id, sum(item.time) from ImputationItem item where item.project.product.technology.id in ?1 group by item.project.product.technology.id")
    List<Object[]> timesByTechnologyIds(List<Long> technologyIds);

    @Query("select product.responsible.id, count(product.id) from Product product where product.responsible.id in ?1 group by product.responsible.id")
    List<Object[]> productCountsByResponsibleUserIds(List<Long> userIds);

    @Query("select project.responsible.id, count(project.id) from Project project where project.responsible.id in ?1 group by project.responsible.id")
    List<Object[]> projectCountsByResponsibleUserIds(List<Long> userIds);

    @Query("select imputation.user.id, sum(item.time) from Imputation imputation join imputation.items item where imputation.user.id in ?1 group by imputation.user.id")
    List<Object[]> timesByUserIds(List<Long> userIds);

    @Query("select sum(item.time) from Imputation imputation join imputation.items item where imputation.user.id = ?1 and item.project.product.id = ?2")
    Integer timeByProductAndUser(Long userId, Long productId);

    @Query("select sum(time) from Product product left join Project project on product.id = project.product.id left join ImputationItem item on project.id = item.project.id where product.id = ?1")
    Integer timeByProduct(Long productId);

    @Query("select sum(time) from Technology technology left join Product product on technology.id = product.technology.id left join Project project on product.id = project.product.id left join ImputationItem item on project.id = item.project.id where technology.id = ?1")
    Integer timeByTechnology(Long technologyId);

    @NativeQuery("select projects.name as projectName, count(distinct users.id) as countContributors, sum(imputations_items.time) as time from imputations left join users on imputations.user_id = users.id left join imputations_items on imputations.id = imputations_items.imputation_id left join projects on imputations_items.project_id = projects.id left join products on projects.product_id = products.id where products.id = ?1 and products.company_id = ?2 group by project_id, product_id")
    List<Map<String, Object>> findByProductAndCompany(Long productId, Long companyId);

    @NativeQuery("select users.name as name, sum(imputations_items.time) as time from imputations left join users on imputations.user_id = users.id left join imputations_items on imputations.id = imputations_items.imputation_id left join projects on imputations_items.project_id = projects.id where imputations_items.project_id = ?1 and projects.company_id = ?2 group by users.id, users.name")
    List<Map<String, Object>> findByProjectAndCompany(Long projectId, Long companyId);

    @NativeQuery("select sum(time) / count(distinct project_id) from projects left join imputations_items on projects.id = imputations_items.project_id where product_id = ?1 and date_pro is not null")
    Float avgTimeByProduct(Long productId);

    @Query("select project.dateDev, project.datePro from Project project where project.product.id = ?1 and project.datePro is not null")
    List<Object[]> findCompletedProjectDates(Long productId);
}