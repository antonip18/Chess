package org.id_group.chess.viewmodel;

import org.id_group.chess.model.Game;
import org.id_group.chess.model.GameState;
import org.id_group.chess.model.MoveResult;
import org.id_group.chess.model.piece.*;

/** Commands shared by click, drag and keyboard interactions. */
final class MoveController {
  private final GameViewState state;

  MoveController(GameViewState state) {
    this.state = state;
  }

  boolean selectSquare(int row, int col) {
    Game game = state.getGame();
    if (!inBounds(row, col) || state.isActivePromotion() || game.getGameState() != GameState.NORMAL)
      return false;
    Piece piece = game.getPieceAt(row, col);
    if (piece != null && piece.getPieceColor() == game.getCurrentPlayerColor()) {
      state.select(piece);
      state.refresh();
      return true;
    }
    moveSelectedPiece(row, col);
    return false;
  }

  void moveSelectedPiece(int row, int col) {
    if (!inBounds(row, col) || state.isActivePromotion() || state.getSelectedPiece() == null)
      return;
    MoveResult result = state.getGame().move(state.getSelectedPiece(), row, col);
    if (result == MoveResult.SUCCESS) state.clearSelection();
    else if (result == MoveResult.PROMOTION_REQUIRED)
      state.requestPromotion(new Position(row, col));
    state.refresh();
  }

  void finishPromotion(PieceType type) {
    if (!state.isActivePromotion()) return;
    Position target = state.getTargetPosition();
    if (state.getGame().move(state.getSelectedPiece(), target.getRow(), target.getCol(), type)
        == MoveResult.SUCCESS) state.clearSelection();
    state.refresh();
  }

  void undoLastMove() {
    boolean pending = state.isActivePromotion();
    state.clearSelection();
    if (!pending) state.getGame().reverseLastMove();
    state.refresh();
  }

  private boolean inBounds(int row, int col) {
    return row >= 0 && row < 8 && col >= 0 && col < 8;
  }
}
