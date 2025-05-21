package com.example.fin.sorting;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class SortingServiceImpl implements SortingService {

    /**
     * Parses String[] to Sort object according to the given sorting fields
     * "-" - descending sorting
     * nothing - ascending sorting
     *
     * @param sort          Array of strings from which the sort will be created
     * @param sortingFields Fields that can be sorted
     * @return Sort object with sorting elements from sort array
     */
    @Override
    public Sort getSort(String[] sort, String[] sortingFields) {
        List<Sort.Order> orders = new ArrayList<>();
        for (String order : sort) {
            if (stringContainsItemFromList(order, sortingFields)) {
                if (order.charAt(0) == '-') {
                    orders.add(new Sort.Order(Sort.Direction.DESC, order.substring(1)));
                } else {
                    orders.add(new Sort.Order(Sort.Direction.ASC, order));
                }
            }
        }
        return Sort.by(orders);
    }

    private boolean stringContainsItemFromList(String inputStr, String[] items) {
        return Arrays.stream(items).anyMatch(inputStr::contains);
    }
}
