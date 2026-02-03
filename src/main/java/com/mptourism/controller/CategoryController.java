package com.mptourism.controller;

import com.mptourism.data.CategoryData;
import com.mptourism.model.Category;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    // GET all categories
    @GetMapping
    public List<Category> getAllCategories() {
        return CategoryData.getAllCategories();
    }

    // POST add new category
    @PostMapping
    public String addCategory(@RequestBody Category category) {
        CategoryData.addCategory(category);
        return "Category added successfully";
    }
}
