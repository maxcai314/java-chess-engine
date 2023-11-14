public enum Player {
    WHITE, BLACK;

    public Player opponent() {
        return switch (this) {
            case WHITE -> BLACK;
            case BLACK -> WHITE;
        };
    }

    public int homeRank() {
        return switch (this) {
            case WHITE -> 0;
            case BLACK -> 7;
        };
    }

    public int pawnRank() {
        return switch (this) {
            case WHITE -> 1;
            case BLACK -> 6;
        };
    }
}
