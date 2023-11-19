package game;

public record BoardCoordinate(
        int rank,
        int file
) {
    public static BoardCoordinate fromString(String s) { // example: "e4"
        return new BoardCoordinate(s.charAt(1) - '1', s.charAt(0) - 'a');
    }

    public BoardCoordinate step(int rankStep, int fileStep) {
        return new BoardCoordinate(rank + rankStep, file + fileStep);
    }

    public BoardCoordinate step(BoardCoordinate step) {
        return step(step.rank(), step.file());
    }

    public boolean isValid() {
        return rank >= 0 && rank < 8 && file >= 0 && file < 8;
    }
}
