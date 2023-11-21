import game.Board;
import game.moves.PlayerMove;
import game.moves.RegularMove;

import static game.BoardCoordinate.fromString;

public class Main {
    public static void main(String[] args) {
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
            System.out.println("\n");
            System.out.printf("Possible Moves for %s: %n%s%n", board.currentTurn(), board.getLegalMoves());
            System.out.printf("%s chose: %s%n", board.getCurrentTurn(), move);
            PlayerMove playerMove = board.fromNotation(move);
            assert playerMove.isPossible(board);
            board.makeMove(playerMove);
            System.out.println(board);
            System.out.println("Position: " + board.toFEN());
        }

        System.out.println("\n\n");
        System.out.println("Permalink to analysis: " + board.analysisLink());

        System.out.println("Starting moves:\n" + new Board().getLegalMoves());
    }
}