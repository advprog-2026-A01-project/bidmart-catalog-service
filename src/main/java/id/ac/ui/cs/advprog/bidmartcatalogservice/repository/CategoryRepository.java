package id.ac.ui.cs.advprog.bidmartcatalogservice.repository;

import id.ac.ui.cs.advprog.bidmartcatalogservice.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    // ambil semua kategori root (tidak punya parent), untuk tampilan tree
    List<Category> findByParentIsNull();

    // ambil semua child dari satu parent
    List<Category> findByParentId(UUID parentId);

    boolean existsByNameAndParentId(String name, UUID parentId);
}
