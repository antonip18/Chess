package org.id_group.chess.model.move;

import org.id_group.chess.model.piece.Piece;
import org.id_group.chess.model.piece.Position;

public abstract class Move {
  public abstract Piece getPiece();

  public abstract Position getTargetPosition();

  public abstract Position getStartingPosition();

  public abstract Piece getCapturedPiece();
}
