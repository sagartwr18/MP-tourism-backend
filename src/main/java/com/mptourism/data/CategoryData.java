package com.mptourism.data;

import com.mptourism.model.Category;
import com.mptourism.storage.CategoryFileStorage;

import java.util.List;

public class CategoryData {

    private static List<Category> categories;

    static {
        categories = CategoryFileStorage.loadCategories();
    }

    public static List<Category> getAllCategories() {
        return categories;
    }

    public static void addCategory(Category category) {
        categories.add(category);
        CategoryFileStorage.saveCategories(categories);
    }
}
