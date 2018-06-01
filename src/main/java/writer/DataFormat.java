package writer;

public enum DataFormat {
    ASCII, BINARY;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
