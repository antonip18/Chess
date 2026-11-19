package org.id_group.chess.model;

import static org.junit.jupiter.api.Assertions.*;

import org.id_group.chess.model.piece.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RulesTest {
  static MoveResult play(Game g, String move) {
    int col = move.charAt(0) - 'a', row = 8 - (move.charAt(1) - '0');
    int toCol = move.charAt(2) - 'a', toRow = 8 - (move.charAt(3) - '0');
    return g.move(g.getPieceAt(row, col), toRow, toCol);
  }

  static void success(Game g, String... moves) {
    for (String move : moves) assertEquals(MoveResult.SUCCESS, play(g, move), move);
  }

  @Test
  void initialPositionAndTurnValidation() {
    Game g = new Game();
    assertEquals(Board.INITIAL_FEN, g.toFen());
    assertEquals(MoveResult.ILLEGAL, play(g, "e7e5"));
    assertEquals(MoveResult.ILLEGAL, g.move(null, 0, 0));
    assertEquals(MoveResult.ILLEGAL, g.move(g.getPieceAt(6, 0), -1, 0));
    assertEquals(
        MoveResult.ILLEGAL, g.move(new Piece(PieceType.PAWN, PieceColor.WHITE_PIECE, 6, 0), 4, 0));
    success(g, "e2e4");
    assertEquals("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1", g.toFen());
    g.reverseLastMove();
    assertEquals(Board.INITIAL_FEN, g.toFen());
  }

  @Test
  void queensideCastleRequiresBFileToBeEmpty() {
    Game g = new Game("4k3/8/8/8/8/8/8/RN2K2R w KQ - 0 1");
    assertEquals(MoveResult.ILLEGAL, play(g, "e1c1"));
    assertEquals(MoveResult.ILLEGAL, play(g, "e1b1"));
    success(g, "e1g1");
  }

  @Test
  void castlesUndoExactlyAndCannotCrossCheck() {
    for (String move : new String[] {"e1c1", "e1g1"}) {
      String fen = "4k3/8/8/8/8/8/8/R3K2R w KQ - 7 1";
      Game g = new Game(fen);
      success(g, move);
      g.reverseLastMove();
      assertEquals(fen, g.toFen());
    }
    Game g = new Game("4kr2/8/8/8/8/8/8/R3K2R w KQ - 0 1");
    assertEquals(MoveResult.ILLEGAL, play(g, "e1g1"));
    success(g, "e1c1");
  }

  @Test
  void kingCannotTurnAnyLongHorizontalClickIntoCastle() {
    Game g = new Game("4k3/8/8/8/8/8/8/R3K2R w KQ - 0 1");
    assertEquals(MoveResult.ILLEGAL, play(g, "e1a1"));
    assertEquals(MoveResult.ILLEGAL, play(g, "e1h1"));
  }

  @Test
  void blackKingOffStartingFileCannotCastle() {
    Game g = new Game("r2k3r/8/8/8/8/8/8/4K3 b - - 0 1");
    assertEquals(MoveResult.ILLEGAL, play(g, "d8g8"));
    assertEquals(MoveResult.ILLEGAL, play(g, "d8b8"));
  }

  @Test
  void doublePawnMoveMayBlockCheckEvenIfSingleStepDoesNot() {
    Game g = new Game("7k/8/8/1b6/8/8/2P1K3/8 w - - 0 1");
    assertTrue(g.isCheck());
    assertEquals(MoveResult.ILLEGAL, play(g, "c2c3"));
    success(g, "c2c4");
  }

  @Test
  void enPassantCannotExposeOwnKing() {
    Game g = new Game("7k/8/8/4KPpr/8/8/8/8 w - g6 0 1");
    String before = g.toFen();
    assertEquals(MoveResult.ILLEGAL, play(g, "f5g6"));
    assertEquals(before, g.toFen());
  }

  @Test
  void enPassantCanEscapePawnCheckAndUndo() {
    String fen = "7k/8/8/3pP3/4K3/8/8/8 w - d6 0 1";
    Game g = new Game(fen);
    assertTrue(g.isCheck());
    success(g, "e5d6");
    assertNull(g.getPieceAt(3, 3));
    g.reverseLastMove();
    assertEquals(fen, g.toFen());
  }

  @Test
  void enPassantExpiresAfterAnotherMove() {
    Game g = new Game();
    success(g, "e2e4", "a7a6", "e4e5", "d7d5", "g1f3", "a6a5");
    assertEquals(MoveResult.ILLEGAL, play(g, "e5d6"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"QUEEN", "ROOK", "BISHOP", "KNIGHT"})
  void promotionAndCapturePromotionUndo(String name) {
    for (String fen :
        new String[] {"7k/P7/8/8/8/8/8/7K w - - 73 1", "1r5k/P7/8/8/8/8/8/7K w - - 73 1"}) {
      Game g = new Game(fen);
      int col = g.getPieceAt(0, 1) == null ? 0 : 1;
      assertEquals(MoveResult.PROMOTION_REQUIRED, g.move(g.getPieceAt(1, 0), 0, col));
      assertEquals(fen, g.toFen());
      assertEquals(MoveResult.ILLEGAL, g.move(g.getPieceAt(1, 0), 0, col, PieceType.KING));
      assertEquals(MoveResult.SUCCESS, g.move(g.getPieceAt(1, 0), 0, col, PieceType.valueOf(name)));
      assertEquals("0", g.toFen().split(" ")[4]);
      g.reverseLastMove();
      assertEquals(fen, g.toFen());
    }
  }

  @Test
  void repetitionIncludesInitialPositionAndUndoRemovesAbandonedBranch() {
    Game g = new Game();
    success(g, "g1f3", "g8f6", "f3g1", "f6g8", "g1f3", "g8f6", "f3g1", "f6g8");
    assertEquals(GameState.DRAW_THREEFOLD_REPETITION, g.getGameState());
    g.reverseLastMove();
    assertEquals(GameState.NORMAL, g.getGameState());
    success(g, "f6h5");
    assertEquals(GameState.NORMAL, g.getGameState());
    for (int i = 0; i < 8; i++) g.reverseLastMove();
    assertEquals(Board.INITIAL_FEN, g.toFen());
    success(g, "g1f3", "g8f6", "f3g1", "f6g8");
    assertEquals(GameState.NORMAL, g.getGameState());
  }

  @Test
  void reevaluatingDoesNotCountRepetitions() {
    Board b = new Board();
    GameHistory h = new GameHistory(b);
    CheckValidator c = new CheckValidator(b);
    GameStateEvaluator e = new GameStateEvaluator(b, h, c, new MoveGenerator(b, c, h));
    for (int i = 0; i < 10; i++) e.calculateGameState();
    assertEquals(GameState.NORMAL, e.getCurrentGameState());
    assertEquals(1, h.currentPositionOccurrences());
  }

  @Test
  void halfmoveClockIsRestoredAfterUndo() {
    Game g = new Game("7k/8/8/8/8/8/8/R6K w - - 99 1");
    success(g, "a1a2");
    assertEquals(GameState.DRAW_FIFTY_MOVE_RULE, g.getGameState());
    g.reverseLastMove();
    assertEquals(GameState.NORMAL, g.getGameState());
    assertEquals("99", g.toFen().split(" ")[4]);
  }

  @Test
  void pawnMoveResetsClockAndUndoRestoresIt() {
    String fen = "7k/8/8/8/8/8/P7/7K w - - 99 1";
    Game g = new Game(fen);
    success(g, "a2a3");
    assertEquals("0", g.toFen().split(" ")[4]);
    g.reverseLastMove();
    assertEquals(fen, g.toFen());
  }

  @Test
  void foolMateStopsPlayAndUndoReopensGame() {
    Game g = new Game();
    success(g, "f2f3", "e7e5", "g2g4", "d8h4");
    assertEquals(GameState.CHECKMATE_WHITE, g.getGameState());
    assertEquals(MoveResult.ILLEGAL, play(g, "a2a3"));
    g.reverseLastMove();
    assertEquals(GameState.NORMAL, g.getGameState());
    success(g, "b8c6");
  }

  @Test
  void stalemateAndMaterialDraws() {
    assertEquals(
        GameState.DRAW_STALEMATE, new Game("7k/5Q2/6K1/8/8/8/8/8 b - - 0 1").getGameState());
    assertEquals(
        GameState.DRAW_INSUFFICIENT_MATERIAL,
        new Game("7k/8/8/8/8/8/8/KB6 w - - 0 1").getGameState());
    assertEquals(GameState.NORMAL, new Game("7k/8/8/8/8/8/8/KNN5 w - - 0 1").getGameState());
  }

  @Test
  void fenRoundTripsAndRejectsInvalidPositions() {
    String fen = "r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 8 14";
    Game g = new Game(fen);
    assertEquals(fen, g.toFen());
    success(g, "e8g8");
    assertEquals("15", g.toFen().split(" ")[5]);
    g.reverseLastMove();
    assertEquals(fen, g.toFen());
    for (String invalid :
        new String[] {
          "",
          "8/8/8/8/8/8/8/8 w - - 0 1",
          "7k/8/8/8/8/8/8/7K w K - 0 1",
          "7k/8/8/8/8/8/8/7K w - e3 0 1",
          "7k/7K/8/8/8/8/8/8 w - - 0 1"
        }) assertThrows(IllegalArgumentException.class, () -> new Game(invalid), invalid);
  }

  @Test
  void repetitionKeyAccountsForTurnRightsAndLegalEnPassantOnly() {
    assertNotEquals(
        key("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1"), key("r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 0 1"));
    assertNotEquals(
        key("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1"), key("r3k2r/8/8/8/8/8/8/R3K2R w - - 0 1"));
    assertEquals(key("7k/8/8/4KPpr/8/8/8/8 w - g6 0 1"), key("7k/8/8/4KPpr/8/8/8/8 w - - 0 1"));
    assertNotEquals(
        key("7k/8/8/3pP3/4K3/8/8/8 w - d6 0 1"), key("7k/8/8/3pP3/4K3/8/8/8 w - - 0 1"));
  }

  static String key(String fen) {
    Board b = new Board(fen);
    GameHistory h = new GameHistory(b);
    return new BoardEncoder(b, h, new MoveGenerator(b, new CheckValidator(b), h)).mapToString();
  }
}
