package com.sergio.retrodexjavafx;

import com.sergio.retrodexjavafx.core.CharacterCatalogService;
import com.sergio.retrodexjavafx.model.RetroCharacter;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class RetroDexFxApp extends Application {
    private final CharacterCatalogService catalog = new CharacterCatalogService().seedDefaults();

    private TextField searchField;
    private ComboBox<String> decadeFilter;
    private ComboBox<String> categoryFilter;
    private ComboBox<String> sortCombo;
    private ListView<RetroCharacter> characterList;
    private TextField nameField;
    private ComboBox<String> decadeCombo;
    private ComboBox<String> categoryCombo;
    private TextField originField;
    private TextArea descriptionArea;
    private Label detailName;
    private Label detailMeta;
    private Label detailOrigin;
    private Label detailDescription;
    private TextArea shareOutput;
    private Label statusLabel;

    private RetroCharacter selectedCharacter;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(16));
        root.setTop(buildHeader());
        root.setLeft(buildCatalogPanel());
        root.setCenter(buildDetailPanel());
        root.setRight(buildFormPanel());
        root.setBottom(buildStatusBar());

        refreshList();

        Scene scene = new Scene(root, 1120, 680);
        stage.setTitle("RetroDex JavaFX");
        stage.setScene(scene);
        stage.show();
    }

    private HBox buildHeader() {
        Label title = new Label("RetroDex JavaFX");
        title.setId("titleLabel");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Button catalogButton = new Button("Catalogo");
        catalogButton.setId("catalogButton");
        catalogButton.setOnAction(event -> statusLabel.setText("Catalogo activo"));

        Button settingsButton = new Button("Ajustes");
        settingsButton.setId("settingsButton");
        settingsButton.setOnAction(event -> showInfoDialog(
                "Ajustes",
                "Ajustes de RetroDex",
                "Tema oscuro activado y orden por decadas disponible."));

        Button aboutButton = new Button("Acerca de");
        aboutButton.setId("aboutButton");
        aboutButton.setOnAction(event -> showInfoDialog(
                "Acerca de",
                "Acerca de RetroDex",
                "Aplicacion JavaFX preparada para pruebas unitarias, E2E y CI/CD."));

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(12, title, spacer, catalogButton, settingsButton, aboutButton);
        header.setPadding(new Insets(0, 0, 16, 0));
        return header;
    }

    private VBox buildCatalogPanel() {
        searchField = new TextField();
        searchField.setId("searchField");
        searchField.setPromptText("Buscar por nombre u origen");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> refreshList());

        decadeFilter = new ComboBox<>(FXCollections.observableArrayList(
                CharacterCatalogService.ALL, "70s", "80s", "90s", "2000s"));
        decadeFilter.setId("decadeFilter");
        decadeFilter.setValue(CharacterCatalogService.ALL);
        decadeFilter.setOnAction(event -> refreshList());

        categoryFilter = new ComboBox<>(FXCollections.observableArrayList(
                CharacterCatalogService.ALL, "Videojuego", "Anime", "Animacion"));
        categoryFilter.setId("categoryFilter");
        categoryFilter.setValue(CharacterCatalogService.ALL);
        categoryFilter.setOnAction(event -> refreshList());

        sortCombo = new ComboBox<>(FXCollections.observableArrayList(
                CharacterCatalogService.SORT_DECADE,
                CharacterCatalogService.SORT_NAME_ASC,
                CharacterCatalogService.SORT_NAME_DESC));
        sortCombo.setId("sortCombo");
        sortCombo.setValue(CharacterCatalogService.SORT_DECADE);
        sortCombo.setOnAction(event -> refreshList());

        characterList = new ListView<>();
        characterList.setId("characterList");
        characterList.setPrefWidth(310);
        characterList.setOnMouseClicked(event -> selectFromList());
        characterList.setOnKeyReleased(event -> selectFromList());

        VBox panel = new VBox(
                8,
                new Label("Busqueda"),
                searchField,
                new Label("Decada"),
                decadeFilter,
                new Label("Categoria"),
                categoryFilter,
                new Label("Orden"),
                sortCombo,
                characterList
        );
        VBox.setVgrow(characterList, Priority.ALWAYS);
        panel.setPadding(new Insets(0, 16, 0, 0));
        return panel;
    }

    private VBox buildDetailPanel() {
        detailName = new Label("Selecciona un personaje");
        detailName.setId("detailName");
        detailName.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        detailMeta = new Label("-");
        detailMeta.setId("detailMeta");

        detailOrigin = new Label("-");
        detailOrigin.setId("detailOrigin");

        detailDescription = new Label("-");
        detailDescription.setId("detailDescription");
        detailDescription.setWrapText(true);

        Button shareButton = new Button("Compartir");
        shareButton.setId("shareButton");
        shareButton.setOnAction(event -> shareSelected());

        Button deleteButton = new Button("Eliminar");
        deleteButton.setId("deleteButton");
        deleteButton.setOnAction(event -> deleteSelected());

        shareOutput = new TextArea();
        shareOutput.setId("shareOutput");
        shareOutput.setPromptText("Aqui aparecera el texto para compartir");
        shareOutput.setPrefRowCount(5);
        shareOutput.setWrapText(true);

        HBox actions = new HBox(10, shareButton, deleteButton);
        VBox panel = new VBox(
                12,
                detailName,
                detailMeta,
                new Label("Origen"),
                detailOrigin,
                new Label("Descripcion"),
                detailDescription,
                actions,
                new Label("Salida de compartir"),
                shareOutput
        );
        panel.setPadding(new Insets(0, 16, 0, 0));
        return panel;
    }

    private VBox buildFormPanel() {
        nameField = new TextField();
        nameField.setId("nameField");
        nameField.setPromptText("Nombre");

        decadeCombo = new ComboBox<>(FXCollections.observableArrayList("70s", "80s", "90s", "2000s"));
        decadeCombo.setId("decadeCombo");
        decadeCombo.setValue("80s");

        categoryCombo = new ComboBox<>(FXCollections.observableArrayList("Videojuego", "Anime", "Animacion"));
        categoryCombo.setId("categoryCombo");
        categoryCombo.setValue("Videojuego");

        originField = new TextField();
        originField.setId("originField");
        originField.setPromptText("Origen");

        descriptionArea = new TextArea();
        descriptionArea.setId("descriptionArea");
        descriptionArea.setPromptText("Descripcion");
        descriptionArea.setPrefRowCount(6);
        descriptionArea.setWrapText(true);

        Button clearButton = new Button("Nuevo");
        clearButton.setId("clearButton");
        clearButton.setOnAction(event -> clearForm());

        Button saveButton = new Button("Guardar");
        saveButton.setId("saveButton");
        saveButton.setOnAction(event -> saveForm());

        GridPane grid = new GridPane();
        grid.setVgap(8);
        grid.setHgap(8);
        grid.addRow(0, new Label("Nombre"), nameField);
        grid.addRow(1, new Label("Decada"), decadeCombo);
        grid.addRow(2, new Label("Categoria"), categoryCombo);
        grid.addRow(3, new Label("Origen"), originField);
        grid.addRow(4, new Label("Descripcion"), descriptionArea);

        VBox panel = new VBox(12, new Label("Formulario"), grid, new HBox(10, clearButton, saveButton));
        panel.setPrefWidth(330);
        return panel;
    }

    private HBox buildStatusBar() {
        statusLabel = new Label("RetroDex listo");
        statusLabel.setId("statusLabel");
        HBox bar = new HBox(statusLabel);
        bar.setPadding(new Insets(16, 0, 0, 0));
        return bar;
    }

    private void refreshList() {
        if (characterList == null) {
            return;
        }

        List<RetroCharacter> result = catalog.filter(
                searchField.getText(),
                decadeFilter.getValue(),
                categoryFilter.getValue(),
                sortCombo.getValue()
        );
        characterList.setItems(FXCollections.observableArrayList(result));
        statusLabel.setText(result.size() + " personajes visibles");
    }

    private void selectFromList() {
        RetroCharacter selected = characterList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        selectedCharacter = selected;
        detailName.setText(selected.getName());
        detailMeta.setText(selected.getDecade() + " | " + selected.getCategory());
        detailOrigin.setText(selected.getOrigin());
        detailDescription.setText(selected.getDescription());

        nameField.setText(selected.getName());
        decadeCombo.setValue(selected.getDecade());
        categoryCombo.setValue(selected.getCategory());
        originField.setText(selected.getOrigin());
        descriptionArea.setText(selected.getDescription());
        statusLabel.setText("Seleccionado: " + selected.getName());
    }

    private void clearForm() {
        selectedCharacter = null;
        characterList.getSelectionModel().clearSelection();
        nameField.clear();
        decadeCombo.setValue("80s");
        categoryCombo.setValue("Videojuego");
        originField.clear();
        descriptionArea.clear();
        shareOutput.clear();
        statusLabel.setText("Formulario preparado para nuevo personaje");
    }

    private void saveForm() {
        try {
            if (selectedCharacter == null) {
                selectedCharacter = catalog.add(
                        nameField.getText(),
                        decadeCombo.getValue(),
                        categoryCombo.getValue(),
                        originField.getText(),
                        descriptionArea.getText()
                );
                statusLabel.setText("Personaje creado: " + selectedCharacter.getName());
            } else {
                selectedCharacter = catalog.update(
                        selectedCharacter.getId(),
                        nameField.getText(),
                        decadeCombo.getValue(),
                        categoryCombo.getValue(),
                        originField.getText(),
                        descriptionArea.getText()
                );
                statusLabel.setText("Personaje actualizado: " + selectedCharacter.getName());
            }
            searchField.clear();
            decadeFilter.setValue(CharacterCatalogService.ALL);
            categoryFilter.setValue(CharacterCatalogService.ALL);
            refreshList();
            characterList.getSelectionModel().select(selectedCharacter);
            selectFromList();
        } catch (IllegalArgumentException exception) {
            statusLabel.setText(exception.getMessage());
        }
    }

    private void deleteSelected() {
        if (selectedCharacter == null) {
            statusLabel.setText("Selecciona un personaje antes de eliminar");
            return;
        }

        String deletedName = selectedCharacter.getName();
        catalog.delete(selectedCharacter.getId());
        clearForm();
        refreshList();
        detailName.setText("Selecciona un personaje");
        detailMeta.setText("-");
        detailOrigin.setText("-");
        detailDescription.setText("-");
        statusLabel.setText("Personaje eliminado: " + deletedName);
    }

    private void shareSelected() {
        if (selectedCharacter == null) {
            statusLabel.setText("Selecciona un personaje antes de compartir");
            return;
        }

        shareOutput.setText(catalog.shareText(selectedCharacter));
        statusLabel.setText("Texto preparado para compartir");
    }

    private void showInfoDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
        statusLabel.setText("Ventana cerrada: " + title);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
