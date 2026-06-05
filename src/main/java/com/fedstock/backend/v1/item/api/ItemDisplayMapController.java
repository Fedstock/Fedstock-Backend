package com.fedstock.backend.v1.item.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fedstock.backend.v1.item.api.dto.ItemDisplayMapResponse;
import com.fedstock.backend.v1.item.application.ItemDisplayMapService;

@RestController
@RequestMapping("/api/v1/items")
public class ItemDisplayMapController {

    private final ItemDisplayMapService itemDisplayMapService;

    public ItemDisplayMapController(ItemDisplayMapService itemDisplayMapService) {
        this.itemDisplayMapService = itemDisplayMapService;
    }

    @GetMapping("/display-map")
    public ItemDisplayMapResponse displayMap(
        @RequestParam String itemIds
    ) {
        return itemDisplayMapService.displayMap(itemIds);
    }
}
