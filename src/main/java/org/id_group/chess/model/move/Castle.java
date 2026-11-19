package org.id_group.chess.model.move;

import java.util.Objects;
import org.id_group.chess.model.piece.Piece;
import org.id_group.chess.model.piece.Position;

public class Castle extends Move {
  private final Piece king;
  private final Piece rook;
  private final Position kingStartingPosition;
  private final Position rookStartingPosition;
  private final boolean shortCastle;

  public Castle(
      Piece king,
      Piece rook,
      Position kingStartingPosition,
      Position rookStartingPosition,
      boolean shortCastle) {
    this.king = king;
    this.rook = rook;
    this.kingStartingPosition = kingStartingPosition;
    this.rookStartingPosition = rookStartingPosition;
    this.shortCastle = shortCastle;
  }

  public Piece getKing() {
    return king;
  }

  public Piece getRook() {
    return rook;
  }

  public Position getKingStartingPosition() {
    return kingStartingPosition;
  }

  public Position getRookStartingPosition() {
    return rookStartingPosition;
  }

  public boolean wasShortCastle() {
    return shortCastle;
  }

  @Override
  public Piece getPiece() {
    return king;
  }

  @Override
  public Position getTargetPosition() {
    return new Position(kingStartingPosition.getRow(), shortCastle ? 6 : 2);
  }

  @Override
  public Position getStartingPosition() {
    return kingStartingPosition;
  }

  @Override
  public Piece getCapturedPiece() {
    return null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(king, shortCastle);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Castle cas)) return false;
    return cas.king == king && cas.shortCastle == shortCastle;
  }
}
