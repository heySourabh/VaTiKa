package writer;

import data.Cell;
import data.Point;
import data.UnstructuredGrid;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import static data.VTKType.VTK_QUAD;
import static data.VTKType.VTK_TRIANGLE;
import static org.junit.Assert.assertEquals;

public class UnstructuredGridWriterTest {

    @org.junit.Test
    public void writeLegacy() {
        Point[] points = {
                new Point(2, 3, 0),
                new Point(5, 3, 0),
                new Point(6, 4, 0),
                new Point(5.5, 5.2, 0),
                new Point(4, 5, 0)
        };
        Cell[] cells = {
                new Cell(new int[]{0, 1, 4}, VTK_TRIANGLE),
                new Cell(new int[]{1, 2, 3, 4}, VTK_QUAD)
        };
        UnstructuredGrid grid = new UnstructuredGrid(points, cells);
        String title = "Test data for unstructured grid";
        UnstructuredGridWriter gridWriter = new UnstructuredGridWriter(grid, title);
        File outFile = new File("src/test/resources/unstructuredTestOut.vtk");
        try {
            gridWriter.writeLegacy(outFile, FileFormat.ASCII);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Scanner fileScanner = new Scanner(outFile);
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

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Point strToPoint(String xyzStr) {
        String[] xyz = xyzStr.trim().split("\\s+");
        return new Point(Double.valueOf(xyz[0]), Double.valueOf(xyz[1]), Double.valueOf(xyz[2]));
    }
}