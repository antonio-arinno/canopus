package com.arinno.canopus.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.arinno.canopus.organization.company.domain.Company;
import com.arinno.canopus.entities.Imputation;
import com.arinno.canopus.entities.ImputationItem;
import com.arinno.canopus.entities.Product;
import com.arinno.canopus.entities.Project;
import com.arinno.canopus.entities.Technology;
import com.arinno.canopus.entities.User;
import com.arinno.canopus.organization.company.infrastructure.persistence.CompanyRepository;
import com.arinno.canopus.repositories.ImputationRepository;
import com.arinno.canopus.repositories.ProductRepository;
import com.arinno.canopus.repositories.ProjectRepository;
import com.arinno.canopus.repositories.TechnologyRepository;
import com.arinno.canopus.repositories.UserRepository;

// Verifies that repository queries never leak data across companies (tenant isolation).
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@EntityScan(basePackages = "com.arinno.canopus")
@EnableJpaRepositories(basePackages = "com.arinno.canopus")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:canopus_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class CompanyIsolationRepositoryTests {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TechnologyRepository technologyRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ImputationRepository imputationRepository;

    private User newUser(Company company, String username) {
        User user = new User();
        user.setName("Name");
        user.setLastname("Lastname");
        user.setEmail(username + "@example.com");
        user.setUsername(username);
        user.setPassword("secret123");
        user.setCompany(company);
        return userRepository.save(user);
    }

    private Technology newTechnology(Company company, User responsible, String name) {
        Technology technology = new Technology();
        technology.setName(name);
        technology.setResponsible(responsible);
        technology.setCompany(company);
        return technologyRepository.save(technology);
    }

    private Product newProduct(Company company, Technology technology, User responsible, User backup, String name) {
        Product product = new Product();
        product.setName(name);
        product.setDescription("desc");
        product.setTechnology(technology);
        product.setResponsible(responsible);
        product.setBackup(backup);
        product.setCompany(company);
        return productRepository.save(product);
    }

    private Project newProject(Company company, Product product, User responsible, String name) {
        Project project = new Project();
        project.setName(name);
        project.setDescription("desc");
        project.setProduct(product);
        project.setResponsible(responsible);
        project.setContributors(new ArrayList<>());
        project.setCompany(company);
        return projectRepository.save(project);
    }

    private Imputation newImputation(User user, Project project) {
        ImputationItem item = new ImputationItem();
        item.setProject(project);
        item.setTime(8);

        Imputation imputation = new Imputation();
        imputation.setUser(user);
        imputation.setDate(new Date());
        imputation.setItems(List.of(item));
        return imputationRepository.save(imputation);
    }

    @Test
    void findByTechnologyAndCompany_doesNotReturnProductsFromOtherCompanies() {
        Company companyA = companyRepository.save(companyOf("Company A"));
        Company companyB = companyRepository.save(companyOf("Company B"));

        User userA = newUser(companyA, "userA");
        User userB = newUser(companyB, "userB");

        Technology technologyA = newTechnology(companyA, userA, "Java");
        Technology technologyB = newTechnology(companyB, userB, "Java");

        newProduct(companyA, technologyA, userA, userA, "Product A");
        newProduct(companyB, technologyB, userB, userB, "Product B");

        List<Product> resultForCompanyA = productRepository.findByTechnologyAndCompany(technologyA, companyA);

        assertThat(resultForCompanyA).extracting(Product::getName).containsExactly("Product A");
    }

    @Test
    void findByTechnologyAndCompany_doesNotReturnUsersFromOtherCompanies() {
        Company companyA = companyRepository.save(companyOf("Company A"));
        Company companyB = companyRepository.save(companyOf("Company B"));

        User userA = newUser(companyA, "userA");
        User userB = newUser(companyB, "userB");

        Technology technologyA = newTechnology(companyA, userA, "Java");
        Technology technologyB = newTechnology(companyB, userB, "Java");

        userA.setTechnologies(List.of(technologyA));
        userRepository.save(userA);
        userB.setTechnologies(List.of(technologyB));
        userRepository.save(userB);

        List<User> resultForCompanyA = userRepository.findByTechnologyAndCompany(technologyA.getId(), companyA.getId());

        assertThat(resultForCompanyA).extracting(User::getUsername).containsExactly("userA");
    }

    @Test
    void findByProductAndCompany_doesNotReturnProjectsFromOtherCompanies() {
        Company companyA = companyRepository.save(companyOf("Company A"));
        Company companyB = companyRepository.save(companyOf("Company B"));

        User userA = newUser(companyA, "userA");
        User userB = newUser(companyB, "userB");

        Technology technologyA = newTechnology(companyA, userA, "Java");
        Technology technologyB = newTechnology(companyB, userB, "Java");

        Product productA = newProduct(companyA, technologyA, userA, userA, "Product A");
        Product productB = newProduct(companyB, technologyB, userB, userB, "Product B");

        newProject(companyA, productA, userA, "Project A");
        newProject(companyB, productB, userB, "Project B");

        List<Project> resultForCompanyA = projectRepository.findByProductAndCompany(productA, companyA);

        assertThat(resultForCompanyA).extracting(Project::getName).containsExactly("Project A");
    }

    @Test
    void imputationAggregates_doNotReturnDataFromAnotherCompany() {
        Company companyA = companyRepository.save(companyOf("Company A"));
        Company companyB = companyRepository.save(companyOf("Company B"));
        User userA = newUser(companyA, "userA");
        User userB = newUser(companyB, "userB");
        Technology technologyA = newTechnology(companyA, userA, "Java");
        Technology technologyB = newTechnology(companyB, userB, "Java");
        Product productA = newProduct(companyA, technologyA, userA, userA, "Product A");
        Product productB = newProduct(companyB, technologyB, userB, userB, "Product B");
        Project projectA = newProject(companyA, productA, userA, "Project A");
        Project projectB = newProject(companyB, productB, userB, "Project B");
        newImputation(userA, projectA);
        newImputation(userB, projectB);

        List<Map<String, Object>> productResult = imputationRepository.findByProductAndCompany(productB.getId(), companyA.getId());
        List<Map<String, Object>> projectResult = imputationRepository.findByProjectAndCompany(projectB.getId(), companyA.getId());

        assertThat(productResult).isEmpty();
        assertThat(projectResult).isEmpty();
    }

    @Test
    void responsibleQueries_doNotReturnRecordsOutsideTheRequestedCompany() {
        Company companyA = companyRepository.save(companyOf("Company A"));
        Company companyB = companyRepository.save(companyOf("Company B"));
        User userA = newUser(companyA, "userA");
        User userB = newUser(companyB, "userB");
        Technology technologyA = newTechnology(companyA, userA, "Java");
        Technology technologyB = newTechnology(companyB, userB, "Java");
        Product productA = newProduct(companyA, technologyA, userA, userA, "Product A");
        Product foreignProduct = newProduct(companyB, technologyB, userA, userB, "Product B");
        newProject(companyA, productA, userA, "Project A");
        newProject(companyB, foreignProduct, userA, "Project B");

        List<Product> products = productRepository.findByResponsibleAndCompany(userA, companyA);
        List<Project> projects = projectRepository.findByResponsibleAndCompany(userA, companyA);
        List<Project> openProjects = projectRepository.findByResponsibleAndCompanyAndDateProIsNull(userA, companyA);

        assertThat(products).extracting(Product::getName).containsExactly("Product A");
        assertThat(projects).extracting(Project::getName).containsExactly("Project A");
        assertThat(openProjects).extracting(Project::getName).containsExactly("Project A");
    }

    private Company companyOf(String name) {
        Company company = new Company();
        company.setName(name);
        company.setDescription("desc");
        return company;
    }

}
