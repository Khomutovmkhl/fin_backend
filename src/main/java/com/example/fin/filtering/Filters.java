package com.example.fin.filtering;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Filters {
    private List<Filter> filters = new ArrayList<>();
}
