package org.id_group.chess.viewmodel;

import javafx.beans.property.*;
import org.id_group.chess.model.Game;
import org.id_group.chess.model.GameState;
import org.id_group.chess.model.piece.*;

/** Observable presentation state; selection never mutates the chess position. */
final class GameViewState {
  private Game game;
  private Piece selectedPiece;
  private Position targetPosition;
  private final ReadOnlyObjectWrapper<GameState> gameState = new ReadOnlyObjectWrapper<>();
  private final ReadOnlyObjectWrapper<PieceColor> currentPlayerColor =
      new ReadOnlyObjectWrapper<>();
  private final ReadOnlyBooleanWrapper activePromotion = new ReadOnlyBooleanWrapper();
  private final ReadOnlyBooleanWrapper canUndo = new ReadOnlyBooleanWrapper();
  private final ReadOnlyIntegerWrapper revision = new ReadOnlyIntegerWrapper();
  private final ReadOnlyStringWrapper status = new ReadOnlyStringWrapper();

  GameViewState(Game game) {
    replaceGame(game);
  }

  Game getGame() {
    return game;
  }

  void replaceGame(Game game) {
    this.game = game;
    clearSelection();
    refresh();
  }

  Piece getSelectedPiece() {
    return selectedPiece;
  }

  Position getSelectedPosition() {
    return selectedPiece == null ? null : selectedPiece.getPosition();
  }

  Position getTargetPosition() {
    return targetPosition;
  }

  void select(Piece piece) {
    selectedPiece = piece;
  }

  void requestPromotion(Position target) {
    targetPosition = target;
    activePromotion.set(true);
  }

  void clearSelection() {
    selectedPiece = null;
    targetPosition = null;
    activePromotion.set(false);
  }

  boolean isActivePromotion() {
    return activePromotion.get();
  }

  ReadOnlyBooleanProperty activePromotionProperty() {
    return activePromotion.getReadOnlyProperty();
  }

  ReadOnlyBooleanProperty canUndoProperty() {
    return canUndo.getReadOnlyProperty();
  }

  ReadOnlyObjectProperty<GameState> gameStateProperty() {
    return gameState.getReadOnlyProperty();
  }

  ReadOnlyObjectProperty<PieceColor> currentPlayerColorProperty() {
    return currentPlayerColor.getReadOnlyProperty();
  }

  ReadOnlyIntegerProperty revisionProperty() {
    return revision.getReadOnlyProperty();
  }

  ReadOnlyStringProperty statusProperty() {
    return status.getReadOnlyProperty();
  }

  void refresh() {
    currentPlayerColor.set(game.getCurrentPlayerColor());
    canUndo.set(game.canRevertLastMove() || isActivePromotion());
    status.set(
        switch (game.getGameState()) {
          case NORMAL ->
              isActivePromotion()
                  ? "Wybierz figurę do promocji"
                  : (game.getCurrentPlayerColor() == PieceColor.WHITE_PIECE
                          ? "Ruch: białe"
                          : "Ruch: czarne")
                      + (game.isCheck() ? " · szach" : "");
          case CHECKMATE_WHITE -> "Mat · wygrały czarne";
          case CHECKMATE_BLACK -> "Mat · wygrały białe";
          case DRAW_STALEMATE -> "Remis · pat";
          case DRAW_FIFTY_MOVE_RULE -> "Remis · 50 posunięć";
          case DRAW_THREEFOLD_REPETITION -> "Remis · powtórzenie pozycji";
          case DRAW_INSUFFICIENT_MATERIAL -> "Remis · brak materiału";
        });
    gameState.set(game.getGameState());
    revision.set(revision.get() + 1);
  }
}
