package com.example.fin.filtering;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Filter {
    private String filteringAttribute;
    private RestFilterOperator operator;
    private List<String> values;
}
