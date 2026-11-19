package org.id_group.chess.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.Stream;
import org.id_group.chess.model.move.Move;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Standard perft positions, also used by Stockfish's tests/perft.sh. Counts verify the entire
 * legal-move tree, including make/unmake and special moves.
 */
class PerftTest {
  static Stream<Arguments> positions() {
    return Stream.of(
        Arguments.of(Board.INITIAL_FEN, 4, 197281L),
        Arguments.of(
            "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1", 3, 97862L),
        Arguments.of("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1", 4, 43238L),
        Arguments.of("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1", 3, 9467L),
        Arguments.of("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", 3, 62379L),
        Arguments.of(
            "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10", 3, 89890L));
  }

  @ParameterizedTest
  @MethodSource("positions")
  void referenceMoveTrees(String fen, int depth, long expected) {
    Board b = new Board(fen);
    GameHistory h = new GameHistory(b);
    MoveGenerator g = new MoveGenerator(b, new CheckValidator(b), h);
    assertEquals(expected, perft(b, h, g, depth));
    assertEquals(
        fen, new BoardEncoder(b, h, g).toFen(), "Search must restore every position field");
  }

  private long perft(Board b, GameHistory h, MoveGenerator g, int depth) {
    if (depth == 0) return 1;
    List<Move> moves = new ArrayList<>();
    for (int r = 0; r < 8; r++)
      for (int c = 0; c < 8; c++) moves.addAll(g.getPossibleMoves(b.getPieceAtPosition(r, c)));
    long nodes = 0;
    for (Move move : moves) {
      b.performTheMove(move, true);
      h.move(move);
      nodes += perft(b, h, g, depth - 1);
      h.revertMove();
    }
    return nodes;
  }
}
