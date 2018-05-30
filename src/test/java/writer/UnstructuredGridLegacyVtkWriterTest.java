package writer;

import data.*;

import java.io.*;
import java.util.Scanner;

import static data.VTKType.VTK_QUAD;
import static data.VTKType.VTK_TRIANGLE;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UnstructuredGridLegacyVtkWriterTest {

    private Point[] points = {
            new Point(2, 3, 0.0),
            new Point(5, 3, 0),
            new Point(6, 4, 0),
            new Point(5.5, 5.2, 0),
            new Point(4, 5, 0)
    };
    private Cell[] cells = {
            new Cell(new int[]{0, 1, 4}, VTK_TRIANGLE),
            new Cell(new int[]{1, 2, 3, 4}, VTK_QUAD)
    };
    private ScalarData[] pointScalarData = {
            new ScalarData("Temperature", new double[]{200, 300, 250, 230, 400}),
            new ScalarData("Pressure", new double[]{126920.01, 133028.18, 83980.67, 85415.18, 62601.26})
    };
    private VectorData[] pointVectorData = {
            new VectorData("Velocity", new Vector[]{
                    new Vector(4.12, -0.68, 2.67),
                    new Vector(-2.65, 0.00, 1.14),
                    new Vector(-0.00, 2.09, 0.15),
                    new Vector(-0.73, 0.39, 0.64),
                    new Vector(-0.75, 1.22, -0.33)
            })
    };

    private ScalarData[] cellScalarData = {
            new ScalarData("speed", new double[]{1.5, 20.8})
    };
    private VectorData[] cellVectorData = {
            new VectorData("Vorticity", new Vector[]{
                    new Vector(-6.23, 8.87, -6.28),
                    new Vector(15.79, -14.54, -2.30)
            }),
            new VectorData("Acceleration", new Vector[]{
                    new Vector(0.34, -1.70, 0.72),
                    new Vector(0.33, -1.55, -0.00)
            })
    };

    @Test
    public void writeASCII_withData() {
        writeASCII(true);
    }

    @Test
    public void writeASCII_withoutData() {
        writeASCII(false);
    }

    private void writeASCII(boolean useData) {
        UnstructuredGrid grid;
        if (useData) {
            grid = new UnstructuredGrid(points, cells,
                    pointScalarData, pointVectorData,
                    cellScalarData, cellVectorData);
        } else {
            grid = new UnstructuredGrid(points, cells,
                    null, null,
                    null, null);
        }
        String title = "Test data for unstructured grid ASCII format";
        UnstructuredGridLegacyVtkWriter gridWriter = new UnstructuredGridLegacyVtkWriter(grid, title);
        File outFile = new File("src/test/resources/unstructuredTestASCII.vtk");
        try {
            gridWriter.write(outFile, FileFormat.ASCII);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (Scanner fileScanner = new Scanner(outFile)) {
            assertEquals("# vtk DataFile Version 2.0", fileScanner.nextLine());
            assertEquals(title, fileScanner.nextLine());
            assertEquals("ASCII", fileScanner.nextLine());
            assertEquals("DATASET UNSTRUCTURED_GRID", fileScanner.nextLine());

            assertEquals("POINTS 5 double", fileScanner.nextLine());

            assertEquals(0, points[0].distance(strToPoint(fileScanner.nextLine())), 1e-15);
            assertEquals(0, points[1].distance(strToPoint(fileScanner.nextLine())), 1e-15);
            assertEquals(0, points[2].distance(strToPoint(fileScanner.nextLine())), 1e-15);
            assertEquals(0, points[3].distance(strToPoint(fileScanner.nextLine())), 1e-15);
            assertEquals(0, points[4].distance(strToPoint(fileScanner.nextLine())), 1e-15);

            assertEquals("CELLS 2 9", fileScanner.nextLine());

            assertEquals("3 0 1 4", fileScanner.nextLine());
            assertEquals("4 1 2 3 4", fileScanner.nextLine());

            assertEquals("CELL_TYPES 2", fileScanner.nextLine());

            assertEquals(VTK_TRIANGLE.ID + "", fileScanner.nextLine());
            assertEquals(VTK_QUAD.ID + "", fileScanner.nextLine());

            if (!useData) {
                assertEquals("POINT_DATA 5", fileScanner.nextLine());
                assertEquals("CELL_DATA 2", fileScanner.nextLine());
                return;
            }
            assertEquals("POINT_DATA 5", fileScanner.nextLine());
            assertEquals("SCALARS Temperature double 1", fileScanner.nextLine());
            assertEquals("LOOKUP_TABLE default", fileScanner.nextLine());
            for (int i = 0; i < 5; i++) {
                assertEquals(pointScalarData[0].scalars[i], fileScanner.nextDouble(), 1e-15);
            }
            fileScanner.nextLine();

            assertEquals("SCALARS Pressure double 1", fileScanner.nextLine());
            assertEquals("LOOKUP_TABLE default", fileScanner.nextLine());
            for (int i = 0; i < 5; i++) {
                assertEquals(pointScalarData[1].scalars[i], fileScanner.nextDouble(), 1e-15);
            }
            fileScanner.nextLine();

            assertEquals("VECTORS Velocity double", fileScanner.nextLine());
            for (Vector vector : pointVectorData[0].vectors) {
                assertEquals(vector.x, fileScanner.nextDouble(), 1e-15);
                assertEquals(vector.y, fileScanner.nextDouble(), 1e-15);
                assertEquals(vector.z, fileScanner.nextDouble(), 1e-15);
            }
            fileScanner.nextLine();

            assertEquals("CELL_DATA 2", fileScanner.nextLine());
            assertEquals(String.format("SCALARS %s double 1", cellScalarData[0].dataName), fileScanner.nextLine());
            assertEquals("LOOKUP_TABLE default", fileScanner.nextLine());
            for (int i = 0; i < 2; i++) {
                assertEquals(cellScalarData[0].scalars[i], fileScanner.nextDouble(), 1e-15);
            }
            fileScanner.nextLine();

            assertEquals("VECTORS Vorticity double", fileScanner.nextLine());
            for (Vector vector : cellVectorData[0].vectors) {
                assertEquals(vector.x, fileScanner.nextDouble(), 1e-15);
                assertEquals(vector.y, fileScanner.nextDouble(), 1e-15);
                assertEquals(vector.z, fileScanner.nextDouble(), 1e-15);
            }
            fileScanner.nextLine();

            assertEquals("VECTORS Acceleration double", fileScanner.nextLine());
            for (Vector vector : cellVectorData[1].vectors) {
                assertEquals(vector.x, fileScanner.nextDouble(), 1e-15);
                assertEquals(vector.y, fileScanner.nextDouble(), 1e-15);
                assertEquals(vector.z, fileScanner.nextDouble(), 1e-15);
            }
            fileScanner.nextLine();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Point strToPoint(String xyzStr) {
        String[] xyz = xyzStr.trim().split("\\s+");
        return new Point(Double.valueOf(xyz[0]), Double.valueOf(xyz[1]), Double.valueOf(xyz[2]));
    }

    @Test
    public void writeBinary_withData() {
        writeBINARY(true);
    }

    @Test
    public void writeBinary_withoutData() {
        writeBINARY(false);
    }

    private void writeBINARY(boolean useData) {
        UnstructuredGrid grid;
        if (useData) {
            grid = new UnstructuredGrid(points, cells,
                    pointScalarData, pointVectorData,
                    cellScalarData, cellVectorData);
        } else {
            grid = new UnstructuredGrid(points, cells,
                    null, null,
                    null, null);
        }
        String title = "Test data for unstructured grid ASCII format";
        UnstructuredGridLegacyVtkWriter gridWriter = new UnstructuredGridLegacyVtkWriter(grid, title);
        File outFile = new File("src/test/resources/unstructuredTestBINARY.vtk");
        try {
            gridWriter.write(outFile, FileFormat.BINARY);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (DataInputStream fileStream = new DataInputStream(new FileInputStream(outFile))) {
            assertEquals("# vtk DataFile Version 2.0", readLine(fileStream));
            assertEquals(title, readLine(fileStream));
            assertEquals("BINARY", readLine(fileStream));
            assertEquals("DATASET UNSTRUCTURED_GRID", readLine(fileStream));

            assertEquals("POINTS 5 double", readLine(fileStream));

            assertEquals(0, points[0].distance(readPoint(fileStream)), 1e-15);
            assertEquals(0, points[1].distance(readPoint(fileStream)), 1e-15);
            assertEquals(0, points[2].distance(readPoint(fileStream)), 1e-15);
            assertEquals(0, points[3].distance(readPoint(fileStream)), 1e-15);
            assertEquals(0, points[4].distance(readPoint(fileStream)), 1e-15);

            assertEquals("CELLS 2 9", readLine(fileStream));
            assertEquals(3, fileStream.readInt());
            assertEquals(0, fileStream.readInt());
            assertEquals(1, fileStream.readInt());
            assertEquals(4, fileStream.readInt());

            assertEquals(4, fileStream.readInt());
            assertEquals(1, fileStream.readInt());
            assertEquals(2, fileStream.readInt());
            assertEquals(3, fileStream.readInt());
            assertEquals(4, fileStream.readInt());

            assertEquals("CELL_TYPES 2", readLine(fileStream));

            assertEquals(VTK_TRIANGLE.ID, fileStream.readInt());
            assertEquals(VTK_QUAD.ID, fileStream.readInt());

            if (!useData) {
                assertEquals("POINT_DATA 5", readLine(fileStream));
                assertEquals("CELL_DATA 2", readLine(fileStream));
                return;
            }

            assertEquals("POINT_DATA 5", readLine(fileStream));
            assertEquals("SCALARS Temperature double 1", readLine(fileStream));
            assertEquals("LOOKUP_TABLE default", readLine(fileStream));
            for (int i = 0; i < 5; i++) {
                assertEquals(pointScalarData[0].scalars[i], fileStream.readDouble(), 1e-15);
            }
            assertEquals("SCALARS Pressure double 1", readLine(fileStream));
            assertEquals("LOOKUP_TABLE default", readLine(fileStream));
            for (int i = 0; i < 5; i++) {
                assertEquals(pointScalarData[1].scalars[i], fileStream.readDouble(), 1e-15);
            }

            assertEquals("VECTORS Velocity double", readLine(fileStream));
            for (Vector vector : pointVectorData[0].vectors) {
                assertEquals(vector.x, fileStream.readDouble(), 1e-15);
                assertEquals(vector.y, fileStream.readDouble(), 1e-15);
                assertEquals(vector.z, fileStream.readDouble(), 1e-15);
            }

            assertEquals("CELL_DATA 2", readLine(fileStream));
            assertEquals(String.format("SCALARS %s double 1", cellScalarData[0].dataName), readLine(fileStream));
            assertEquals("LOOKUP_TABLE default", readLine(fileStream));
            for (int i = 0; i < 2; i++) {
                assertEquals(cellScalarData[0].scalars[i], fileStream.readDouble(), 1e-15);
            }

            assertEquals("VECTORS Vorticity double", readLine(fileStream));
            for (Vector vector : cellVectorData[0].vectors) {
                assertEquals(vector.x, fileStream.readDouble(), 1e-15);
                assertEquals(vector.y, fileStream.readDouble(), 1e-15);
                assertEquals(vector.z, fileStream.readDouble(), 1e-15);
            }

            assertEquals("VECTORS Acceleration double", readLine(fileStream));
            for (Vector vector : cellVectorData[1].vectors) {
                assertEquals(vector.x, fileStream.readDouble(), 1e-15);
                assertEquals(vector.y, fileStream.readDouble(), 1e-15);
                assertEquals(vector.z, fileStream.readDouble(), 1e-15);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Point readPoint(DataInputStream fileStream) throws IOException {
        double x = fileStream.readDouble();
        double y = fileStream.readDouble();
        double z = fileStream.readDouble();

        return new Point(x, y, z);
    }

    private String readLine(DataInputStream fileStream) throws IOException {
        StringBuilder str = new StringBuilder();
        while (true) {
            int c = fileStream.read();
            if (c == -1) {
                break; // End of stream while reading line (EOF)
            }
            if (c == '\n') {
                break;
            } else {
                str.append((char) c);
            }
        }
        return str.toString();
    }
}