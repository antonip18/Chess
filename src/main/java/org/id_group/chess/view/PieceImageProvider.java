package org.id_group.chess.view;

import java.io.*;
import java.util.*;
import javafx.scene.image.Image;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.id_group.chess.model.piece.*;

/** Rasterizes bundled SVGs lazily at a resolution suitable for the display. */
public final class PieceImageProvider {
  private record Key(PieceTheme theme, PieceType type, PieceColor color) {}

  private final BoardState state;
  private final Map<Key, Image> images = new HashMap<>();
  private int pixelSize = 256;

  public PieceImageProvider(BoardState state) {
    this.state = state;
  }

  void setPixelSize(double required) {
    int size = 256;
    while (size < required) size *= 2;
    if (size != pixelSize) {
      images.clear();
      pixelSize = size;
    }
  }

  public Image getPieceImage(PieceType type, PieceColor color) {
    return images.computeIfAbsent(new Key(state.getCurrentPieceTheme(), type, color), this::load);
  }

  private Image load(Key key) {
    String piece = key.type() == PieceType.KNIGHT ? "N" : key.type().name().substring(0, 1);
    String path =
        "/pieces/"
            + key.theme().name().toLowerCase(Locale.ROOT)
            + "/"
            + (key.color() == PieceColor.WHITE_PIECE ? "w" : "b")
            + piece
            + ".svg";
    try (InputStream input = PieceImageProvider.class.getResourceAsStream(path)) {
      if (input == null) throw new IOException("Missing resource: " + path);
      PNGTranscoder transcoder = new PNGTranscoder();
      transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float) pixelSize);
      transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float) pixelSize);
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      transcoder.transcode(new TranscoderInput(input), new TranscoderOutput(output));
      return new Image(new ByteArrayInputStream(output.toByteArray()));
    } catch (Exception exception) {
      throw new IllegalStateException("Cannot load chess piece: " + path, exception);
    }
  }
}
