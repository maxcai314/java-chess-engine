package game;

import org.junit.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BoardTest {
    public static final String DEFAULT_BOARD =
            """
  _________________
8| r n b q k b n r |
7| p p p p p p p p |
6|                 |
5|                 |
4|                 |
3|                 |
2| P P P P P P P P |
1| R N B Q K B N R |
  -----------------
   a b c d e f g h
 """;

    @Test
    public void testToString() {
        Board board = new Board();
        assertEquals(board.toString(), DEFAULT_BOARD);
        System.out.println(board);
    }

    @Test
    public void testFromFEN() {
        Board board = Board.fromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        assertEquals(board.toString(), DEFAULT_BOARD);

        board = Board.fromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w");
        assertEquals(board.toString(), DEFAULT_BOARD);

        board = Board.fromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b");
        assertEquals(board.toString(), DEFAULT_BOARD);

        board = Board.fromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq");
        assertEquals(board.toString(), DEFAULT_BOARD);

        board = Board.fromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq");
        assertEquals(board.toString(), DEFAULT_BOARD);

        board = Board.fromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - -");
        assertEquals(board.toString(), DEFAULT_BOARD);

        board = Board.fromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b -");
        assertEquals(board.toString(), DEFAULT_BOARD);

        System.out.print(board);
    }

    @Test
    public void testToFen() {
        String FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";
        System.out.println("Loading FEN: " + FEN);
        assertTrue(FEN.regionMatches(0, Board.fromFEN(FEN).toFEN(), 0, FEN.length()));
    }

}