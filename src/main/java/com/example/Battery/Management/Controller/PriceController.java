package com.example.Battery.Management.Controller;

import com.example.Battery.Management.Module.Price;
import com.example.Battery.Management.Service.PriceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PriceController {

    private final PriceService priceService;

    public PriceController(PriceService priceService) {
        this.priceService = priceService;
    }

    @GetMapping("/showPrice")
    public String showPriceInfo() {
        return priceService.getFormattedPriceInfo();
    }
}
