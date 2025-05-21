package com.example.fin.filtering;

import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class FiltersResolver implements HandlerMethodArgumentResolver {
    private final FilteringService filteringService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(Filters.class);
    }

    @Override
    public Filters resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        FilteringFields filteringFieldsAnnotation = parameter.getMethodAnnotation(FilteringFields.class);
        String[] filteringFields = filteringFieldsAnnotation != null ? filteringFieldsAnnotation.value() : new String[0];
        return filteringService.findFiltersInRequestParameters(webRequest.getParameterMap(), filteringFields);
    }
}
