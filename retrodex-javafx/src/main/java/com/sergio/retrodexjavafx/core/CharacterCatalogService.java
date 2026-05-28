package com.sergio.retrodexjavafx.core;

import com.sergio.retrodexjavafx.model.RetroCharacter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class CharacterCatalogService {
    public static final String ALL = "Todos";
    public static final String SORT_NAME_ASC = "Nombre A-Z";
    public static final String SORT_NAME_DESC = "Nombre Z-A";
    public static final String SORT_DECADE = "Decada";

    private final List<RetroCharacter> characters = new ArrayList<>();
    private int nextId = 1;

    public CharacterCatalogService() {
    }

    public CharacterCatalogService seedDefaults() {
        add("Mario", "80s", "Videojuego", "Super Mario Bros", "Icono de Nintendo y de los plataformas.");
        add("Sonic", "90s", "Videojuego", "Sonic The Hedgehog", "El erizo azul de SEGA.");
        add("Goku", "90s", "Anime", "Dragon Ball Z", "Guerrero Saiyan que siempre supera sus limites.");
        add("Mazinger Z", "70s", "Anime", "Mazinger Z", "Robot clasico del anime mecha.");
        add("Bob Esponja", "2000s", "Animacion", "Nickelodeon", "La esponja mas optimista de Fondo de Bikini.");
        return this;
    }

    public RetroCharacter add(String name,
                              String decade,
                              String category,
                              String origin,
                              String description) {
        validateRequiredName(name);
        RetroCharacter character = new RetroCharacter(
                nextId++,
                clean(name),
                clean(decade),
                clean(category),
                clean(origin),
                clean(description)
        );
        characters.add(character);
        return character;
    }

    public RetroCharacter update(int id,
                                 String name,
                                 String decade,
                                 String category,
                                 String origin,
                                 String description) {
        validateRequiredName(name);
        for (int i = 0; i < characters.size(); i++) {
            RetroCharacter current = characters.get(i);
            if (current.getId() == id) {
                RetroCharacter updated = new RetroCharacter(
                        id,
                        clean(name),
                        clean(decade),
                        clean(category),
                        clean(origin),
                        clean(description)
                );
                characters.set(i, updated);
                return updated;
            }
        }
        throw new IllegalArgumentException("No existe un personaje con id " + id);
    }

    public boolean delete(int id) {
        return characters.removeIf(character -> character.getId() == id);
    }

    public Optional<RetroCharacter> findById(int id) {
        return characters.stream()
                .filter(character -> character.getId() == id)
                .findFirst();
    }

    public List<RetroCharacter> filter(String searchText,
                                       String decade,
                                       String category,
                                       String sortMode) {
        String normalizedSearch = clean(searchText).toLowerCase();
        List<RetroCharacter> result = new ArrayList<>();

        for (RetroCharacter character : characters) {
            if (matchesSearch(character, normalizedSearch)
                    && matchesOption(character.getDecade(), decade)
                    && matchesOption(character.getCategory(), category)) {
                result.add(character);
            }
        }

        result.sort(comparatorFor(sortMode));
        return result;
    }

    public List<RetroCharacter> all() {
        return filter("", ALL, ALL, SORT_DECADE);
    }

    public String shareText(RetroCharacter character) {
        return "Descubre a " + character.getName() + " en RetroDex\n"
                + "Decada: " + character.getDecade() + "\n"
                + "Categoria: " + character.getCategory() + "\n"
                + "Origen: " + character.getOrigin();
    }

    public boolean isEmpty() {
        return characters.isEmpty();
    }

    private void validateRequiredName(String name) {
        if (clean(name).isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
    }

    private boolean matchesSearch(RetroCharacter character, String searchText) {
        return searchText.isEmpty()
                || character.getName().toLowerCase().contains(searchText)
                || character.getOrigin().toLowerCase().contains(searchText);
    }

    private boolean matchesOption(String value, String selectedOption) {
        String option = clean(selectedOption);
        return option.isEmpty() || ALL.equals(option) || value.equals(option);
    }

    private Comparator<RetroCharacter> comparatorFor(String sortMode) {
        if (SORT_NAME_DESC.equals(sortMode)) {
            return Comparator.comparing(RetroCharacter::getName, String.CASE_INSENSITIVE_ORDER).reversed();
        }
        if (SORT_NAME_ASC.equals(sortMode)) {
            return Comparator.comparing(RetroCharacter::getName, String.CASE_INSENSITIVE_ORDER);
        }
        return Comparator
                .comparingInt((RetroCharacter character) -> decadeWeight(character.getDecade()))
                .thenComparing(RetroCharacter::getName, String.CASE_INSENSITIVE_ORDER);
    }

    private int decadeWeight(String decade) {
        switch (decade) {
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

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
