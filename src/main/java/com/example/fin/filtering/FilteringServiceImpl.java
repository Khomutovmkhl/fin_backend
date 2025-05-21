package com.example.fin.filtering;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FilteringServiceImpl implements FilteringService {
    private static final Pattern filterPattern = Pattern.compile("filter\\[(\\w+(?:\\.\\w+)*)]\\[(\\w+)]");

    /**
     * Returns Object Filters that contain list of filters. It takes request parameters and finds filters in it.
     * Filtering fields are limited in controllers and filters cannot be to other fields than defined
     *
     * @param filteringFields fields what can be filtered
     * @return Filters object, what contains serialized filters.
     */
    @Override
    public Filters findFiltersInRequestParameters(Map<String, String[]> requestParameters, String[] filteringFields) {
        var filters = new Filters();
        requestParameters.forEach((parameter, values) -> {
            Matcher matcher = filterPattern.matcher(parameter);
            if (matcher.find() && stringContainsItemFromList(matcher.group(1), filteringFields)) {
                var filter = Filter.builder()
                    .filteringAttribute(matcher.group(1))
                    .operator(RestFilterOperator.valueOf(matcher.group(2)))
                    .values(transformParameterValuesToClassifiedListOfStrings(values))
                    .build();
                filters.getFilters().add(filter);
            }
        });
        return filters;
    }

    private List<String> transformParameterValuesToClassifiedListOfStrings(String[] values) {
        List<String> resultList = new ArrayList<>();
        Arrays.stream(values)
            .filter(value -> value != null && !value.isBlank())
            .forEach(value -> resultList.addAll(Arrays.stream(value.trim().split(",")).map(String::trim).toList()));

        return resultList;
    }

    private boolean stringContainsItemFromList(String inputStr, String[] items) {
        return Arrays.stream(items).anyMatch(inputStr::contains);
    }
}
