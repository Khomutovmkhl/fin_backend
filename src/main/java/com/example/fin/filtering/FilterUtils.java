package com.example.fin.filtering;

import org.springframework.data.jpa.domain.Specification;

public class FilterUtils {
    /**
     * Method transforms each filter to Specification and combines all of them using logical 'and'.
     *
     * @param filters the filters what can be applied to entity of controller class
     * @return the specification describing all filters
     */
    public static <T> Specification<T> and(Filters filters) {
        Specification<T> spec = Specification.where(null);
        for (Filter filter : filters.getFilters()) {
            spec = spec.and(new CustomSpecification<>(filter));
        }
        return spec;
    }
}
