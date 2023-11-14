package game;

public record Piece(
        Player owner,
        PieceType type
) {
    public boolean attacksDiagonally() {
        return switch (type) {
            case BISHOP, QUEEN -> true;
            default -> false;
        };
    }

    public boolean attacksOrthogonally() {
        return switch (type) {
            case ROOK, QUEEN -> true;
            default -> false;
        };
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Piece that)) return false;
        return (this.owner() == that.owner && this.type() == that.type());
    }

    char toChar() {
        return switch (owner) {
            case WHITE -> Character.toUpperCase(type.toChar());
            case BLACK -> Character.toLowerCase(type.toChar());
        };
    }
}
