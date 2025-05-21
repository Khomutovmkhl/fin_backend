package com.example.fin.paging;

import com.example.fin.sorting.SortingService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.server.ResponseStatusException;

/**
 * Component to handle Pageable object from request defined in JSON:API format
 *
 * @see <a href="https://jsonapi.org/format/#query-parameters-families">Query Parameter Families</a>
 */
@Component
@RequiredArgsConstructor
public class OffsetBasedPageableResolver implements HandlerMethodArgumentResolver {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final SortingService sortingService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Pageable.class.equals(parameter.getParameterType());
    }

    @Override
    public Pageable resolveArgument(@NonNull MethodParameter parameter, ModelAndViewContainer mavContainer,
                                    NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        String offsetString = webRequest.getParameter("page[offset]");
        String limitString = webRequest.getParameter("page[limit]");

        int size = DEFAULT_PAGE_SIZE;
        int offset = 0;

        if (limitString != null) {
            size = Integer.parseInt(limitString);
        }
        if (offsetString != null) {
            offset = Integer.parseInt(offsetString);
        }
        if (offset % size != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Offset must be divisible by size");
        }

        // Retrieve sorting fields from the annotation
        SortingFields sortingFieldsAnnotation = parameter.getMethodAnnotation(SortingFields.class);
        String[] sortingFields = sortingFieldsAnnotation != null ? sortingFieldsAnnotation.value() : new String[0];

        // Use default sort parameters if sortParams is null or empty
        String[] sortParams = webRequest.getParameterValues("sort");
        if ((sortParams == null || sortParams.length == 0) && sortingFieldsAnnotation != null) {
            sortParams = sortingFieldsAnnotation.defaultSort();
        }

        return PageRequest.of(offset / size, size, sortingService.getSort(sortParams, sortingFields));
    }
}
