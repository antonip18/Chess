package org.id_group.chess.model.move;

import java.util.Objects;
import org.id_group.chess.model.piece.Piece;
import org.id_group.chess.model.piece.PieceType;
import org.id_group.chess.model.piece.Position;

public class Promotion extends Move {
  private final Piece piece;
  private final Piece capturedPiece;
  private final Position startingPosition;
  private final Position targetPosition;
  private final PieceType promotedTo;

  public Promotion(
      Piece piece,
      Piece capturedPiece,
      Position targetPosition,
      Position startingPosition,
      PieceType promotedTo) {
    this.piece = piece;
    this.capturedPiece = capturedPiece;
    this.startingPosition = startingPosition;
    this.targetPosition = targetPosition;
    this.promotedTo = promotedTo;
  }

  @Override
  public Piece getPiece() {
    return piece;
  }

  public Piece getCapturedPiece() {
    return capturedPiece;
  }

  @Override
  public Position getStartingPosition() {
    return startingPosition;
  }

  @Override
  public Position getTargetPosition() {
    return targetPosition;
  }

  public PieceType getPromotedTo() {
    return promotedTo;
  }

  @Override
  public int hashCode() {
    return Objects.hash(piece, targetPosition, promotedTo);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Promotion mov)) return false;
    return mov.piece == piece
        && targetPosition.equals(mov.targetPosition)
        && promotedTo.equals(mov.promotedTo);
  }
}
