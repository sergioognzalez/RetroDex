package com.sergio.retrodexjavafx.core;

import com.sergio.retrodexjavafx.model.RetroCharacter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CharacterCatalogServiceTest {

    @Test
    void newCatalogStartsEmptyAndSeedDefaultsLoadsCharacters() {
        CharacterCatalogService catalog = new CharacterCatalogService();

        assertTrue(catalog.isEmpty());

        CharacterCatalogService returnedCatalog = catalog.seedDefaults();

        assertEquals(catalog, returnedCatalog);
        assertFalse(catalog.isEmpty());
        assertEquals(5, catalog.all().size());
    }

    @Test
    void addCleansInputAndAssignsIncrementalIds() {
        CharacterCatalogService catalog = new CharacterCatalogService();

        RetroCharacter first = catalog.add("  Link  ", " 80s ", " Videojuego ", " Zelda ", " Heroe ");
        RetroCharacter second = catalog.add("Samus", "80s", "Videojuego", "Metroid", "Cazarrecompensas");

        assertEquals(1, first.getId());
        assertEquals(2, second.getId());
        assertEquals("Link", first.getName());
        assertEquals("80s", first.getDecade());
        assertEquals("Videojuego", first.getCategory());
        assertEquals("Zelda", first.getOrigin());
        assertEquals("Heroe", first.getDescription());
    }

    @Test
    void addRejectsBlankNames() {
        CharacterCatalogService catalog = new CharacterCatalogService();

        assertThrows(IllegalArgumentException.class,
                () -> catalog.add("   ", "80s", "Videojuego", "Origen", "Descripcion"));
    }

    @Test
    void updateChangesExistingCharacterAndRejectsInvalidCases() {
        CharacterCatalogService catalog = new CharacterCatalogService();
        RetroCharacter character = catalog.add("Mario", "80s", "Videojuego", "Nintendo", "Fontanero");

        RetroCharacter updated = catalog.update(
                character.getId(),
                "  Super Mario ",
                "90s",
                "Animacion",
                "Serie TV",
                "Version animada");

        assertEquals(character.getId(), updated.getId());
        assertEquals("Super Mario", updated.getName());
        assertEquals("90s", updated.getDecade());
        assertEquals("Animacion", updated.getCategory());
        assertEquals("Serie TV", updated.getOrigin());
        assertEquals("Version animada", updated.getDescription());
        assertThrows(IllegalArgumentException.class,
                () -> catalog.update(999, "Peach", "80s", "Videojuego", "Nintendo", "Princesa"));
        assertThrows(IllegalArgumentException.class,
                () -> catalog.update(character.getId(), "", "80s", "Videojuego", "Nintendo", "Sin nombre"));
    }

    @Test
    void deleteAndFindByIdHandleExistingAndMissingCharacters() {
        CharacterCatalogService catalog = new CharacterCatalogService();
        RetroCharacter sonic = catalog.add("Sonic", "90s", "Videojuego", "SEGA", "Rapido");
        catalog.add("Mario", "80s", "Videojuego", "Nintendo", "Plataformas");

        assertTrue(catalog.findById(sonic.getId()).isPresent());
        assertFalse(catalog.findById(999).isPresent());
        assertTrue(catalog.delete(sonic.getId()));
        assertFalse(catalog.delete(sonic.getId()));
        assertFalse(catalog.isEmpty());
    }

    @Test
    void filterSearchesByNameAndOrigin() {
        CharacterCatalogService catalog = new CharacterCatalogService();
        catalog.add("Goku", "90s", "Anime", "Dragon Ball Z", "Saiyan");
        catalog.add("Naruto", "2000s", "Anime", "Konoha", "Ninja");
        catalog.add("Mario", "80s", "Videojuego", "Nintendo", "Plataformas");

        assertEquals("Goku", catalog.filter("gok", CharacterCatalogService.ALL, CharacterCatalogService.ALL, CharacterCatalogService.SORT_NAME_ASC).get(0).getName());
        assertEquals("Naruto", catalog.filter("kono", CharacterCatalogService.ALL, CharacterCatalogService.ALL, CharacterCatalogService.SORT_NAME_ASC).get(0).getName());
        assertTrue(catalog.filter("no existe", CharacterCatalogService.ALL, CharacterCatalogService.ALL, CharacterCatalogService.SORT_NAME_ASC).isEmpty());
    }

    @Test
    void filterUsesDecadeCategoryAndEmptyOptions() {
        CharacterCatalogService catalog = new CharacterCatalogService();
        catalog.add("Goku", "90s", "Anime", "Dragon Ball Z", "Saiyan");
        catalog.add("Sonic", "90s", "Videojuego", "SEGA", "Rapido");
        catalog.add("Mario", "80s", "Videojuego", "Nintendo", "Plataformas");

        List<RetroCharacter> byDecade = catalog.filter("", "90s", CharacterCatalogService.ALL, CharacterCatalogService.SORT_NAME_ASC);
        List<RetroCharacter> byCategory = catalog.filter("", "", "Videojuego", CharacterCatalogService.SORT_NAME_ASC);

        assertEquals(2, byDecade.size());
        assertEquals("Goku", byDecade.get(0).getName());
        assertEquals(2, byCategory.size());
        assertEquals("Mario", byCategory.get(0).getName());
        assertEquals("Sonic", byCategory.get(1).getName());
    }

    @Test
    void sortModesOrderCharactersCorrectly() {
        CharacterCatalogService catalog = new CharacterCatalogService();
        catalog.add("Zelda", "80s", "Videojuego", "Nintendo", "Princesa");
        catalog.add("Astro", "2010s", "Videojuego", "PlayStation", "Robot");
        catalog.add("Mazinger Z", "70s", "Anime", "Toei", "Robot");
        catalog.add("Naruto", "2000s", "Anime", "Konoha", "Ninja");
        catalog.add("Sonic", "90s", "Videojuego", "SEGA", "Rapido");

        List<RetroCharacter> asc = catalog.filter(null, null, null, CharacterCatalogService.SORT_NAME_ASC);
        List<RetroCharacter> desc = catalog.filter(null, null, null, CharacterCatalogService.SORT_NAME_DESC);
        List<RetroCharacter> decade = catalog.filter(null, null, null, CharacterCatalogService.SORT_DECADE);

        assertEquals("Astro", asc.get(0).getName());
        assertEquals("Zelda", desc.get(0).getName());
        assertEquals("Mazinger Z", decade.get(0).getName());
        assertEquals("Zelda", decade.get(1).getName());
        assertEquals("Sonic", decade.get(2).getName());
        assertEquals("Naruto", decade.get(3).getName());
        assertEquals("Astro", decade.get(4).getName());
    }

    @Test
    void shareTextContainsVisibleCharacterData() {
        CharacterCatalogService catalog = new CharacterCatalogService();
        RetroCharacter character = catalog.add("Pac-Man", "80s", "Videojuego", "Namco", "Comecocos");

        String text = catalog.shareText(character);

        assertTrue(text.contains("Pac-Man"));
        assertTrue(text.contains("80s"));
        assertTrue(text.contains("Videojuego"));
        assertTrue(text.contains("Namco"));
    }
}
