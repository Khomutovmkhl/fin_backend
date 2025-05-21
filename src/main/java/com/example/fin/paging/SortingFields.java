package com.example.fin.paging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation allows you to specify sorting fields for your object, so you will not get unexpected sort from request.
 * <p>
 * Works only with Pageable object from API request
 * <p>
 * {@see OffsetBasedPageableResolver}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SortingFields {
    String[] value();

    String[] defaultSort() default {};
}
