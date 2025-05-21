package com.example.fin.sorting;

import org.springframework.data.domain.Sort;

/**
 * The interface of Sorting service.
 * Provides method to generate Sort from String[] with only accepted sorting fields
 */
public interface SortingService {
    Sort getSort(String[] sort, String[] sortingFields);
}
