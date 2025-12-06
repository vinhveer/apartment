package com.qminh.apartment.repository;

import com.qminh.apartment.dto.property.PropertyDetailFilterReq;
import com.qminh.apartment.dto.property.PropertySearchReq;
import com.qminh.apartment.entity.Property;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class PropertySpecifications {

	private PropertySpecifications() {
	}

	public static Specification<Property> bySearchReq(PropertySearchReq req) {
		if (req == null) {
			return (root, query, cb) -> cb.conjunction();
		}

		Specification<Property> spec = (root, query, cb) -> cb.conjunction();

		if (req.getQ() != null && !req.getQ().trim().isEmpty()) {
			spec = spec.and(byKeyword(req.getQ().trim()));
		}

		if (req.getTitle() != null && !req.getTitle().trim().isEmpty()) {
			spec = spec.and(byTitle(req.getTitle().trim()));
		}

		if (req.getDescription() != null && !req.getDescription().trim().isEmpty()) {
			spec = spec.and(byDescription(req.getDescription().trim()));
		}

		if (req.getTypeIds() != null && !req.getTypeIds().isEmpty()) {
			spec = spec.and(byTypeIds(req.getTypeIds()));
		}

		if (req.getAreaIds() != null && !req.getAreaIds().isEmpty()) {
			spec = spec.and(byAreaIds(req.getAreaIds()));
		}

		if (req.getSaleUserIds() != null && !req.getSaleUserIds().isEmpty()) {
			spec = spec.and(bySaleUserIds(req.getSaleUserIds()));
		}

		if (req.getIsPublic() != null) {
			spec = spec.and(byIsPublic(req.getIsPublic()));
		}

		if (req.getIsForRent() != null) {
			spec = spec.and(byIsForRent(req.getIsForRent()));
		}

		if (req.getMinPrice() != null) {
			spec = spec.and(byMinPrice(req.getMinPrice()));
		}

		if (req.getMaxPrice() != null) {
			spec = spec.and(byMaxPrice(req.getMaxPrice()));
		}

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
			Predicate[] existsPredicates = new Predicate[details.size()];
			for (int i = 0; i < details.size(); i++) {
				PropertyDetailFilterReq detailFilter = details.get(i);
				Subquery<Long> existsSubquery = query.subquery(Long.class);
				Root<com.qminh.apartment.entity.PropertyDetails> detailRoot = existsSubquery.from(com.qminh.apartment.entity.PropertyDetails.class);
				Join<Object, Object> defineDetailJoin = detailRoot.join("detail", JoinType.INNER);

				Predicate detailIdPred = cb.equal(detailRoot.get("id").get("detailId"), detailFilter.getDetailId());
				Predicate propertyIdPred = cb.equal(detailRoot.get("id").get("propertyId"), root.get("propertyId"));

				Predicate valuePred = cb.conjunction();
				if (detailFilter.getNumber() != null) {
					Predicate isNumberPred = cb.equal(defineDetailJoin.get("isNumber"), true);
					Predicate numberEqualsPred = cb.equal(detailRoot.get("value"), detailFilter.getNumber().toString());
					valuePred = cb.and(isNumberPred, numberEqualsPred);
				} else if (detailFilter.getMinNumber() != null || detailFilter.getMaxNumber() != null) {
					Predicate isNumberPred = cb.equal(defineDetailJoin.get("isNumber"), true);
					if (detailFilter.getMinNumber() != null) {
						valuePred = cb.and(valuePred, cb.greaterThanOrEqualTo(
							detailRoot.get("value"),
							detailFilter.getMinNumber().toString()
						));
					}
					if (detailFilter.getMaxNumber() != null) {
						valuePred = cb.and(valuePred, cb.lessThanOrEqualTo(
							detailRoot.get("value"),
							detailFilter.getMaxNumber().toString()
						));
					}
					valuePred = cb.and(isNumberPred, valuePred);
				} else if (detailFilter.getText() != null && !detailFilter.getText().trim().isEmpty()) {
					Predicate isTextPred = cb.equal(defineDetailJoin.get("isNumber"), false);
					Predicate textLikePred = cb.like(cb.lower(detailRoot.get("value")), "%" + detailFilter.getText().toLowerCase().trim() + "%");
					valuePred = cb.and(isTextPred, textLikePred);
				}

				existsSubquery.select(cb.literal(1L))
					.where(cb.and(detailIdPred, propertyIdPred, valuePred));

				existsPredicates[i] = cb.exists(existsSubquery);
			}

			return cb.and(existsPredicates);
		};
	}
}

