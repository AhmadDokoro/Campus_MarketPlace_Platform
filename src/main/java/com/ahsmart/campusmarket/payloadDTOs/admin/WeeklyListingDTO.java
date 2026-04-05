package com.ahsmart.campusmarket.payloadDTOs.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WeeklyListingDTO {
    private final String weekLabel;
    private final long count;
}
