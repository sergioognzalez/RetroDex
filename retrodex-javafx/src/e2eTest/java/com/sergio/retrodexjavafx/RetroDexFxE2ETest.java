package com.sergio.retrodexjavafx;

import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetroDexFxE2ETest extends ApplicationTest {

    static {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
    }

    @Override
    public void start(Stage stage) {
        new RetroDexFxApp().start(stage);
    }

    @Test
    void userSearchesFiltersSortsAndOpensACharacter() {
        pauseForVideo();
        clickOn("#searchField").write("gok");
        pauseForVideo();
        clickOn("Goku | 90s | Anime");
        pauseForVideo();
        assertLabel("#detailName", "Goku");
        assertLabel("#detailOrigin", "Dragon Ball Z");

        replaceText("#searchField", "");
        pauseForVideo();
        chooseComboValue("#decadeFilter", "80s");
        pauseForVideo();
        chooseComboValue("#sortCombo", "Nombre A-Z");
        pauseForVideo();
        clickOn("Mario | 80s | Videojuego");
        pauseForVideo();

        assertLabel("#detailName", "Mario");
        assertLabel("#detailMeta", "80s | Videojuego");
        assertTrue(statusText().contains("Seleccionado: Mario"));
    }

    @Test
    void userCreatesUpdatesSharesAndDeletesACharacter() {
        String originalName = "E2E Hero";
        String updatedName = "E2E Hero Turbo";

        pauseForVideo();
        clickOn("#clearButton");
        pauseForVideo();
        clickOn("#nameField").write(originalName);
        pauseForVideo();
        chooseComboValue("#decadeCombo", "2000s");
        pauseForVideo();
        chooseComboValue("#categoryCombo", "Animacion");
        pauseForVideo();
        clickOn("#originField").write("Retro Lab");
        pauseForVideo();
        clickOn("#descriptionArea").write("Personaje creado desde una prueba E2E completa.");
        pauseForVideo();
        clickOn("#saveButton");
        pauseForVideo();

        assertLabel("#detailName", originalName);
        assertLabel("#detailOrigin", "Retro Lab");

        replaceText("#nameField", updatedName);
        pauseForVideo();
        replaceText("#originField", "Retro Lab 2");
        pauseForVideo();
        replaceText("#descriptionArea", "Descripcion actualizada tras editar el formulario.");
        pauseForVideo();
        clickOn("#saveButton");
        pauseForVideo();

        assertLabel("#detailName", updatedName);
        assertLabel("#detailOrigin", "Retro Lab 2");
        assertLabel("#detailDescription", "Descripcion actualizada tras editar el formulario.");

        clickOn("#shareButton");
        pauseForVideo();
        assertTextAreaContains("#shareOutput", updatedName);
        assertTextAreaContains("#shareOutput", "Retro Lab 2");

        clickOn("#deleteButton");
        pauseForVideo();
        assertLabel("#detailName", "Selecciona un personaje");
        assertTrue(statusText().contains("Personaje eliminado"));
    }

    @Test
    void userNavigatesToolbarDialogsAndValidationMessages() {
        pauseForVideo();
        clickOn("#settingsButton");
        WaitForAsyncUtils.waitForFxEvents();
        pauseForVideo();
        press(KeyCode.ENTER).release(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(statusText().contains("Ventana cerrada: Ajustes"));

        clickOn("#aboutButton");
        WaitForAsyncUtils.waitForFxEvents();
        pauseForVideo();
        press(KeyCode.ENTER).release(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(statusText().contains("Ventana cerrada: Acerca de"));

        clickOn("#catalogButton");
        pauseForVideo();
        assertTrue(statusText().contains("Catalogo activo"));

        clickOn("#clearButton");
        pauseForVideo();
        clickOn("#saveButton");
        pauseForVideo();
        assertTrue(statusText().contains("El nombre es obligatorio"));

        clickOn("#searchField").write("sonic");
        pauseForVideo();
        clickOn("Sonic | 90s | Videojuego");
        pauseForVideo();
        clickOn("#shareButton");
        pauseForVideo();

        assertLabel("#detailName", "Sonic");
        assertTextAreaContains("#shareOutput", "Sonic");
        assertTextAreaContains("#shareOutput", "Sonic The Hedgehog");
    }

    private void replaceText(String selector, String value) {
        clickOn(selector);
        TextInputControl input = lookup(selector).queryAs(TextInputControl.class);
        interact(input::selectAll);
        if (value.isEmpty()) {
            press(KeyCode.BACK_SPACE).release(KeyCode.BACK_SPACE);
        } else {
            write(value);
        }
    }

    private void chooseComboValue(String selector, String value) {
        clickOn(selector);
        ComboBox<String> comboBox = lookup(selector).queryAs(ComboBox.class);
        interact(() -> {
            comboBox.setValue(value);
            comboBox.hide();
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void assertLabel(String selector, String expected) {
        WaitForAsyncUtils.waitForFxEvents();
        Label label = lookup(selector).queryAs(Label.class);
        assertEquals(expected, label.getText());
    }

    private void assertTextAreaContains(String selector, String expected) {
        WaitForAsyncUtils.waitForFxEvents();
        TextArea area = lookup(selector).queryAs(TextArea.class);
        assertTrue(area.getText().contains(expected));
    }

    private String statusText() {
        WaitForAsyncUtils.waitForFxEvents();
        return lookup("#statusLabel").queryAs(Label.class).getText();
    }

    private void pauseForVideo() {
        sleep(700);
    }
}
