package vatika.data;

public class UnstructuredGrid {
    public final Cell[] cells;
    public final Point[] points;
    public final ScalarData[] pointScalarData;
    public final ScalarData[] cellScalarData;
    public final VectorData[] pointVectorData;
    public final VectorData[] cellVectorData;

    public UnstructuredGrid(Point[] points, Cell[] cells,
                            ScalarData[] pointScalarData, VectorData[] pointVectorData,
                            ScalarData[] cellScalarData, VectorData[] cellVectorData) {
        this.points = points;
        this.cells = cells;
        this.pointScalarData = pointScalarData;
        this.cellScalarData = cellScalarData;
        this.pointVectorData = pointVectorData;
        this.cellVectorData = cellVectorData;
    }
}
