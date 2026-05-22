package id.ac.ui.cs.advprog.bidmartcatalogservice.repository;

import id.ac.ui.cs.advprog.bidmartcatalogservice.model.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:repotest",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    private Category root;
    private Category child;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();

        root = categoryRepository.save(Category.builder()
                .name("Elektronik")
                .description("Kategori elektronik")
                .build());

        child = categoryRepository.save(Category.builder()
                .name("Handphone")
                .parent(root)
                .build());
    }

    @Test
    void findByParentIsNull_returnsOnlyRootCategories() {
        List<Category> roots = categoryRepository.findByParentIsNull();

        assertThat(roots).hasSize(1);
        assertThat(roots.get(0).getName()).isEqualTo("Elektronik");
    }

    @Test
    void findByParentId_returnsChildCategories() {
        List<Category> children = categoryRepository.findByParentId(root.getId());

        assertThat(children).hasSize(1);
        assertThat(children.get(0).getName()).isEqualTo("Handphone");
    }

    @Test
    void existsByNameAndParentId_whenExists_returnsTrue() {
        boolean exists = categoryRepository.existsByNameAndParentId("Handphone", root.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void existsByNameAndParentId_whenNotExists_returnsFalse() {
        boolean exists = categoryRepository.existsByNameAndParentId("Tablet", root.getId());

        assertThat(exists).isFalse();
    }

    @Test
    void existsByNameAndParentId_withWrongParent_returnsFalse() {
        boolean exists = categoryRepository.existsByNameAndParentId("Handphone", child.getId());

        assertThat(exists).isFalse();
    }
}
