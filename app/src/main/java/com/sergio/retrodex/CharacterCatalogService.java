package com.sergio.retrodex;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class CharacterCatalogService {

    public static final String MODE_DECADE = "decade";
    public static final String MODE_CATEGORY = "category";
    public static final String SORT_NAME_ASC = "name";
    public static final String SORT_NAME_DESC = "name_desc";
    public static final String SORT_DECADE = "decade";

    private CharacterCatalogService() {
    }

    public static boolean hasRequiredName(String name) {
        return !normalize(name).isEmpty();
    }

    public static Character createCharacter(String name,
                                            String description,
                                            String decade,
                                            String category,
                                            String origin,
                                            String imagePath) {
        if (!hasRequiredName(name)) {
            throw new IllegalArgumentException("Character name is required");
        }

        return new Character(
                normalize(name),
                normalize(description),
                normalize(decade),
                normalize(category),
                normalize(origin),
                normalizeNullable(imagePath)
        );
    }

    public static void updateCharacter(Character character,
                                       String name,
                                       String description,
                                       String decade,
                                       String category,
                                       String origin,
                                       String imagePath) {
        if (character == null) {
            throw new IllegalArgumentException("Character cannot be null");
        }
        if (!hasRequiredName(name)) {
            throw new IllegalArgumentException("Character name is required");
        }

        character.setName(normalize(name));
        character.setDescription(normalize(description));
        character.setDecade(normalize(decade));
        character.setCategory(normalize(category));
        character.setOrigin(normalize(origin));
        if (imagePath != null) {
            character.setImagePath(normalizeNullable(imagePath));
        }
    }

    public static List<Character> filterAndSort(List<Character> characters,
                                                String mode,
                                                String selectedFilter,
                                                String sortOrder) {
        List<Character> result = new ArrayList<>();
        if (characters == null) {
            return result;
        }

        for (Character character : characters) {
            if (matchesFilter(character, mode, selectedFilter)) {
                result.add(character);
            }
        }

        result.sort(resolveComparator(sortOrder));
        return result;
    }

    public static String buildShareText(Character character) {
        if (character == null) {
            throw new IllegalArgumentException("Character cannot be null");
        }
        return "Acabo de descubrir a " + normalize(character.getName()) + " en RetroDex.\n"
                + "Decada: " + normalize(character.getDecade())
                + " | Categoria: " + normalize(character.getCategory()) + "\n"
                + "Origen: " + normalize(character.getOrigin());
    }

    private static boolean matchesFilter(Character character, String mode, String selectedFilter) {
        if (character == null) {
            return false;
        }
        if (selectedFilter == null || normalize(selectedFilter).isEmpty()) {
            return true;
        }

        String filter = normalize(selectedFilter);
        if (MODE_CATEGORY.equals(mode)) {
            return filter.equals(character.getCategory());
        }
        return filter.equals(character.getDecade());
    }

    private static Comparator<Character> resolveComparator(String sortOrder) {
        if (SORT_NAME_DESC.equals(sortOrder)) {
            return Comparator.comparing(
                    CharacterCatalogService::safeName,
                    String.CASE_INSENSITIVE_ORDER
            ).reversed();
        }

        if (SORT_DECADE.equals(sortOrder)) {
            return Comparator
                    .comparingInt((Character character) -> decadeWeight(character.getDecade()))
                    .thenComparing(CharacterCatalogService::safeName, String.CASE_INSENSITIVE_ORDER);
        }

        return Comparator.comparing(
                CharacterCatalogService::safeName,
                String.CASE_INSENSITIVE_ORDER
        );
    }

    private static int decadeWeight(String decade) {
        switch (normalize(decade)) {
            case "70s":
                return 1;
            case "80s":
                return 2;
            case "90s":
                return 3;
            case "2000s":
                return 4;
            default:
                return 5;
        }
    }

    private static String safeName(Character character) {
        return normalize(character.getName());
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private static String normalizeNullable(String value) {
        String normalized = normalize(value);
        return normalized.isEmpty() ? null : normalized;
    }
}
