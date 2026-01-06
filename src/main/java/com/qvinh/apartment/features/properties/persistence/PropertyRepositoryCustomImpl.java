package com.qvinh.apartment.features.properties.persistence;

import com.qvinh.apartment.features.properties.domain.Property;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class PropertyRepositoryCustomImpl implements PropertyRepositoryCustom {

	private static final String ATTR_TYPE = "type";
	private static final String ATTR_AREA = "area";
	private static final String ATTR_SALE_INFO = "saleInfo";
	private static final String ATTR_GALLERIES = "galleries";
	private static final String ATTR_DETAILS = "details";
	private static final String ATTR_USER = "user";
	private static final String ATTR_FILE = "file";
	private static final String ATTR_DETAIL = "detail";
	private static final String ATTR_PROPERTY_ID = "propertyId";
	private static final String FETCHGRAPH_HINT = "jakarta.persistence.fetchgraph";

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public Page<Property> findAllWithRelations(Specification<Property> spec, Pageable pageable) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();

		// Count query
		CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
		Root<Property> countRoot = countQuery.from(Property.class);
		countQuery.select(cb.count(countRoot));
		if (spec != null) {
			Predicate predicate = spec.toPredicate(countRoot, countQuery, cb);
			if (predicate != null) {
				countQuery.where(predicate);
			}
		}
		Long total = entityManager.createQuery(countQuery).getSingleResult();

		// Data query with EntityGraph
		CriteriaQuery<Property> dataQuery = cb.createQuery(Property.class);
		Root<Property> dataRoot = dataQuery.from(Property.class);
		dataQuery.select(dataRoot);
		if (spec != null) {
			Predicate predicate = spec.toPredicate(dataRoot, dataQuery, cb);
			if (predicate != null) {
				dataQuery.where(predicate);
			}
		}

		// Apply sorting
		if (pageable.getSort().isSorted()) {
			dataQuery.orderBy(pageable.getSort().stream()
				.map(order -> order.isAscending()
					? cb.asc(dataRoot.get(order.getProperty()))
					: cb.desc(dataRoot.get(order.getProperty())))
				.toList());
		}

		TypedQuery<Property> typedQuery = entityManager.createQuery(dataQuery);

		// Create and apply EntityGraph
		EntityGraph<Property> graph = entityManager.createEntityGraph(Property.class);
		graph.addAttributeNodes(ATTR_TYPE, ATTR_AREA, ATTR_SALE_INFO, ATTR_GALLERIES, ATTR_DETAILS);
		graph.addSubgraph(ATTR_SALE_INFO).addAttributeNodes(ATTR_USER);
		graph.addSubgraph(ATTR_GALLERIES).addAttributeNodes(ATTR_FILE);
		graph.addSubgraph(ATTR_DETAILS).addAttributeNodes(ATTR_DETAIL);
		typedQuery.setHint(FETCHGRAPH_HINT, graph);

		// Apply pagination
		typedQuery.setFirstResult((int) pageable.getOffset());
		typedQuery.setMaxResults(pageable.getPageSize());

		List<Property> content = typedQuery.getResultList();
		return new PageImpl<>(content, pageable, total);
	}

	@Override
	public Property findByIdWithRelations(Long id) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Property> query = cb.createQuery(Property.class);
		Root<Property> root = query.from(Property.class);
		query.select(root).where(cb.equal(root.get(ATTR_PROPERTY_ID), id));

		TypedQuery<Property> typedQuery = entityManager.createQuery(query);

		// Create and apply EntityGraph
		EntityGraph<Property> graph = entityManager.createEntityGraph(Property.class);
		graph.addAttributeNodes(ATTR_TYPE, ATTR_AREA, ATTR_SALE_INFO, ATTR_GALLERIES, ATTR_DETAILS);
		graph.addSubgraph(ATTR_SALE_INFO).addAttributeNodes(ATTR_USER);
		graph.addSubgraph(ATTR_GALLERIES).addAttributeNodes(ATTR_FILE);
		graph.addSubgraph(ATTR_DETAILS).addAttributeNodes(ATTR_DETAIL);
		typedQuery.setHint(FETCHGRAPH_HINT, graph);

		List<Property> results = typedQuery.getResultList();
		return results.isEmpty() ? null : results.get(0);
	}
}
