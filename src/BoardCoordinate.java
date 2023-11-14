public record BoardCoordinate(
        int rank,
        int file
) {
    public boolean isValid() {
        return rank >= 0 && rank < 8 && file >= 0 && file < 8;
    }
}
