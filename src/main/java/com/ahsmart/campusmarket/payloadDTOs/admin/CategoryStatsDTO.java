package com.ahsmart.campusmarket.payloadDTOs.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryStatsDTO {
    private final String categoryName;
    private final long productCount;
}
