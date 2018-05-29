package writer;

import data.Cell;
import data.Point;
import data.UnstructuredGrid;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class UnstructuredGridWriter {
    private final UnstructuredGrid data;
    private final String title;

    public UnstructuredGridWriter(UnstructuredGrid data, String title) {
        this.data = data;
        this.title = title.length() <= 255 ? title : title.substring(0, 255);
    }

    public void writeLegacy(File file, FileFormat format) throws IOException {
        switch (format) {
            case ASCII:
                writeLegacyASCII(file);
                break;
            case BINARY:
                writeLegacyBINARY(file);
                break;
        }
    }

    private void writeLegacyASCII(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("# vtk DataFile Version 2.0\n");
            writer.write(title + "\n");
            writer.write("ASCII\n");
            writer.write("DATASET UNSTRUCTURED_GRID\n");
            writer.write("POINTS");
            writer.write(" " + data.points.length);
            writer.write(" double\n");

            for (Point point : data.points) {
                writer.write(String.format("%-20.15f %-20.15f %-20.15f\n", point.x, point.y, point.z));
            }

            int listSize = data.cells.length
                    + Arrays.stream(data.cells)
                    .mapToInt(c -> c.connectivity.length)
                    .sum();

            writer.write(String.format("CELLS %d %d\n", data.cells.length, listSize));
            for (Cell cell : data.cells) {
                writer.write(cellConnectivityString(cell) + "\n");
            }

            writer.write(String.format("CELL_TYPES %d\n", data.cells.length));
            for (Cell cell : data.cells) {
                writer.write(cell.vtkType.ID + "\n");
            }
        }
    }

    private String cellConnectivityString(Cell cell) {
        return cell.connectivity.length + " "
                + Arrays.stream(cell.connectivity)
                .mapToObj(i -> "" + i)
                .collect(Collectors.joining(" "));
    }

    private void writeLegacyBINARY(File file) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
