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
    public PageResponse<Produk> getAll(String nama, Kategori kategori, int page, int size, String sortBy,
            Integer minPrice, Integer maxPrice) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Produk> criteriaQuery = criteriaBuilder.createQuery(Produk.class);
        Root<Produk> produkRoot = criteriaQuery.from(Produk.class);

        Predicate[] predicates = createPredicates(criteriaBuilder, produkRoot, nama, kategori, minPrice, maxPrice);
        criteriaQuery.where(predicates);

        if (sortBy != null && !sortBy.isBlank()) {
            if (sortBy.equalsIgnoreCase("asc")) {
                criteriaQuery.orderBy(criteriaBuilder.asc(produkRoot.get(sortBy)));
            }
            else {
                criteriaQuery.orderBy(criteriaBuilder.desc(produkRoot.get(sortBy)));
            }
        }

        List<Produk> showProduks = entityManager.createQuery(criteriaQuery)
            .setFirstResult((page - 1) * size)
            .setMaxResults(size)
            .getResultList();
        
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<Produk> root = countQuery.from(Produk.class);
        countQuery.select(criteriaBuilder.count(root))
            .where(createPredicates(criteriaBuilder, produkRoot, nama, kategori, minPrice, maxPrice));

        Long totalItem = entityManager.createQuery(countQuery).getSingleResult();

        return PageResponse.success(showProduks, page, size, totalItem);
    }

    private Predicate[] createPredicates(CriteriaBuilder criteriaBuilder, Root<Produk> produkRoot, String nama,
            Kategori kategori, Integer minPrice, Integer maxPrice) {

                List<Predicate> predicates = new ArrayList<>();

                if (nama != null && !nama.isEmpty() && !nama.isBlank()) {
                    predicates.add(criteriaBuilder.like(produkRoot.get("nama"), "%" + nama + "%"));
                }
                if (minPrice != null && maxPrice != null) {
                    predicates.add(criteriaBuilder.between(produkRoot.get("harga"), maxPrice, maxPrice));
                }
                if (kategori != null) {
                    predicates.add(criteriaBuilder.equal(produkRoot.get("kategori"), kategori));
                }

        return predicates.toArray(new Predicate[0]);
    }
}
