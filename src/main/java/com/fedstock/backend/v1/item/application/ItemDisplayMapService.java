package com.fedstock.backend.v1.item.application;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fedstock.backend.main.error.BadRequestException;
import com.fedstock.backend.v1.item.api.dto.ItemDisplayMapResponse;
import com.fedstock.backend.v1.item.api.dto.ItemDisplayMapResponse.ItemDisplayResponse;

@Service
public class ItemDisplayMapService {

    public ItemDisplayMapResponse displayMap(String itemIds) {
        if (itemIds == null || itemIds.isBlank()) {
            throw new BadRequestException("itemIds is required.");
        }

        List<ItemDisplayResponse> items = Arrays.stream(itemIds.split(","))
            .map(String::trim)
            .filter(itemId -> !itemId.isBlank())
            .map(this::display)
            .toList();

        if (items.isEmpty()) {
            throw new BadRequestException("itemIds is required.");
        }

        return new ItemDisplayMapResponse(items);
    }

    private ItemDisplayResponse display(String itemId) {
        String category = category(itemId);
        return new ItemDisplayResponse(
            itemId,
            name(itemId, category),
            category,
            "ITEM_MASTER"
        );
    }

    private String category(String itemId) {
        if (itemId.contains("FOODS")) {
            return "식품";
        }
        if (itemId.contains("HOBBIES")) {
            return "생활용품";
        }
        if (itemId.contains("HOUSEHOLD")) {
            return "가정용품";
        }
        return "기타";
    }

    private String name(String itemId, String category) {
        String normalized = itemId.substring(itemId.lastIndexOf(':') + 1);
        return category + " " + normalized;
    }
}
