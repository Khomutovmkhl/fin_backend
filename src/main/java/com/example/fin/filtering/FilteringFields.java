package com.example.fin.filtering;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation allows you to specify filtering fields for your entity, so you will not get unexpected filter from request.
 * <p>
 * You don't need to specify nested fields, only attributes or relations of querying entity.
 * <p>
 * Works only with Filter objects from API request
 * <p>
 * {@link FiltersResolver}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FilteringFields {
    String[] value();
}
