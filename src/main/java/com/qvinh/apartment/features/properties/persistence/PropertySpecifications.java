package com.qvinh.apartment.features.properties.persistence;

import com.qvinh.apartment.features.properties.dto.property.PropertyDetailFilterReq;
import com.qvinh.apartment.features.properties.dto.property.PropertySearchReq;
import com.qvinh.apartment.features.properties.domain.Property;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class PropertySpecifications {

	private static final String FIELD_DETAIL = "detail";
	private static final String FIELD_ID = "id";
	private static final String FIELD_DETAIL_ID = "detailId";
	private static final String FIELD_PROPERTY_ID = "propertyId";
	private static final String FIELD_IS_NUMBER = "isNumber";
	private static final String FIELD_VALUE = "value";

	private PropertySpecifications() {
	}

	public static Specification<Property> bySearchReq(PropertySearchReq req) {
		if (req == null) {
			return (root, query, cb) -> cb.conjunction();
		}

		Specification<Property> spec = (root, query, cb) -> cb.conjunction();
		spec = applyTextFilters(req, spec);
		spec = applyListFilters(req, spec);
		spec = applyFlagFilters(req, spec);
		spec = applyPriceFilters(req, spec);
		spec = applyDateFilters(req, spec);
		spec = applyDetailFilters(req, spec);
		return spec;
	}

	private static Specification<Property> applyTextFilters(PropertySearchReq req, Specification<Property> spec) {
		if (req.getQ() != null && !req.getQ().trim().isEmpty()) {
			spec = spec.and(byKeyword(req.getQ().trim()));
		}
		if (req.getTitle() != null && !req.getTitle().trim().isEmpty()) {
			spec = spec.and(byTitle(req.getTitle().trim()));
		}
		if (req.getDescription() != null && !req.getDescription().trim().isEmpty()) {
			spec = spec.and(byDescription(req.getDescription().trim()));
		}
		return spec;
	}

	private static Specification<Property> applyListFilters(PropertySearchReq req, Specification<Property> spec) {
		if (req.getTypeIds() != null && !req.getTypeIds().isEmpty()) {
			spec = spec.and(byTypeIds(req.getTypeIds()));
		}
		if (req.getAreaIds() != null && !req.getAreaIds().isEmpty()) {
			spec = spec.and(byAreaIds(req.getAreaIds()));
		}
		if (req.getSaleUserIds() != null && !req.getSaleUserIds().isEmpty()) {
			spec = spec.and(bySaleUserIds(req.getSaleUserIds()));
		}
		return spec;
	}

	private static Specification<Property> applyFlagFilters(PropertySearchReq req, Specification<Property> spec) {
		if (req.getIsPublic() != null) {
			spec = spec.and(byIsPublic(req.getIsPublic()));
		}
		if (req.getIsForRent() != null) {
			spec = spec.and(byIsForRent(req.getIsForRent()));
		}
		return spec;
	}

	private static Specification<Property> applyPriceFilters(PropertySearchReq req, Specification<Property> spec) {
		if (req.getMinPrice() != null) {
			spec = spec.and(byMinPrice(req.getMinPrice()));
		}
		if (req.getMaxPrice() != null) {
			spec = spec.and(byMaxPrice(req.getMaxPrice()));
		}
		return spec;
	}

	private static Specification<Property> applyDateFilters(PropertySearchReq req, Specification<Property> spec) {
		if (req.getCreatedFrom() != null) {
			spec = spec.and(byCreatedFrom(req.getCreatedFrom()));
		}
		if (req.getCreatedTo() != null) {
			spec = spec.and(byCreatedTo(req.getCreatedTo()));
		}
		if (req.getUpdatedFrom() != null) {
			spec = spec.and(byUpdatedFrom(req.getUpdatedFrom()));
		}
		if (req.getUpdatedTo() != null) {
			spec = spec.and(byUpdatedTo(req.getUpdatedTo()));
		}
		return spec;
	}

	private static Specification<Property> applyDetailFilters(PropertySearchReq req, Specification<Property> spec) {
		if (req.getDetails() != null && !req.getDetails().isEmpty()) {
			spec = spec.and(byDetails(req.getDetails()));
		}
		return spec;
	}

	private static Specification<Property> byKeyword(String keyword) {
		return (root, query, cb) -> {
			String pattern = "%" + keyword.toLowerCase() + "%";
			Join<Object, Object> typeJoin = root.join("type", JoinType.LEFT);
			Join<Object, Object> areaJoin = root.join("area", JoinType.LEFT);
			Join<Object, Object> saleInfoJoin = root.join("saleInfo", JoinType.LEFT);

			Predicate titlePred = cb.like(cb.lower(root.get("title")), pattern);
			Predicate descPred = cb.like(cb.lower(root.get("description")), pattern);
			Predicate typeNamePred = cb.like(cb.lower(typeJoin.get("typeName")), pattern);
			Predicate areaNamePred = cb.like(cb.lower(areaJoin.get("areaName")), pattern);
			Predicate areaLinkPred = cb.like(cb.lower(areaJoin.get("areaLink")), pattern);
			Predicate saleFullNamePred = cb.like(cb.lower(saleInfoJoin.get("fullName")), pattern);
			Predicate salePhonePred = cb.like(cb.lower(saleInfoJoin.get("phone")), pattern);

			return cb.or(titlePred, descPred, typeNamePred, areaNamePred, areaLinkPred, saleFullNamePred, salePhonePred);
		};
	}

	private static Specification<Property> byTitle(String title) {
		return (root, query, cb) -> cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
	}

	private static Specification<Property> byDescription(String description) {
		return (root, query, cb) -> cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%");
	}

	private static Specification<Property> byTypeIds(List<Integer> typeIds) {
		return (root, query, cb) -> {
			Join<Object, Object> typeJoin = root.join("type", JoinType.INNER);
			return typeJoin.get("typeId").in(typeIds);
		};
	}

	private static Specification<Property> byAreaIds(List<Integer> areaIds) {
		return (root, query, cb) -> {
			Join<Object, Object> areaJoin = root.join("area", JoinType.INNER);
			return areaJoin.get("areaId").in(areaIds);
		};
	}

	private static Specification<Property> bySaleUserIds(List<Long> saleUserIds) {
		return (root, query, cb) -> {
			Join<Object, Object> saleInfoJoin = root.join("saleInfo", JoinType.INNER);
			return saleInfoJoin.get("userId").in(saleUserIds);
		};
	}

	private static Specification<Property> byIsPublic(Boolean isPublic) {
		return (root, query, cb) -> cb.equal(root.get("isPublic"), isPublic);
	}

	private static Specification<Property> byIsForRent(Boolean isForRent) {
		return (root, query, cb) -> cb.equal(root.get("isForRent"), isForRent);
	}

	private static Specification<Property> byMinPrice(BigDecimal minPrice) {
		return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice);
	}

	private static Specification<Property> byMaxPrice(BigDecimal maxPrice) {
		return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice);
	}

	private static Specification<Property> byCreatedFrom(LocalDateTime createdFrom) {
		return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom);
	}

	private static Specification<Property> byCreatedTo(LocalDateTime createdTo) {
		return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), createdTo);
	}

	private static Specification<Property> byUpdatedFrom(LocalDateTime updatedFrom) {
		return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("updatedAt"), updatedFrom);
	}

	private static Specification<Property> byUpdatedTo(LocalDateTime updatedTo) {
		return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("updatedAt"), updatedTo);
	}

	private static Specification<Property> byDetails(List<PropertyDetailFilterReq> details) {
		return (root, query, cb) -> {
			if (query == null) {
				return cb.disjunction();
			}
			List<Predicate> existsPredicates = details.stream()
				.map(detailFilter -> buildDetailExistsPredicate(detailFilter, root, query, cb))
				.toList();
			return cb.and(existsPredicates.toArray(Predicate[]::new));
		};
	}

	private static Predicate buildDetailExistsPredicate(PropertyDetailFilterReq detailFilter, Root<Property> propertyRoot,
	                                                   CriteriaQuery<?> query, CriteriaBuilder cb) {
		Subquery<Long> existsSubquery = query.subquery(Long.class);
		Root<com.qvinh.apartment.features.properties.domain.PropertyDetails> detailRoot =
			existsSubquery.from(com.qvinh.apartment.features.properties.domain.PropertyDetails.class);
		Join<Object, Object> defineDetailJoin = detailRoot.join(FIELD_DETAIL, JoinType.INNER);

		Predicate detailIdPred = cb.equal(detailRoot.get(FIELD_ID).get(FIELD_DETAIL_ID), detailFilter.getDetailId());
		Predicate propertyIdPred = cb.equal(detailRoot.get(FIELD_ID).get(FIELD_PROPERTY_ID), propertyRoot.get(FIELD_PROPERTY_ID));
		Predicate valuePred = buildDetailValuePredicate(detailFilter, cb, detailRoot, defineDetailJoin);

		existsSubquery.select(cb.literal(1L))
			.where(cb.and(detailIdPred, propertyIdPred, valuePred));

		return cb.exists(existsSubquery);
	}

	private static Predicate buildDetailValuePredicate(PropertyDetailFilterReq detailFilter, CriteriaBuilder cb,
	                                                  Root<com.qvinh.apartment.features.properties.domain.PropertyDetails> detailRoot,
	                                                  Join<Object, Object> defineDetailJoin) {
		if (detailFilter.getNumber() != null) {
			Predicate isNumberPred = cb.equal(defineDetailJoin.get(FIELD_IS_NUMBER), true);
			Predicate numberEqualsPred = cb.equal(detailRoot.get(FIELD_VALUE), detailFilter.getNumber().toString());
			return cb.and(isNumberPred, numberEqualsPred);
		}

		if (detailFilter.getMinNumber() != null || detailFilter.getMaxNumber() != null) {
			Predicate valuePred = cb.conjunction();
			if (detailFilter.getMinNumber() != null) {
				valuePred = cb.and(valuePred, cb.greaterThanOrEqualTo(detailRoot.get(FIELD_VALUE), detailFilter.getMinNumber().toString()));
			}
			if (detailFilter.getMaxNumber() != null) {
				valuePred = cb.and(valuePred, cb.lessThanOrEqualTo(detailRoot.get(FIELD_VALUE), detailFilter.getMaxNumber().toString()));
			}
			Predicate isNumberPred = cb.equal(defineDetailJoin.get(FIELD_IS_NUMBER), true);
			return cb.and(isNumberPred, valuePred);
		}

		if (detailFilter.getText() != null && !detailFilter.getText().trim().isEmpty()) {
			Predicate isTextPred = cb.equal(defineDetailJoin.get(FIELD_IS_NUMBER), false);
			String pattern = "%" + detailFilter.getText().toLowerCase().trim() + "%";
			Predicate textLikePred = cb.like(cb.lower(detailRoot.get(FIELD_VALUE)), pattern);
			return cb.and(isTextPred, textLikePred);
		}

		return cb.conjunction();
	}
}
