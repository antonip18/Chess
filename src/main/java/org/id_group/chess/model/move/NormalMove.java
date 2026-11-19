package org.id_group.chess.model.move;

import java.util.Objects;
import org.id_group.chess.model.piece.Piece;
import org.id_group.chess.model.piece.Position;

public class NormalMove extends Move {
  private final Piece piece;
  private final Piece capturedPiece;
  private final Position startingPosition;
  private final Position targetPosition;

  public NormalMove(
      Piece piece, Piece capturedPiece, Position targetPosition, Position startingPosition) {
    this.piece = piece;
    this.capturedPiece = capturedPiece;
    this.startingPosition = startingPosition;
    this.targetPosition = targetPosition;
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

  @Override
  public int hashCode() {
    return Objects.hash(piece, targetPosition);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof NormalMove mov)) return false;
    return mov.piece == piece && targetPosition.equals(mov.targetPosition);
  }
}
