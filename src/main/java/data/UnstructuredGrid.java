package data;

public class UnstructuredGrid {
    public final Cell[] cells;
    public final Point[] points;

    public UnstructuredGrid(Point[] points, Cell[] cells) {
        this.points = points;
        this.cells = cells;
    }
}
