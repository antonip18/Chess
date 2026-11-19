package org.id_group.chess.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.id_group.chess.model.GameState;
import org.id_group.chess.model.piece.PieceColor;

final class GameEndOverlay extends StackPane {
  private final Label result = new Label();
  private final Label cause = new Label();

  GameEndOverlay(Runnable newGame) {
    setId("game-end");
    result.setId("result");
    result.getStyleClass().add("result-label");
    cause.getStyleClass().add("cause-label");
    cause.setWrapText(true);
    cause.setAlignment(Pos.CENTER);
    Button restart = new Button("Nowa gra");
    restart.setId("restart");
    restart.getStyleClass().add("primary-button");
    restart.setOnAction(e -> newGame.run());
    Button close = new Button("Pokaż szachownicę");
    close.getStyleClass().add("secondary-button");
    close.setOnAction(e -> hide());
    restart.setMaxWidth(Double.MAX_VALUE);
    close.setMaxWidth(Double.MAX_VALUE);
    VBox card = new VBox(14, result, cause, restart, close);
    card.getStyleClass().add("result-card");
    card.setAlignment(Pos.CENTER);
    card.setPadding(new Insets(26));
    card.setMaxSize(330, 240);
    getChildren().add(card);
    hide();
  }

  void hide() {
    setVisible(false);
    setManaged(false);
  }

  private void show() {
    setVisible(true);
    setManaged(true);
  }

  void showWin(PieceColor winner) {
    result.setText(winner == PieceColor.WHITE_PIECE ? "Białe wygrały" : "Czarne wygrały");
    cause.setText("Szach-mat");
    show();
  }

  void showDraw(GameState state) {
    result.setText("Remis");
    cause.setText(
        switch (state) {
          case DRAW_THREEFOLD_REPETITION -> "Trzykrotne powtórzenie pozycji";
          case DRAW_FIFTY_MOVE_RULE -> "Zasada 50 posunięć";
          case DRAW_INSUFFICIENT_MATERIAL -> "Niewystarczający materiał";
          case DRAW_STALEMATE -> "Pat";
          default -> throw new IllegalArgumentException("Not a draw: " + state);
        });
    show();
  }
}
