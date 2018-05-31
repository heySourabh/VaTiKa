package writer;

import data.*;
import org.junit.Test;

import java.io.File;

import static data.VTKType.VTK_QUAD;
import static data.VTKType.VTK_TRIANGLE;

public class UnstructuredGridXmlVtKWriterTest {

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
    public void write() {
        UnstructuredGrid grid = new UnstructuredGrid(points, cells, pointScalarData, pointVectorData, cellScalarData, cellVectorData);
        File outFile = new File("src/test/resources/unstructuredTestXML.vtu");
        try {
            new UnstructuredGridXmlVtKWriter(grid).write(outFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*
            The output is visually tested using Paraview software
        */
    }
}