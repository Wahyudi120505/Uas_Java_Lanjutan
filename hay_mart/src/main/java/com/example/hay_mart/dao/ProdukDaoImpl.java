package com.example.hay_mart.dao;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.example.hay_mart.dto.PageResponse;
import com.example.hay_mart.models.Kategori;
import com.example.hay_mart.models.Produk;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Repository
public class ProdukDaoImpl implements ProdukDao {

    @Autowired
    EntityManager entityManager;

    @Override
    public PageResponse<Produk> getAll(String nama, Kategori kategori, int page, int size,
            String sortBy, String sortOrder,
            Integer minPrice, Integer maxPrice) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Produk> criteriaQuery = criteriaBuilder.createQuery(Produk.class);
        Root<Produk> produkRoot = criteriaQuery.from(Produk.class);

        // Filter
        Predicate[] predicates = createPredicates(criteriaBuilder, produkRoot, nama, kategori, minPrice, maxPrice);
        criteriaQuery.where(predicates);

        // Sorting
        if (sortBy != null && !sortBy.isBlank() && sortOrder != null && !sortOrder.isBlank()) {
            if (sortOrder.equalsIgnoreCase("asc")) {
                criteriaQuery.orderBy(criteriaBuilder.asc(produkRoot.get(sortBy)));
            } else {
                criteriaQuery.orderBy(criteriaBuilder.desc(produkRoot.get(sortBy)));
            }
        }

        // Ambil data sesuai halaman
        List<Produk> showProduks = entityManager.createQuery(criteriaQuery)
                .setFirstResult((page - 1) * size)
                .setMaxResults(size)
                .getResultList();

        // Hitung total item untuk pagination
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<Produk> root = countQuery.from(Produk.class);
        countQuery.select(criteriaBuilder.count(root))
                .where(createPredicates(criteriaBuilder, root, nama, kategori, minPrice, maxPrice));

        Long totalItem = entityManager.createQuery(countQuery).getSingleResult();

        return PageResponse.success(showProduks, page, size, totalItem);
    }

    private Predicate[] createPredicates(CriteriaBuilder criteriaBuilder, Root<Produk> produkRoot,
            String nama, Kategori kategori,
            Integer minPrice, Integer maxPrice) {

        List<Predicate> predicates = new ArrayList<>();

        // Filter nama
        if (nama != null && !nama.isBlank()) {
            predicates.add(criteriaBuilder.like(produkRoot.get("nama"), "%" + nama + "%"));
        }

        // Filter harga
        if (minPrice != null && maxPrice != null) {
            predicates.add(criteriaBuilder.between(produkRoot.get("harga"), minPrice, maxPrice));
        } else if (minPrice != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(produkRoot.get("harga"), minPrice));
        } else if (maxPrice != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(produkRoot.get("harga"), maxPrice));
        }

        // Filter kategori
        if (kategori != null) {
            predicates.add(criteriaBuilder.equal(produkRoot.get("kategori"), kategori));
        }

        // Filter deleted == false (soft delete filter)
        predicates.add(criteriaBuilder.equal(produkRoot.get("deleted"), false));
        return predicates.toArray(new Predicate[0]);
    }
}
