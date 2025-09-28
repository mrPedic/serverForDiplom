package com.example.com.venom.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MenuDto {
    private List<Object> foodGroups = new ArrayList<>();
    private List<Object> drinkGroups = new ArrayList<>();
}
