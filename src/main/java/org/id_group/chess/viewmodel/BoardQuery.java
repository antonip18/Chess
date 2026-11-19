package org.id_group.chess.viewmodel;

import java.util.List;
import org.id_group.chess.model.move.Move;
import org.id_group.chess.model.piece.Piece;
import org.id_group.chess.model.piece.PieceColor;
import org.id_group.chess.model.piece.PieceType;
import org.id_group.chess.model.piece.Position;

final class BoardQuery {

  private final GameViewState gameViewState;

  BoardQuery(GameViewState gameViewState) {
    this.gameViewState = gameViewState;
  }

  public Position getKingPosition(PieceColor color) {
    return gameViewState.getGame().getKingPosition(color);
  }

  public SquareState getSquareState(int i, int j) {
    Position p = new Position(i, j);

    if (gameViewState.isActivePromotion() && p.equals(gameViewState.getSelectedPosition()))
      return SquareState.SELECTED_IN_PROMOTION;
    if (p.equals(gameViewState.getSelectedPosition())) return SquareState.SELECTED;
    if (p.equals(gameViewState.getGame().getLastMoveStartingPosition()))
      return SquareState.PREVIOUS_MOVE_FROM;
    if (p.equals(gameViewState.getGame().getLastMoveTargetPosition()))
      return SquareState.PREVIOUS_MOVE_TO;
    return SquareState.NORMAL;
  }

  public PieceType getPieceTypeAt(int i, int j) {
    if (i < 0 || i >= 8 || j < 0 || j >= 8) return null;
    Piece piece = gameViewState.getGame().getPieceAt(i, j);
    return piece == null ? null : piece.getPieceType();
  }

  public PieceColor getPieceColorAt(int i, int j) {
    if (i < 0 || i >= 8 || j < 0 || j >= 8) return null;
    Piece piece = gameViewState.getGame().getPieceAt(i, j);
    return piece == null ? null : piece.getPieceColor();
  }

  public List<Position> getPossibleMovesWithFreeSquares() {
    List<Move> moves = gameViewState.getGame().getPossibleMoves(gameViewState.getSelectedPiece());
    return moves.stream()
        .map(Move::getTargetPosition)
        .distinct()
        .filter(
            position ->
                gameViewState.getGame().getPieceAt(position.getRow(), position.getCol()) == null)
        .toList();
  }

  public List<Position> getPossibleMovesWithNotFreeSquares() {
    List<Move> moves = gameViewState.getGame().getPossibleMoves(gameViewState.getSelectedPiece());
    return moves.stream()
        .map(Move::getTargetPosition)
        .distinct()
        .filter(
            position ->
                gameViewState.getGame().getPieceAt(position.getRow(), position.getCol()) != null)
        .toList();
  }
}
