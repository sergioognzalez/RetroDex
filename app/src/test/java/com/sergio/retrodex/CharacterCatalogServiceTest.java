package com.sergio.retrodex;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class CharacterCatalogServiceTest {

    @Test
    public void hasRequiredNameRejectsBlankValues() {
        assertFalse(CharacterCatalogService.hasRequiredName(null));
        assertFalse(CharacterCatalogService.hasRequiredName("   "));
        assertTrue(CharacterCatalogService.hasRequiredName("Mario"));
    }

    @Test
    public void createCharacterNormalizesTextAndOptionalImage() {
        Character character = CharacterCatalogService.createCharacter(
                "  Sonic  ",
                "  Fast hero  ",
                "  90s ",
                " Videojuego ",
                " SEGA ",
                "   ");

        assertEquals("Sonic", character.getName());
        assertEquals("Fast hero", character.getDescription());
        assertEquals("90s", character.getDecade());
        assertEquals("Videojuego", character.getCategory());
        assertEquals("SEGA", character.getOrigin());
        assertNull(character.getImagePath());
    }

    @Test
    public void createCharacterRequiresName() {
        assertThrows(IllegalArgumentException.class, () ->
                CharacterCatalogService.createCharacter(" ", "desc", "80s", "Anime", "TV", null));
    }

    @Test
    public void updateCharacterKeepsExistingImageWhenNoNewImageIsProvided() {
        Character character = new Character("Old", "Old desc", "70s", "Anime", "Old TV", "old.png");

        CharacterCatalogService.updateCharacter(
                character,
                "  Mazinger Z ",
                " Robot ",
                "70s",
                "Anime",
                "TV",
                null);

        assertEquals("Mazinger Z", character.getName());
        assertEquals("Robot", character.getDescription());
        assertEquals("old.png", character.getImagePath());
    }

    @Test
    public void filterAndSortByDecadeReturnsMatchingCharactersAlphabetically() {
        List<Character> characters = Arrays.asList(
                new Character("Sonic", "", "90s", "Videojuego", "", null),
                new Character("Mario", "", "80s", "Videojuego", "", null),
                new Character("Mega Man", "", "80s", "Videojuego", "", null)
        );

        List<Character> result = CharacterCatalogService.filterAndSort(
                characters,
                CharacterCatalogService.MODE_DECADE,
                "80s",
                CharacterCatalogService.SORT_NAME_ASC);

        assertEquals(2, result.size());
        assertEquals("Mario", result.get(0).getName());
        assertEquals("Mega Man", result.get(1).getName());
    }

    @Test
    public void filterAndSortByCategoryDescendingUsesNameOrder() {
        List<Character> characters = Arrays.asList(
                new Character("Goku", "", "90s", "Anime", "", null),
                new Character("Naruto", "", "2000s", "Anime", "", null),
                new Character("Shrek", "", "2000s", "Animacion", "", null)
        );

        List<Character> result = CharacterCatalogService.filterAndSort(
                characters,
                CharacterCatalogService.MODE_CATEGORY,
                "Anime",
                CharacterCatalogService.SORT_NAME_DESC);

        assertEquals(2, result.size());
        assertEquals("Naruto", result.get(0).getName());
        assertEquals("Goku", result.get(1).getName());
    }

    @Test
    public void sortByDecadeUsesRetroChronologicalOrder() {
        List<Character> characters = Arrays.asList(
                new Character("Naruto", "", "2000s", "Anime", "", null),
                new Character("Space Invader", "", "70s", "Videojuego", "", null),
                new Character("Sonic", "", "90s", "Videojuego", "", null),
                new Character("Mario", "", "80s", "Videojuego", "", null)
        );

        List<Character> result = CharacterCatalogService.filterAndSort(
                characters,
                CharacterCatalogService.MODE_DECADE,
                null,
                CharacterCatalogService.SORT_DECADE);

        assertEquals("Space Invader", result.get(0).getName());
        assertEquals("Mario", result.get(1).getName());
        assertEquals("Sonic", result.get(2).getName());
        assertEquals("Naruto", result.get(3).getName());
    }

    @Test
    public void buildShareTextContainsMainCharacterData() {
        Character character = new Character("Link", "", "80s", "Videojuego", "Zelda", null);

        String text = CharacterCatalogService.buildShareText(character);

        assertTrue(text.contains("Link"));
        assertTrue(text.contains("80s"));
        assertTrue(text.contains("Videojuego"));
        assertTrue(text.contains("Zelda"));
    }
}
