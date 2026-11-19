# Third-party notices

## Chess pieces

The three SVG sets supplied with this project originate from the [Lichess piece collection](https://github.com/lichess-org/lila/tree/master/public/piece). Attribution and license mapping follow [Lichess COPYING.md](https://github.com/lichess-org/lila/blob/master/COPYING.md).

| Directory under `src/main/resources/pieces/` | Creator | License |
| --- | --- | --- |
| `california` | [Jerry S.](https://sites.google.com/view/jerrychess/home) | [CC BY-NC-SA 4.0](licenses/CC-BY-NC-SA-4.0.html) |
| `cburnett` | [Colin M. L. Burnett](https://en.wikipedia.org/wiki/User:Cburnett) | [GPL-2.0-or-later](licenses/GPL-2.0.txt) |
| `merida` | Armando Hernandez Marroquin | [GPL-2.0-or-later](licenses/GPL-2.0.txt) |

The supplied SVG files are retained, including their existing sizing/viewBox adjustments. Runtime rasterization changes display resolution only. Source SVGs are included so the artwork remains available in editable form. California permits noncommercial use under its license; it is not covered by the application's MIT license. The screenshot in `assets/preview.png` displays this artwork and is also excluded from the code license.

## Build and runtime

- Maven Wrapper scripts/JAR: Apache Maven project, [Apache License 2.0](licenses/Apache-2.0.txt).
- Apache Batik: Apache Software Foundation, [Apache License 2.0](licenses/Apache-2.0.txt).
- JavaFX: [OpenJFX, GPLv2 with the Classpath Exception](https://github.com/openjdk/jfx/blob/master/LICENSE).
- JUnit: [Eclipse Public License 2.0](https://github.com/junit-team/junit5/blob/main/LICENSE.md).

Runtime and test dependencies are downloaded by Maven rather than vendored. Their individual licenses and notices apply.

## Test references

Perft positions are standard chess-engine regression positions, also used in [Stockfish's perft tests](https://github.com/official-stockfish/Stockfish/blob/master/tests/perft.sh). No Stockfish engine code is included. The move generator and perft traversal are implemented in this project.
