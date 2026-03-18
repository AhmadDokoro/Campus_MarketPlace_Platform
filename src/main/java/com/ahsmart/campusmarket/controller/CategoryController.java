package com.ahsmart.campusmarket.controller;

import com.ahsmart.campusmarket.model.Category;
import com.ahsmart.campusmarket.model.enums.Role;
import com.ahsmart.campusmarket.service.category.CategoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.List;

@Controller
@RequestMapping("/admin/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    private boolean isAdmin(HttpSession session) {
        Object roleObj = session.getAttribute("role");
        return (roleObj instanceof Role) && Role.ADMIN.equals(roleObj);
    }

    @GetMapping
    public String categoriesPage(Model model, HttpSession session,
                                 @RequestParam(value = "success", required = false) String success,
                                 @RequestParam(value = "error", required = false) String error) {
        if (!isAdmin(session)) {
            return "redirect:/signin";
        }

        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);
        model.addAttribute("success", success);
        model.addAttribute("error", error);

        // Used by the "Add category" form
        model.addAttribute("categoryForm", new Category());
        return "admin/categories";
    }

    @PostMapping("/create")
    public String createCategory(@RequestParam("categoryName") String categoryName,
                                 @RequestParam(value = "description", required = false) String description,
                                 HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/signin";
        }

        try {
            categoryService.create(categoryName, description);
            return "redirect:/admin/categories?success=Category%20created";
        } catch (IllegalArgumentException ex) {
            return "redirect:/admin/categories?error=" + urlEncode(ex.getMessage());
        }
    }

    @PostMapping("/{id}/update")
    public String updateCategory(@PathVariable("id") Long id,
                                 @RequestParam("categoryName") String categoryName,
                                 @RequestParam(value = "description", required = false) String description,
                                 HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/signin";
        }

        try {
            categoryService.update(id, categoryName, description);
            return "redirect:/admin/categories?success=Category%20updated";
        } catch (IllegalArgumentException ex) {
            return "redirect:/admin/categories?error=" + urlEncode(ex.getMessage());
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable("id") Long id, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/signin";
        }

        try {
            categoryService.deleteIfNoProducts(id);
            return "redirect:/admin/categories?success=Category%20deleted";
        } catch (IllegalArgumentException ex) {
            return "redirect:/admin/categories?error=" + urlEncode(ex.getMessage());
        }
    }

    @GetMapping("/{id}/edit")
    public String editCategoryPage(@PathVariable("id") Long id,
                                   Model model,
                                   HttpSession session,
                                   @RequestParam(value = "error", required = false) String error) {
        if (!isAdmin(session)) {
            return "redirect:/signin";
        }

        Category category = categoryService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Category not found"));

        model.addAttribute("category", category);
        model.addAttribute("error", error);
        return "admin/edit-category";
    }

    private String urlEncode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }
}
