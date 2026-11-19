package org.id_group.chess.viewmodel;

import static org.junit.jupiter.api.Assertions.*;

import org.id_group.chess.model.Board;
import org.id_group.chess.model.piece.*;
import org.junit.jupiter.api.Test;

class ViewModelTest {
  @Test
  void selectsOnlySideToMoveAndPublishesHistory() {
    ViewModel vm = new ViewModel();
    assertFalse(vm.selectSquare(1, 4));
    assertNull(vm.getSelectedPieceType());
    assertTrue(vm.selectSquare(6, 4));
    vm.selectSquare(4, 4);
    assertEquals(PieceColor.BLACK_PIECE, vm.currentPlayerColorProperty().get());
    assertEquals("e2–e4", vm.moveHistory().getFirst());
    assertTrue(vm.canUndoProperty().get());
    assertThrows(UnsupportedOperationException.class, () -> vm.moveHistory().clear());
    vm.undoLastMove();
    assertEquals(Board.INITIAL_FEN, vm.toFen());
    assertTrue(vm.moveHistory().isEmpty());
    assertFalse(vm.canUndoProperty().get());
  }

  @Test
  void newGameRetainsBindingsAndResetsAllState() {
    ViewModel vm = new ViewModel();
    var property = vm.currentPlayerColorProperty();
    vm.selectSquare(6, 4);
    vm.moveSelectedPiece(4, 4);
    vm.selectSquare(1, 3);
    vm.newGame();
    assertSame(property, vm.currentPlayerColorProperty());
    assertEquals(PieceColor.WHITE_PIECE, property.get());
    assertNull(vm.getSelectedPieceType());
    assertEquals(Board.INITIAL_FEN, vm.toFen());
    assertFalse(vm.canUndoProperty().get());
  }

  @Test
  void promotionIsAtomicAndUndoCancelsPendingChoice() {
    ViewModel vm = new ViewModel();
    String fen = "7k/P7/8/8/8/8/8/7K w - - 0 1";
    vm.loadFen(fen);
    vm.getActivePromotion()
        .addListener(
            (o, a, b) -> {
              if (b) assertEquals(0, vm.getPromotingColumn());
            });
    vm.selectSquare(1, 0);
    vm.moveSelectedPiece(0, 0);
    assertTrue(vm.isActivePromotion());
    assertEquals(fen, vm.toFen());
    assertFalse(vm.selectSquare(7, 7));
    vm.finishPromotion(PieceType.KING);
    assertTrue(vm.isActivePromotion());
    vm.undoLastMove();
    assertFalse(vm.isActivePromotion());
    assertEquals(fen, vm.toFen());
    vm.selectSquare(1, 0);
    vm.moveSelectedPiece(0, 0);
    vm.finishPromotion(PieceType.QUEEN);
    assertEquals(PieceType.QUEEN, vm.getPieceTypeAt(0, 0));
    vm.undoLastMove();
    assertEquals(fen, vm.toFen());
  }

  @Test
  void invalidFenDoesNotDestroyGameOrHistory() {
    ViewModel vm = new ViewModel();
    vm.selectSquare(6, 4);
    vm.moveSelectedPiece(4, 4);
    String fen = vm.toFen();
    assertThrows(IllegalArgumentException.class, () -> vm.loadFen("invalid"));
    assertEquals(fen, vm.toFen());
    assertEquals(1, vm.moveHistory().size());
  }

  @Test
  void droppingOutsideBoardNeverMakesAnEdgeMove() {
    ViewModel vm = new ViewModel();
    vm.selectSquare(6, 0);
    vm.moveSelectedPiece(5, -1);
    assertEquals(Board.INITIAL_FEN, vm.toFen());
  }
}
