package com.arinno.canopus.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.entities.User;
import com.arinno.canopus.entities.UserListItem;


public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByUsername(String name);

    List<User> findByCompany(Company company);

    Page<User> findByCompany(Company company, Pageable pageable);

    @Query("""
            select new com.arinno.canopus.entities.UserListItem(
                u.id, u.username, u.name, u.lastname,
                (select count(p) from Product p where p.responsible = u),
                coalesce((select sum(item.time) from Imputation i join i.items item where i.user = u), 0)
            )
            from User u
            where u.company = :company
            """)
    Page<UserListItem> findListItemsByCompany(@Param("company") Company company, Pageable pageable);

    Optional<User> findByIdAndCompany(Long id, Company company);

    public List<User> findByNameContainingIgnoreCaseAndCompany(String term, Company company);

    @Query("select distinct u from User u join u.technologies t where t.id = ?1 and u.company.id = ?2")
    public List<User> findByTechnologyAndCompany(Long technologyId, Long companyId);

}
