import game.Board;
import game.moves.PlayerMove;
import game.moves.RegularMove;

import static game.BoardCoordinate.fromString;

public class Main {
    public static void main(String[] args) {
        Board board = new Board();
        System.out.println(board);

        PlayerMove[] opening = new PlayerMove[] {
                new RegularMove(board.getFromString("d2"), fromString("d2"), fromString("d4")),
                new RegularMove(board.getFromString("d7"), fromString("d7"), fromString("d5")),
                new RegularMove(board.getFromString("c1"), fromString("c1"), fromString("f4")),
        }; // london system cope harder

        for (PlayerMove move : opening) {
            board.makeMove(move);
            System.out.println("\n\n");
            System.out.println(board);
        }
    }
}