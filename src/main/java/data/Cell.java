package data;

public class Cell {
    public final int[] connectivity;
    public final VTKType vtkType;

    public Cell(int[] connectivity, VTKType vtkType) {
        this.connectivity = connectivity;
        this.vtkType = vtkType;
    }
}
