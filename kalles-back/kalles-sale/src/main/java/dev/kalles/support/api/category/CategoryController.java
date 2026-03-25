package dev.kalles.support.api.category;

import dev.kalles.support.application.dto.CategoryRequest;
import dev.kalles.support.application.dto.CategoryResponse;
import dev.kalles.support.application.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Ticket categories and their default priorities")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "List active categories", description = "Returns all active categories ordered by name and subcategory.")
    public ResponseEntity<List<CategoryResponse>> listAll() {
        return ResponseEntity.ok(
                categoryService.listAllActive().stream().map(CategoryResponse::from).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category found"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<CategoryResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(CategoryResponse.from(categoryService.findById(id)));
    }

    @PostMapping
    @Operation(summary = "Create category")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Category created"),
        @ApiResponse(responseCode = "400", description = "Invalid data or duplicate name/subcategory combination")
    })
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(CategoryResponse.from(
                categoryService.create(request.name(), request.subcategory(), request.defaultPriority())));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update category")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category updated"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "400", description = "Name/subcategory combination already in use")
    })
    public ResponseEntity<CategoryResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(CategoryResponse.from(
                categoryService.update(id, request.name(), request.subcategory(), request.defaultPriority())));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate category", description = "Marks the category as inactive (soft delete).")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Category deactivated"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        categoryService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
