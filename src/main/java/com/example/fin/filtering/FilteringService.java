package com.example.fin.filtering;

import java.util.Map;

public interface FilteringService {
    Filters findFiltersInRequestParameters(Map<String, String[]> requestParameters, String[] filteringFields);
}
