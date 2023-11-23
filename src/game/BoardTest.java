package game;

import game.moves.*;
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

    @Test
    public void testEnPassant() {
        Board board = new Board();
        assertEquals(DEFAULT_BOARD, board.toString());
        System.out.println(board);

        board.makeMove("e4");
        board.makeMove("e6");
        board.makeMove("e5");
        board.makeMove("d5"); // en passant is possible on next move

        PlayerMove enPassant = board.fromNotation("exd6");
        assertTrue(enPassant.isPossible(board));
        System.out.println(board.toFEN());
        System.out.println("Lichess link: " + board.analysisLink());

        assertTrue(board.toFEN().startsWith("rnbqkbnr/ppp2ppp/4p3/3pP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6")); // important: FEN specifies possible en passant on d6

        board.makeMove(enPassant);
        System.out.println(board);
        System.out.println(board.toFEN());
        assertTrue(board.toFEN().startsWith("rnbqkbnr/ppp2ppp/3Pp3/8/8/8/PPPP1PPP/RNBQKBNR b KQkq -")); // important: FEN specifies no possible en passant
    }

    @Test
    public void testLoadEnPassant() {
        Board board = Board.fromFEN("4k3/8/4K3/6Pp/8/8/8/8 w - h6 0 2");
        System.out.println(board.toFEN());
        assertTrue(board.toFEN().startsWith("4k3/8/4K3/6Pp/8/8/8/8 w - h6")); // important: FEN specifies possible en passant on h6

        PlayerMove enPassant = board.fromNotation("gxh6");
        assertTrue(enPassant.isPossible(board));

        board.makeMove(enPassant);

        System.out.println(board);
        System.out.println(board.toFEN());
        assertTrue(board.toFEN().startsWith("4k3/8/4K2P/8/8/8/8/8 b - -")); // important: FEN specifies no possible en passant
    }

    /*
        [SetUp "1"]
        [FEN "8/5r2/P3k3/8/3K4/8/8/8 w - - 0 1"]
        1. Kc3 Rf3+ 2. Kd4 Kf5 3. Kd5 Rh3 4. Kd4 Rh4+ 5. Ke3 Re4+ 6. Kf3 Rf4+ 7. Kg3 Kg5
        8. Kh3 Rg4 9. Kh2 Kf4 10. Kh3 Kf3 11. Kh2 Rg3 12. a7 Ke2 13. Kh1 Kf2 14. Kh2 Ke1
        15. Kxg3 Ke2 16. Kf4 Kd3 17. a8=R *
     */
    @Test
    public void testMoveCount() {
        Board board = Board.fromFEN("8/5r2/P3k3/8/3K4/8/8/8 w - - 0 1");
        System.out.println(board);
        System.out.println(board.toFEN());

        assertEquals(board.toFEN(), "8/5r2/P3k3/8/3K4/8/8/8 w - - 0 1");
        board.makeMove("Kc3");
        assertEquals(board.toFEN(), "8/5r2/P3k3/8/8/2K5/8/8 b - - 1 1");
        board.makeMove("Rf3+");
        assertEquals(board.toFEN(), "8/8/P3k3/8/8/2K2r2/8/8 w - - 2 2");
        board.makeMove("Kd4");
        assertEquals(board.toFEN(), "8/8/P3k3/8/3K4/5r2/8/8 b - - 3 2");
        board.makeMove("Kf5");
        assertEquals(board.toFEN(), "8/8/P7/5k2/3K4/5r2/8/8 w - - 4 3");
        board.makeMove("Kd5");
        assertEquals(board.toFEN(), "8/8/P7/3K1k2/8/5r2/8/8 b - - 5 3");
        board.makeMove("Rh3");
        assertEquals(board.toFEN(), "8/8/P7/3K1k2/8/7r/8/8 w - - 6 4");
        board.makeMove("Kd4");
        assertEquals(board.toFEN(), "8/8/P7/5k2/3K4/7r/8/8 b - - 7 4");
        board.makeMove("Rh4+");
        assertEquals(board.toFEN(), "8/8/P7/5k2/3K3r/8/8/8 w - - 8 5");
        board.makeMove("Ke3");
        assertEquals(board.toFEN(), "8/8/P7/5k2/7r/4K3/8/8 b - - 9 5");
        board.makeMove("Re4+");
        assertEquals(board.toFEN(), "8/8/P7/5k2/4r3/4K3/8/8 w - - 10 6");
        board.makeMove("Kf3");
        assertEquals(board.toFEN(), "8/8/P7/5k2/4r3/5K2/8/8 b - - 11 6");
        board.makeMove("Rf4+");
        assertEquals(board.toFEN(), "8/8/P7/5k2/5r2/5K2/8/8 w - - 12 7");
        board.makeMove("Kg3");
        assertEquals(board.toFEN(), "8/8/P7/5k2/5r2/6K1/8/8 b - - 13 7");
        board.makeMove("Kg5");
        assertEquals(board.toFEN(), "8/8/P7/6k1/5r2/6K1/8/8 w - - 14 8");
        board.makeMove("Kh3");
        assertEquals(board.toFEN(), "8/8/P7/6k1/5r2/7K/8/8 b - - 15 8");
        board.makeMove("Rg4");
        assertEquals(board.toFEN(), "8/8/P7/6k1/6r1/7K/8/8 w - - 16 9");
        board.makeMove("Kh2");
        assertEquals(board.toFEN(), "8/8/P7/6k1/6r1/8/7K/8 b - - 17 9");
        board.makeMove("Kf4");
        assertEquals(board.toFEN(), "8/8/P7/8/5kr1/8/7K/8 w - - 18 10");
        board.makeMove("Kh3");
        assertEquals(board.toFEN(), "8/8/P7/8/5kr1/7K/8/8 b - - 19 10");
        board.makeMove("Kf3");
        assertEquals(board.toFEN(), "8/8/P7/8/6r1/5k1K/8/8 w - - 20 11");
        board.makeMove("Kh2");
        assertEquals(board.toFEN(), "8/8/P7/8/6r1/5k2/7K/8 b - - 21 11");
        board.makeMove("Rg3");
        assertEquals(board.toFEN(), "8/8/P7/8/8/5kr1/7K/8 w - - 22 12");
        board.makeMove("a7");
        assertEquals(board.toFEN(), "8/P7/8/8/8/5kr1/7K/8 b - - 0 12");
        board.makeMove("Ke2");
        assertEquals(board.toFEN(), "8/P7/8/8/8/6r1/4k2K/8 w - - 1 13");
        board.makeMove("Kh1");
        assertEquals(board.toFEN(), "8/P7/8/8/8/6r1/4k3/7K b - - 2 13");
        board.makeMove("Kf2");
        assertEquals(board.toFEN(), "8/P7/8/8/8/6r1/5k2/7K w - - 3 14");
        board.makeMove("Kh2");
        assertEquals(board.toFEN(), "8/P7/8/8/8/6r1/5k1K/8 b - - 4 14");
        board.makeMove("Ke1");
        assertEquals(board.toFEN(), "8/P7/8/8/8/6r1/7K/4k3 w - - 5 15");
        board.makeMove("Kxg3");
        assertEquals(board.toFEN(), "8/P7/8/8/8/6K1/8/4k3 b - - 0 15");
        board.makeMove("Ke2");
        assertEquals(board.toFEN(), "8/P7/8/8/8/6K1/4k3/8 w - - 1 16");
        board.makeMove("Kf4");
        assertEquals(board.toFEN(), "8/P7/8/8/5K2/8/4k3/8 b - - 2 16");
        board.makeMove("Kd3");
        assertEquals(board.toFEN(), "8/P7/8/8/5K2/3k4/8/8 w - - 3 17");
        board.makeMove("a8=R");
        System.out.println(board);
        assertEquals(board.toFEN(), "R7/8/8/8/5K2/3k4/8/8 b - - 0 17");

        System.out.println(board);
        System.out.println(board.toFEN());
    }


    /*
        [SetUp "1"]
        [FEN "r3k3/8/8/8/8/8/8/R3K2R w KQq - 0 1"]
        1. Rb1 O-O-O 2. O-O *
     */
    @Test
    public void testCastling() {
        Board board = Board.fromFEN("r3k3/8/8/8/8/8/8/R3K2R w KQq - 0 1");
        System.out.println(board);

        board.makeMove("Rb1");
        assertEquals(board.toFEN(), "r3k3/8/8/8/8/8/8/1R2K2R b Kq - 1 1");

        Castle shortCastle = Castle.shortCastle(Player.WHITE);
        Castle longCastle = Castle.longCastle(Player.WHITE);

        assertTrue(shortCastle.isPossible(board));
        assertFalse(longCastle.isPossible(board));

        board.makeMove("O-O-O");
        assertEquals(board.toFEN(), "2kr4/8/8/8/8/8/8/1R2K2R w K - 2 2");
        board.makeMove("O-O");
        assertEquals(board.toFEN(), "2kr4/8/8/8/8/8/8/1R3RK1 b - - 3 2");
    }

    @Test
    public void testIsPossible() {
        String FEN = "rnbqkbnr/ppp1pppp/8/3p4/3P1B2/8/PPP1PPPP/RN1QKBNR b KQkq - 1 2";
        Board board = Board.fromFEN(FEN);
        System.out.println("Loading FEN: " + board.toFEN());
        System.out.println(board);

        String[] opening = new String[] {
                "c5",
                "Nf3",
                "Nc6",
                "e3",
                "Bf5",
                "Nbd2",
                "e6",
                "c3",
                "Bd6",
                "Bg3",
                "Nf6",
                "Qb3",
                "O-O"
        }; // london system cope harder

        for (String move : opening) {
            System.out.println("\n\n");
            System.out.println(board.getCurrentTurn() + ": " + move);
            PlayerMove playerMove = board.fromNotation(move);

            assertTrue(playerMove.isPossible(board));

            PlayerMove[] illegalMoves = new PlayerMove[] {
                new Promotion(new Piece(playerMove.getPlayer(), PieceType.KING), playerMove.getPiece(), playerMove.getFrom(), playerMove.getTo()),
                new Promotion(new Piece(playerMove.getPlayer(), PieceType.ROOK), playerMove.getPiece(), playerMove.getFrom(), playerMove.getTo()),
                Castle.longCastle(playerMove.getPlayer()),
                EnPassant.enPassant(playerMove.getPlayer(), BoardCoordinate.fromString("a1"), BoardCoordinate.fromString("a2")),
                new RegularMove(new Piece(playerMove.getPlayer(), PieceType.ROOK), BoardCoordinate.fromString("a1"), BoardCoordinate.fromString("a2"))
            };

            for (PlayerMove illegalMove : illegalMoves) {
                assertFalse(illegalMove.isPossible(board));
            }

            board.makeMove(playerMove);
            System.out.println(board);
            System.out.println("Position: " + board.toFEN());
        }
    }

    @Test
    public void testLegalMoves() {
        Board board = new Board();
        System.out.println(board);

        assertEquals(20, board.getLegalMoves().size());
        board.makeMove("e4");
        assertEquals(20, board.getLegalMoves().size());
        board.makeMove("e5");
        assertEquals(29, board.getLegalMoves().size());
        board.makeMove("f4");
        assertEquals(30, board.getLegalMoves().size());
        board.makeMove("exf4");
        assertEquals(29, board.getLegalMoves().size());
        board.makeMove("Nf3");
        assertEquals(29, board.getLegalMoves().size());
        board.makeMove("Nc6");
        assertEquals(29, board.getLegalMoves().size());
        board.makeMove("Bb5");
        assertEquals(31, board.getLegalMoves().size());
        board.makeMove("d6");
        assertEquals(34, board.getLegalMoves().size()); // including castle
        board.makeMove("d3");
        assertEquals(27, board.getLegalMoves().size()); // black's knight is pinned
        board.makeMove("Qh4+"); // check
        assertEquals(5, board.getLegalMoves().size()); // can't castle while in check
        board.makeMove("Nxh4");
        assertEquals(24, board.getLegalMoves().size()); // knight is still pinned
        board.makeMove("Bd7");
        assertEquals(36, board.getLegalMoves().size());
        board.makeMove("Bxf4");
        assertEquals(32, board.getLegalMoves().size()); // knight is unpinned
        board.makeMove("h6");
        assertEquals(42, board.getLegalMoves().size());
        board.makeMove("Bg5");
        assertEquals(28, board.getLegalMoves().size()); // castle is blocked

        System.out.println(board);


        Board endgame = Board.fromFEN("8/5r1p/2k5/6PR/3K4/8/8/8 w - - 0 1");
        System.out.println(endgame);

        assertEquals(13, endgame.getLegalMoves().size()); // king cuts off other king's moves
        endgame.makeMove("Rh4");
        assertEquals(21, endgame.getLegalMoves().size());
        endgame.makeMove("h5");
        assertEquals(15, endgame.getLegalMoves().size()); // en passant
        endgame.makeMove("gxh6");
        assertEquals(20, endgame.getLegalMoves().size());
        endgame.makeMove("Rd7+");
        assertEquals(5, endgame.getLegalMoves().size()); // escape check
        endgame.makeMove("Ke3");
        assertEquals(21, endgame.getLegalMoves().size());
        endgame.makeMove("Re7+");
        assertEquals(7, endgame.getLegalMoves().size()); // rook can also block check
        endgame.makeMove("Re4");
        assertEquals(19, endgame.getLegalMoves().size());
        endgame.makeMove("Rf7");
        assertEquals(16, endgame.getLegalMoves().size());
        endgame.makeMove("Rh4");
        assertEquals(22, endgame.getLegalMoves().size());
        endgame.makeMove("Rf8");
        assertEquals(17, endgame.getLegalMoves().size());
        endgame.makeMove("h7");
        assertEquals(22, endgame.getLegalMoves().size());
        endgame.makeMove("Rg8");
        assertEquals(28, endgame.getLegalMoves().size()); // two promotion squares
        endgame.makeMove("hxg8=Q");
        assertEquals(7, endgame.getLegalMoves().size());
        endgame.makeMove("Kb7");
        assertEquals(43, endgame.getLegalMoves().size());
        endgame.makeMove("Qg7+");
        assertEquals(6, endgame.getLegalMoves().size());
        endgame.makeMove("Kc8");
        assertEquals(45, endgame.getLegalMoves().size());
        endgame.makeMove("Rh8#");

        assertEquals(0, endgame.getLegalMoves().size()); // checkmate
        assertTrue(endgame.isInCheck(Player.BLACK));

        System.out.println(endgame);
        System.out.println("Checkmate!");
        System.out.println("Position: " + endgame.toFEN());
        System.out.println("Permalink to analysis: " + endgame.analysisLink());
    }

}