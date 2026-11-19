package org.id_group.chess.viewmodel;

import java.util.List;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.id_group.chess.model.Game;
import org.id_group.chess.model.GameState;
import org.id_group.chess.model.piece.*;

/** MVVM boundary: views issue commands and observe read-only properties. */
public final class ViewModel {
  private final GameViewState state = new GameViewState(new Game());
  private final MoveController controller = new MoveController(state);
  private final BoardQuery query = new BoardQuery(state);
  private final ObservableList<String> history = FXCollections.observableArrayList();
  private final ObservableList<String> readOnlyHistory =
      FXCollections.unmodifiableObservableList(history);

  public ViewModel() {
    state
        .revisionProperty()
        .addListener((obs, oldValue, newValue) -> history.setAll(state.getGame().getMoveHistory()));
  }

  public void newGame() {
    state.replaceGame(new Game());
  }

  /** Invalid FEN leaves the existing game and its history intact. */
  public void loadFen(String fen) {
    state.replaceGame(new Game(fen));
  }

  public String toFen() {
    return state.getGame().toFen();
  }

  public void clearSelection() {
    state.clearSelection();
    state.refresh();
  }

  public void undoLastMove() {
    controller.undoLastMove();
  }

  public boolean selectSquare(int row, int col) {
    return controller.selectSquare(row, col);
  }

  public void moveSelectedPiece(int row, int col) {
    controller.moveSelectedPiece(row, col);
  }

  public void finishPromotion(PieceType type) {
    controller.finishPromotion(type);
  }

  public PieceColor getCurrentPlayerColor() {
    return state.getGame().getCurrentPlayerColor();
  }

  public GameState getGameState() {
    return state.getGame().getGameState();
  }

  public boolean isCheck() {
    return state.getGame().isCheck();
  }

  public boolean isActivePromotion() {
    return state.isActivePromotion();
  }

  public int getPromotingColumn() {
    return state.getTargetPosition() == null ? -1 : state.getTargetPosition().getCol();
  }

  public PieceType getSelectedPieceType() {
    return state.getSelectedPiece() == null ? null : state.getSelectedPiece().getPieceType();
  }

  public PieceColor getSelectedPieceColor() {
    return state.getSelectedPiece() == null ? null : state.getSelectedPiece().getPieceColor();
  }

  public SquareState getSquareState(int row, int col) {
    return query.getSquareState(row, col);
  }

  public PieceType getPieceTypeAt(int row, int col) {
    return query.getPieceTypeAt(row, col);
  }

  public PieceColor getPieceColorAt(int row, int col) {
    return query.getPieceColorAt(row, col);
  }

  public Position getKingPosition(PieceColor color) {
    return query.getKingPosition(color);
  }

  public List<Position> getPossibleMovesWithFreeSquares() {
    return query.getPossibleMovesWithFreeSquares();
  }

  public List<Position> getPossibleMovesWithNotFreeSquares() {
    return query.getPossibleMovesWithNotFreeSquares();
  }

  public ObservableList<String> moveHistory() {
    return readOnlyHistory;
  }

  public ReadOnlyBooleanProperty getActivePromotion() {
    return state.activePromotionProperty();
  }

  public ReadOnlyBooleanProperty canUndoProperty() {
    return state.canUndoProperty();
  }

  public ReadOnlyObjectProperty<GameState> gameStateProperty() {
    return state.gameStateProperty();
  }

  public ReadOnlyObjectProperty<PieceColor> currentPlayerColorProperty() {
    return state.currentPlayerColorProperty();
  }

  public ReadOnlyIntegerProperty revisionProperty() {
    return state.revisionProperty();
  }

  public ReadOnlyStringProperty statusProperty() {
    return state.statusProperty();
  }
}
