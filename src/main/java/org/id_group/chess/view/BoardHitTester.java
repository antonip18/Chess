package org.id_group.chess.view;

import org.id_group.chess.model.piece.*;
import org.id_group.chess.viewmodel.ViewModel;

/** All inputs are in logical board coordinates (800 × 800), before rotation. */
final class BoardHitTester {
  private final BoardState state;
  private final ViewModel viewModel;

  BoardHitTester(BoardState state, ViewModel viewModel) {
    this.state = state;
    this.viewModel = viewModel;
  }

  private int index(double coordinate) {
    if (!Double.isFinite(coordinate) || coordinate < 0 || coordinate >= 800) return -1;
    int index = (int) (coordinate / 100);
    return state.isBoardInverted() ? 7 - index : index;
  }

  int getClickedCol(double x) {
    return index(x);
  }

  int getClickedRow(double y) {
    return index(y);
  }

  PieceType getPromotionPieceType(double x, double y) {
    if (!viewModel.isActivePromotion() || getClickedCol(x) != viewModel.getPromotingColumn())
      return null;
    int row = getClickedRow(y);
    if (row < 0) return null;
    if (viewModel.getCurrentPlayerColor() == PieceColor.BLACK_PIECE) row = 7 - row;
    return switch (row) {
      case 0 -> PieceType.QUEEN;
      case 1 -> PieceType.KNIGHT;
      case 2 -> PieceType.ROOK;
      case 3 -> PieceType.BISHOP;
      default -> null;
    };
  }
}
