package org.id_group.chess.view;

import static org.junit.jupiter.api.Assertions.*;

import org.id_group.chess.model.piece.PieceType;
import org.id_group.chess.viewmodel.ViewModel;
import org.junit.jupiter.api.Test;

class BoardHitTesterTest {
  @Test
  void mapsBothOrientationsAndRejectsOutsideCoordinates() {
    BoardState state = new BoardState();
    BoardHitTester hit = new BoardHitTester(state, new ViewModel());
    assertEquals(0, hit.getClickedCol(0));
    assertEquals(7, hit.getClickedRow(799.99));
    state.invertBoard();
    assertEquals(7, hit.getClickedCol(0));
    assertEquals(0, hit.getClickedRow(799.99));
    for (double coordinate :
        new double[] {-0.01, -100, 800, 1000, Double.NaN, Double.POSITIVE_INFINITY})
      assertEquals(-1, hit.getClickedCol(coordinate));
  }

  @Test
  void promotionChoicesWorkForBothColorsAndOrientations() {
    for (boolean white : new boolean[] {true, false})
      for (boolean flipped : new boolean[] {true, false}) {
        ViewModel vm = new ViewModel();
        vm.loadFen(white ? "7k/P7/8/8/8/8/8/7K w - - 0 1" : "7k/8/8/8/8/8/p7/7K b - - 0 1");
        vm.selectSquare(white ? 1 : 6, 0);
        vm.moveSelectedPiece(white ? 0 : 7, 0);
        BoardState state = new BoardState();
        if (flipped) state.invertBoard();
        BoardHitTester hit = new BoardHitTester(state, vm);
        double x = flipped ? 750 : 50;
        boolean top = white != flipped;
        assertEquals(PieceType.QUEEN, hit.getPromotionPieceType(x, top ? 50 : 750));
        assertEquals(PieceType.KNIGHT, hit.getPromotionPieceType(x, top ? 150 : 650));
        assertEquals(PieceType.ROOK, hit.getPromotionPieceType(x, top ? 250 : 550));
        assertEquals(PieceType.BISHOP, hit.getPromotionPieceType(x, top ? 350 : 450));
        assertNull(hit.getPromotionPieceType(x, top ? 450 : 350));
      }
  }
}
