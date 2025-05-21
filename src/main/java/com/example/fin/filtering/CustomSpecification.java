package com.example.fin.filtering;


import jakarta.persistence.criteria.Predicate;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

import static com.example.fin.filtering.RestFilterOperator.*;


public class CustomSpecification<T> implements Specification<T> {
    private final Filter filter;
    private final Map<Class<?>, Map<String, Field>> classFieldMap = new HashMap<>();

    public CustomSpecification(Filter filter) {
        this.filter = filter;
    }

    @Override
    public Predicate toPredicate(@NonNull Root<T> root, @NonNull CriteriaQuery<?> query, @NonNull CriteriaBuilder cb) {
        List<String> attributes = Arrays.asList(filter.getFilteringAttribute().replace(" ", "").split("\\."));
        String attribute = attributes.get(attributes.size() - 1);
        From<?, ?> join = findNestedResource(root, attributes);
        query.distinct(true);

        return switch (filter.getOperator()) {
            case EQ, NEQ -> buildOrPredicate(
                    filter.getValues(),
                    value -> getEqualPredicate(cb, join, attribute, value, filter.getOperator()),
                    cb);
            case LIKE -> buildAndPredicate(
                    filter.getValues(),
                    value -> cb.like(join.get(attribute).as(String.class), "%" + value.replaceAll("([_\\\\%])", "") + "%"),
                    cb);
            case LT, LE, GT, GE -> buildAndPredicate(
                    filter.getValues(),
                    value -> getComparisonPredicate(cb, join, attribute, value, filter.getOperator()),
                    cb);
            case CONTAINS_ANY, CONTAINS_ALL, EMPTY -> buildCustomFiltersPredicate(join, query, cb);
        };
    }

    /**
     * Builds a custom predicate (CONTAINS_ANY, CONTAINS_ALL, EMPTY) based on the provided filter operator.
     *
     * @param join  From<?, ?> representing the joined entity.
     * @param query CriteriaQuery to which the predicate will be applied.
     * @param cb    CriteriaBuilder used to build predicates.
     * @return The custom predicate based on the filter operator.
     */
    private Predicate buildCustomFiltersPredicate(From<?, ?> join, CriteriaQuery<?> query, CriteriaBuilder cb) {
        var joinId = findIdPath(join);

        if (filter.getOperator() == EMPTY) {
            return buildEmptyPredicate(joinId, join, cb);
        }
        if (filter.getOperator() == CONTAINS_ALL) {
            query.having(cb.greaterThanOrEqualTo(cb.count(joinId), (long) filter.getValues().size()));
        }

        return joinId.in(filter.getValues());
    }

    /**
     * Builds an EMPTY predicate based on the filter values. An EMPTY predicate is used to check if a collection
     * attribute is empty or not.
     *
     * @param joinId Expression representing the joined entity's ID field.
     * @param cb     CriteriaBuilder to build predicates.
     * @return null or
     */
    private Predicate buildEmptyPredicate(Expression<?> joinId, From<?, ?> join, CriteriaBuilder cb) {
        if (filter.getValues().size() > 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Value of EMPTY filter can not be list");
        }
        if (!filter.getValues().isEmpty() && !filter.getValues().get(0).equalsIgnoreCase("true") && !filter.getValues().get(0).equalsIgnoreCase("false")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Value of EMPTY filter should be boolean: true or false");
        }

        if (filter.getValues().isEmpty() || Boolean.parseBoolean(filter.getValues().get(0))) {
            return cb.and(cb.isNotNull(join.getParentPath()), cb.isNull(joinId));
        }
        return cb.isNotNull(joinId);
    }

    /**
     * Creates an equality predicate (equals or not equals) based on the provided operator.
     *
     * @param criteriaBuilder CriteriaBuilder to build predicates.
     * @param join            From<?, ?> representing the join to the attribute.
     * @param attribute       Name of the attribute to apply the equality check to.
     * @param value           The value to compare against.
     * @param operator        The equality operator (EQ or NEQ).
     * @return The equality Predicate.
     */
    private Predicate getEqualPredicate(CriteriaBuilder criteriaBuilder, From<?, ?> join, String attribute, String value, RestFilterOperator operator) {
        // Determine the type of the attribute
        Class<?> attributeType = join.get(attribute).getJavaType();
        Predicate predicate;

        // Check for boolean type
        if (attributeType == Boolean.class || attributeType == boolean.class) {
            predicate = criteriaBuilder.equal(join.get(attribute), Boolean.parseBoolean(value));
        } else if (attributeType  == BigDecimal.class) {
            predicate = criteriaBuilder.equal(join.get(attribute), BigDecimal.valueOf(Long.parseLong(value)));
        } else {
            predicate = criteriaBuilder.equal(join.get(attribute).as(String.class), value);
        }

        return operator == EQ ? predicate : predicate.not();
    }

    /**
     * Creates a comparison predicate (e.g., less than, less than or equal to, greater than, or greater than or equal to)
     * based on the provided operator.
     *
     * @param cb        CriteriaBuilder to build predicates.
     * @param join      From<?, ?> representing the join to the attribute.
     * @param attribute Name of the attribute to apply the comparison to.
     * @param value     The value to compare against.
     * @param operator  The comparison operator.
     * @return The comparison Predicate.
     * @throws UnsupportedOperationException If the operator is not supported.
     */
    private Predicate getComparisonPredicate(CriteriaBuilder cb, From<?, ?> join, String attribute, String value, RestFilterOperator operator) {
        return switch (operator) {
            case LT -> getLessThanPredicate(cb, join, attribute, value);
            case LE -> getLessThanOrEqualPredicate(cb, join, attribute, value);
            case GT -> getGreaterThanPredicate(cb, join, attribute, value);
            case GE -> getGreaterThanOrEqualPredicate(cb, join, attribute, value);
            default -> throw new UnsupportedOperationException("Operator not supported yet: " + operator);
        };
    }

    private Predicate getLessThanPredicate(CriteriaBuilder cb, From<?, ?> join, String attribute, String value) {
        var javaType = join.get(attribute).getJavaType();
        if (javaType.equals(BigDecimal.class)) {
            return cb.lessThan(join.get(attribute).as(BigDecimal.class), BigDecimal.valueOf(Long.parseLong(value)));
        } else if (javaType.equals(Instant.class)) {
            return cb.lessThan(join.get(attribute).as(Instant.class), Instant.parse(value));
        }
        return cb.lessThan(join.get(attribute).as(String.class), value);
    }

    private Predicate getLessThanOrEqualPredicate(CriteriaBuilder cb, From<?, ?> join, String attribute, String value) {
        var javaType = join.get(attribute).getJavaType();
        if (javaType.equals(BigDecimal.class)) {
            return cb.lessThanOrEqualTo(join.get(attribute).as(BigDecimal.class), BigDecimal.valueOf(Long.parseLong(value)));
        } else if (javaType.equals(Instant.class)) {
            return cb.lessThanOrEqualTo(join.get(attribute).as(Instant.class), Instant.parse(value));
        }
        return cb.lessThanOrEqualTo(join.get(attribute).as(String.class), value);
    }

    private Predicate getGreaterThanPredicate(CriteriaBuilder cb, From<?, ?> join, String attribute, String value) {
        var javaType = join.get(attribute).getJavaType();
        if (javaType.equals(BigDecimal.class)) {
            return cb.greaterThan(join.get(attribute).as(BigDecimal.class), BigDecimal.valueOf(Long.parseLong(value)));
        } else if (javaType.equals(Instant.class)) {
            return cb.greaterThan(join.get(attribute).as(Instant.class), Instant.parse(value));
        }
        return cb.greaterThan(join.get(attribute).as(String.class), value);
    }

    private Predicate getGreaterThanOrEqualPredicate(CriteriaBuilder cb, From<?, ?> join, String attribute, String value) {
        var javaType = join.get(attribute).getJavaType();
        if (javaType.equals(BigDecimal.class)) {
            return cb.greaterThanOrEqualTo(join.get(attribute).as(BigDecimal.class), BigDecimal.valueOf(Long.parseLong(value)));
        } else if (javaType.equals(Instant.class)) {
            return cb.greaterThanOrEqualTo(join.get(attribute).as(Instant.class), Instant.parse(value));
        }
        return cb.greaterThanOrEqualTo(join.get(attribute).as(String.class), value);
    }

    /**
     * Finds the path to the ID field (annotated with @Id) in the given entity.
     *
     * @param entity The entity from which to find the ID field path.
     * @return The Path<?> representing the ID field in the entity.
     * @throws ResponseStatusException If no ID field with @Id annotation is found.
     */
    private Path<?> findIdPath(From<?, ?> entity) {
        var declaredFields = getSuperClassWithIdAnnotation(entity.getJavaType()).getDeclaredFields();
        var idField = Arrays.stream(declaredFields)
                .filter(field -> field.isAnnotationPresent(Id.class))
                .map(Field::getName)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No ID field for entity: " + entity.getJavaType().getName()));
        return entity.get(idField);
    }

    /**
     * Recursively iterates throw class hierarchy to find last super class before object
     *
     * @param javaClass class what you want to explore till last super class
     * @return last nested relation
     */
    private Class<?> getSuperClassWithIdAnnotation(Class<?> javaClass) {
        if (javaClass.getSuperclass().getSuperclass() == null || Arrays.stream(javaClass.getDeclaredFields()).anyMatch(field -> field.isAnnotationPresent(Id.class))) {
            return javaClass;
        }
        return javaClass.isArray() ? javaClass.arrayType() : getSuperClassWithIdAnnotation(javaClass.getSuperclass());
    }

    /**
     * Builds an OR predicate by applying the given predicate function to each value in the list of values.
     *
     * @param values            List of values to apply the predicate function to.
     * @param predicateFunction Function that creates a predicate from a single value.
     * @param cb                CriteriaBuilder to build predicates.
     * @return The OR predicate combining all predicates created from the values.
     * @throws IllegalArgumentException If no values are provided for the predicate.
     */
    private Predicate buildOrPredicate(List<String> values, Function<String, Predicate> predicateFunction, CriteriaBuilder cb) {
        return values.stream()
                .map(predicateFunction)
                .reduce(cb::or)
                .orElseThrow(() -> new IllegalArgumentException("No values provided for predicate"));
    }

    /**
     * Builds an AND predicate by applying the given predicate function to each value in the list of values.
     *
     * @param values            List of values to apply the predicate function to.
     * @param predicateFunction Function that creates a predicate from a single value.
     * @param cb                CriteriaBuilder to build predicates.
     * @return The AND predicate combining all predicates created from the values.
     * @throws IllegalArgumentException If no values are provided for the predicate.
     */
    private Predicate buildAndPredicate(List<String> values, Function<String, Predicate> predicateFunction, CriteriaBuilder cb) {
        return values.stream()
                .map(predicateFunction)
                .reduce(cb::and)
                .orElseThrow(() -> new IllegalArgumentException("No values provided for predicate"));
    }

    /**
     * Recursively iterates through nested resources and returns the last relation.
     * This method is useful for navigating a series of nested attributes/relations to reach the final desired relation.
     *
     * @param join       The starting join (relation) from which to begin the iteration.
     * @param attributes List of nested resources to iterate through (ordered from root -> last relation/attribute).
     * @return The last nested relation as a From<?, ?> type.
     */
    private From<?, ?> findNestedResource(From<?, ?> join, List<String> attributes) {
        var fieldMap = getAllFields(join.getJavaType());

        if (attributes.isEmpty() || !isAttributeARelation(attributes.get(0), fieldMap)) {
            return join;
        } else {
            var attributeType = join.get(attributes.get(0)).getJavaType();

            if (Set.class.isAssignableFrom(attributeType)) {
                return findNestedResource(join.joinSet(attributes.get(0), JoinType.LEFT), attributes.subList(1, attributes.size()));
            } else if (List.class.isAssignableFrom(attributeType)) {
                return findNestedResource(join.joinList(attributes.get(0), JoinType.LEFT), attributes.subList(1, attributes.size()));
            } else if (Map.class.isAssignableFrom(attributeType)) {
                return findNestedResource(join.joinMap(attributes.get(0), JoinType.LEFT), attributes.subList(1, attributes.size()));
            }

            return findNestedResource(join.join(attributes.get(0), JoinType.LEFT), attributes.subList(1, attributes.size()));
        }
    }

    /**
     * Check if the given attribute represents a relation in the entity.
     *
     * @param attributeName The name of the attribute to check.
     * @return {@code true} if the attribute is a relation (OneToMany, ManyToMany, ManyToOne, or OneToOne), {@code false} otherwise.
     * @throws ResponseStatusException if the attribute does not exist in the entity.
     */
    private boolean isAttributeARelation(String attributeName, Map<String, Field> fieldMap) {
        var field = fieldMap.get(attributeName);
        if (field == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No such field: " + attributeName);
        }
        var type = field.getType();
        if (Collection.class.isAssignableFrom(type)) {
            // Check if it's a collection type
            return field.isAnnotationPresent(OneToMany.class)
                    || field.isAnnotationPresent(ManyToMany.class);
        } else {
            // Check if it's an entity type
            return field.isAnnotationPresent(ManyToOne.class)
                    || field.isAnnotationPresent(OneToOne.class);
        }
    }

    Map<String, Field> getAllFields(Class<?> clazz) {
        if (clazz == null) {
            return Collections.emptyMap();
        }
        var savedResult = classFieldMap.get(clazz);
        if (savedResult != null) {
            return savedResult;
        }

        Map<String, Field> result = new HashMap<>(getAllFields(clazz.getSuperclass()));
        List<Field> filteredFields = Arrays.stream(clazz.getDeclaredFields()).toList();
        filteredFields.forEach(field -> result.put(field.getName(), field));

        if (!classFieldMap.containsKey(clazz)) {
            classFieldMap.put(clazz, result);
        }
        return result;
    }
}
