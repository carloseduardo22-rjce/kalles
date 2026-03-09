package dev.kalles.support.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Classifies a Ticket and defines the default Priority
 * automatically applied when the ticket is opened.
 * <p>
 * Example: Category "System / Bug" → Priority HIGH.
 */
@Getter
@EqualsAndHashCode(of = {"name", "subcategory"})
public class Category {

    private final String name;
    private final String subcategory;
    private final Priority defaultPriority;

    public Category(String name, String subcategory, Priority defaultPriority) {
        this.name = name;
        this.subcategory = subcategory;
        this.defaultPriority = defaultPriority;
    }
}
