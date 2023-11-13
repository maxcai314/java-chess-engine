public record BoardCoordinate(
        int x,
        int y
) {
    public boolean isValid() {
        return x >= 0 && x < 8 && y >= 0 && y < 8;
    }
}
