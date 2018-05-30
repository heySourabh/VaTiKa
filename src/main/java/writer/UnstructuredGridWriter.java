package writer;

import data.*;

import java.io.*;
import java.nio.ByteBuffer;
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

            writePointScalarData(writer);
            writePointVectorData(writer);

            writeCellScalarData(writer);
            writeCellVectorData(writer);
        }
    }

    private void writeCellScalarData(FileWriter writer) throws IOException {
        if (data.cellScalarData == null || data.cellScalarData.length == 0) {
            return;
        }

        writer.write(String.format("CELL_DATA %d\n", data.cells.length));
        for (ScalarData scalarData : data.cellScalarData) {
            writer.write(String.format("SCALARS %s double 1\n", scalarData.dataName));
            writer.write("LOOKUP_TABLE default\n");
            for (double scalar : scalarData.scalars) {
                writer.write(String.format("%1.15f ", scalar));
            }
            writer.write("\n");
        }
    }

    private void writePointScalarData(FileWriter writer) throws IOException {
        if (data.pointScalarData == null || data.pointScalarData.length == 0) {
            return;
        }

        writer.write(String.format("POINT_DATA %d\n", data.points.length));
        for (ScalarData scalarData : data.pointScalarData) {
            writer.write(String.format("SCALARS %s double 1\n", scalarData.dataName));
            writer.write("LOOKUP_TABLE default\n");
            for (double scalar : scalarData.scalars) {
                writer.write(String.format("%1.15f ", scalar));
            }
            writer.write("\n");
        }
    }

    private void writeCellVectorData(FileWriter writer) throws IOException {
        if (data.cellVectorData == null || data.cellVectorData.length == 0) {
            return;
        }

        for (VectorData vectorData : data.cellVectorData) {
            writer.write(String.format("VECTORS %s double\n", vectorData.dataName));
            for (Vector vector : vectorData.vectors) {
                writer.write(String.format("%1.15f %1.15f %1.15f\n", vector.x, vector.y, vector.z));
            }
        }
    }

    private void writePointVectorData(FileWriter writer) throws IOException {
        if (data.pointVectorData == null || data.pointVectorData.length == 0) {
            return;
        }

        for (VectorData vectorData : data.pointVectorData) {
            writer.write(String.format("VECTORS %s double\n", vectorData.dataName));
            for (Vector vector : vectorData.vectors) {
                writer.write(String.format("%1.15f %1.15f %1.15f\n", vector.x, vector.y, vector.z));
            }
        }
    }

    private String cellConnectivityString(Cell cell) {
        return cell.connectivity.length + " "
                + Arrays.stream(cell.connectivity)
                .mapToObj(i -> "" + i)
                .collect(Collectors.joining(" "));
    }

    private void writeLegacyBINARY(File file) throws IOException {
        try (DataOutputStream fileStream = new DataOutputStream(new FileOutputStream(file))) {
            fileStream.writeBytes("# vtk DataFile Version 2.0\n");
            fileStream.writeBytes(title + "\n");
            fileStream.writeBytes("BINARY\n");
            fileStream.writeBytes("DATASET UNSTRUCTURED_GRID\n");
            fileStream.writeBytes("POINTS");
            fileStream.writeBytes(" " + data.points.length);
            fileStream.writeBytes(" double\n");

            for (Point point : data.points) {
                fileStream.writeDouble(point.x);
                fileStream.writeDouble(point.y);
                fileStream.writeDouble(point.z);
            }

            int listSize = data.cells.length
                    + Arrays.stream(data.cells)
                    .mapToInt(c -> c.connectivity.length)
                    .sum();

            fileStream.writeBytes(String.format("CELLS %d %d\n", data.cells.length, listSize));
            for (Cell cell : data.cells) {
                fileStream.write(cellConnectivityBytes(cell));
            }

            fileStream.writeBytes(String.format("CELL_TYPES %d\n", data.cells.length));
            for (Cell cell : data.cells) {
                fileStream.writeInt(cell.vtkType.ID);
            }
        }
    }

    private byte[] cellConnectivityBytes(Cell cell) {
        ByteBuffer bytes = ByteBuffer.allocate(Integer.BYTES * (1 + cell.connectivity.length));
        bytes.putInt(cell.connectivity.length);
        Arrays.stream(cell.connectivity).forEach(bytes::putInt);

        return bytes.array();
    }
}
