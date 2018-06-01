package writer;

public enum DataFormat {
    ASCII, BINARY;

    @Override
    public String toString() {
        switch (this) {
            case ASCII:
                return "ascii";
            case BINARY:
            default:
                return "binary";
        }
    }
}
