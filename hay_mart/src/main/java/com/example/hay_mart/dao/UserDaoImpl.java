package com.example.hay_mart.dao;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.example.hay_mart.dto.PageResponse;
import com.example.hay_mart.models.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Repository
public class UserDaoImpl implements UserDao {

    @Autowired
    private EntityManager entityManager;

    @Override
    public PageResponse<User> getAllKasir(String nama, int page, int size, String sortBy, String sortOrder) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);

        Predicate[] predicates = createPredicates(cb, root, nama);
        cq.where(predicates);

        if (sortBy != null && !sortBy.isBlank() && sortOrder != null && !sortOrder.isBlank()) {
            if (sortOrder.equalsIgnoreCase("asc")) {
                cq.orderBy(cb.asc(root.get(sortBy)));
            } else {
                cq.orderBy(cb.desc(root.get(sortBy)));
            }
        }

        List<User> result = entityManager.createQuery(cq)
                .setFirstResult((page - 1) * size)
                .setMaxResults(size)
                .getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<User> countRoot = countQuery.from(User.class);
        countQuery.select(cb.count(countRoot))
                .where(createPredicates(cb, countRoot, nama));

        Long totalItems = entityManager.createQuery(countQuery).getSingleResult();

        return PageResponse.success(result, page, size, totalItems);
    }

    private Predicate[] createPredicates(CriteriaBuilder cb, Root<User> root, String nama) {
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(root.get("role").get("roleName"), "KASIR"));

        if (nama != null && !nama.isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("nama")), "%" + nama.toLowerCase() + "%"));
        }

        return predicates.toArray(new Predicate[0]);
    }
}
