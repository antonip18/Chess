package org.id_group.chess.model;

import org.id_group.chess.model.piece.*;

/** Validates standard (not Chess960) FEN before a new game replaces the current one. */
record FenPosition(
    Piece[][] pieces,
    boolean whiteToMove,
    String castlingRights,
    Position enPassantTarget,
    int halfmoveClock,
    int fullmoveNumber) {
  static FenPosition parse(String fen) {
    if (fen == null) throw new IllegalArgumentException("Podaj pozycję FEN.");
    String[] fields = fen.trim().split("\\s+");
    if (fields.length != 6) throw new IllegalArgumentException("FEN musi zawierać sześć pól.");
    String[] ranks = fields[0].split("/", -1);
    if (ranks.length != 8) throw new IllegalArgumentException("FEN musi zawierać osiem rzędów.");
    Piece[][] pieces = new Piece[8][8];
    int whiteKings = 0, blackKings = 0;
    for (int r = 0; r < 8; r++) {
      int col = 0;
      boolean previousDigit = false;
      for (char c : ranks[r].toCharArray()) {
        if (c >= '1' && c <= '8') {
          if (previousDigit)
            throw new IllegalArgumentException("Sąsiadujące puste pola FEN zapisz jedną cyfrą.");
          col += c - '0';
          previousDigit = true;
          continue;
        }
        previousDigit = false;
        if (col >= 8) throw new IllegalArgumentException("Za długi rząd FEN.");
        PieceType type =
            switch (Character.toLowerCase(c)) {
              case 'k' -> PieceType.KING;
              case 'q' -> PieceType.QUEEN;
              case 'r' -> PieceType.ROOK;
              case 'b' -> PieceType.BISHOP;
              case 'n' -> PieceType.KNIGHT;
              case 'p' -> PieceType.PAWN;
              default -> throw new IllegalArgumentException("Nieznana figura FEN.");
            };
        PieceColor color =
            Character.isUpperCase(c) ? PieceColor.WHITE_PIECE : PieceColor.BLACK_PIECE;
        if (type == PieceType.PAWN && (r == 0 || r == 7))
          throw new IllegalArgumentException("Pion nie może stać na pierwszej ani ósmej linii.");
        if (type == PieceType.KING) {
          if (color == PieceColor.WHITE_PIECE) whiteKings++;
          else blackKings++;
        }
        pieces[r][col] = new Piece(type, color, r, col);
        col++;
      }
      if (col != 8) throw new IllegalArgumentException("Każdy rząd FEN musi zawierać osiem pól.");
    }
    if (whiteKings != 1 || blackKings != 1)
      throw new IllegalArgumentException("Każda strona musi mieć jednego króla.");
    if (!fields[1].equals("w") && !fields[1].equals("b"))
      throw new IllegalArgumentException("Nieprawidłowa strona na ruchu.");
    boolean white = fields[1].equals("w");
    if (!fields[2].matches("-|K?Q?k?q?") || fields[2].isEmpty())
      throw new IllegalArgumentException("Nieprawidłowe prawa do roszady.");
    int half = number(fields[4], 0), full = number(fields[5], 1);
    Position target = null;
    if (!fields[3].equals("-")) {
      if (!fields[3].matches("[a-h][36]"))
        throw new IllegalArgumentException("Nieprawidłowe pole en passant.");
      int row = 8 - (fields[3].charAt(1) - '0'), col = fields[3].charAt(0) - 'a';
      int pawnRow = white ? 3 : 4, startingRow = white ? 1 : 6;
      Piece pawn = pieces[pawnRow][col];
      if (row != (white ? 2 : 5)
          || pieces[row][col] != null
          || pieces[startingRow][col] != null
          || pawn == null
          || pawn.getPieceType() != PieceType.PAWN
          || pawn.getPieceColor() != (white ? PieceColor.BLACK_PIECE : PieceColor.WHITE_PIECE)
          || half != 0)
        throw new IllegalArgumentException(
            "Pole en passant nie odpowiada ostatniemu ruchowi piona.");
      target = new Position(row, col);
    }
    return new FenPosition(pieces, white, fields[2], target, half, full);
  }

  private static int number(String text, int minimum) {
    try {
      if (!text.matches("[0-9]+")) throw new NumberFormatException();
      int value = Integer.parseInt(text);
      if (value < minimum || value > 1_000_000) throw new NumberFormatException();
      return value;
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Nieprawidłowy licznik ruchów FEN.");
    }
  }
}
