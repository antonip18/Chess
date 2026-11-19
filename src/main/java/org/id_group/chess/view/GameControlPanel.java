package org.id_group.chess.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.id_group.chess.viewmodel.ViewModel;

final class GameControlPanel extends VBox {
  private final Button newGame = button("Nowa gra", "new-game", "primary-button");
  private final Button undo = button("Cofnij ruch", "undo", "secondary-button");
  private final Button flip = button("Odwróć szachownicę", "flip", "secondary-button");
  private final Button copyFen = button("Kopiuj FEN", "copy-fen", "secondary-button");
  private final Button loadFen = button("Wczytaj FEN", "load-fen", "secondary-button");
  private final ComboBox<BoardTheme> boardThemes = new ComboBox<>();
  private final ComboBox<PieceTheme> pieceThemes = new ComboBox<>();

  GameControlPanel(ViewModel viewModel) {
    getStyleClass().add("game-control-panel");
    setPadding(new Insets(22));
    setSpacing(10);
    setMinWidth(280);
    setPrefWidth(280);
    setMaxWidth(280);
    Label title = new Label("Szachy");
    title.getStyleClass().add("title");
    Label subtitle = new Label("DWOJE GRACZY · JEDNA PLANSZA");
    subtitle.getStyleClass().add("eyebrow");
    Label status = new Label();
    status.setId("status");
    status.textProperty().bind(viewModel.statusProperty());
    status.getStyleClass().add("status");
    status.setMinHeight(38);
    status.setWrapText(true);
    undo.disableProperty().bind(viewModel.canUndoProperty().not());
    boardThemes.getItems().setAll(BoardTheme.values());
    boardThemes.setValue(BoardTheme.CLASSIC);
    boardThemes.setId("board-theme");
    pieceThemes.getItems().setAll(PieceTheme.values());
    pieceThemes.setValue(PieceTheme.CALIFORNIA);
    pieceThemes.setId("piece-theme");
    for (ComboBox<?> box : new ComboBox<?>[] {boardThemes, pieceThemes})
      box.setMaxWidth(Double.MAX_VALUE);
    HBox fenButtons = new HBox(8, copyFen, loadFen);
    HBox.setHgrow(copyFen, Priority.ALWAYS);
    HBox.setHgrow(loadFen, Priority.ALWAYS);
    ListView<String> history = new ListView<>(viewModel.moveHistory());
    history.setId("move-history");
    history.setPlaceholder(new Label("Tutaj pojawią się ruchy"));
    history.setMinHeight(74);
    history.setPrefHeight(145);
    history.setCellFactory(
        list ->
            new ListCell<>() {
              @Override
              protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.format("%2d.   %s", getIndex() + 1, item));
              }
            });
    viewModel
        .revisionProperty()
        .addListener((o, a, b) -> history.scrollTo(Math.max(0, history.getItems().size() - 1)));
    VBox.setVgrow(history, Priority.ALWAYS);
    Label shortcuts = new Label("Ctrl+Z  cofnij · F  obróć · Esc  anuluj");
    shortcuts.getStyleClass().add("hint");
    getChildren()
        .addAll(
            title,
            subtitle,
            status,
            newGame,
            undo,
            flip,
            new Separator(),
            label("Szachownica"),
            boardThemes,
            label("Figury"),
            pieceThemes,
            new Separator(),
            fenButtons,
            label("Historia ruchów"),
            history,
            shortcuts);
  }

  private Label label(String text) {
    Label label = new Label(text);
    label.getStyleClass().add("section-label");
    return label;
  }

  private static Button button(String text, String id, String style) {
    Button button = new Button(text);
    button.setId(id);
    button.setMaxWidth(Double.MAX_VALUE);
    button.setAlignment(Pos.CENTER);
    button.getStyleClass().add(style);
    return button;
  }

  Button getNewGameButton() {
    return newGame;
  }

  Button getUndoButton() {
    return undo;
  }

  Button getInvertBoardButton() {
    return flip;
  }

  Button getCopyFenButton() {
    return copyFen;
  }

  Button getLoadFenButton() {
    return loadFen;
  }

  ComboBox<BoardTheme> getBoardThemes() {
    return boardThemes;
  }

  ComboBox<PieceTheme> getPieceThemes() {
    return pieceThemes;
  }
}
