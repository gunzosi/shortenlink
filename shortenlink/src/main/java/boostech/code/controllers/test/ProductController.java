package boostech.code.controllers.test;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/product/")
public class ProductController {

    @GetMapping("/getOne")
    public String getProduct() {
        return "Product";
    }

    // Create (User can't create)
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createProduct() {
        return "Product Created";
    }
}
