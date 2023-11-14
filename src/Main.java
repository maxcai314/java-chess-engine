import game.Board;
import game.moves.PlayerMove;
import game.moves.RegularMove;

import static game.BoardCoordinate.fromString;

public class Main {
    public static void main(String[] args) {
        Board board = new Board();
        System.out.println(board);

        String[] opening = new String[] {
                "d4",
                "d5",
                "Bf4",
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
            board.makeMove(playerMove);
            System.out.println(board);
        }
    }
}