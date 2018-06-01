package writer;

public enum DataFormat {
    ASCII, BINARY;

    public static void main(String[] args) {
        System.out.println(ASCII);
        System.out.println(BINARY);
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
