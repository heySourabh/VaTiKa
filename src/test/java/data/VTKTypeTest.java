package data;

import org.junit.Test;

import static data.VTKType.*;
import static org.junit.Assert.*;

public class VTKTypeTest {

    @Test
    public void get() {
        assertEquals(VTK_VERTEX, VTKType.get(1));
        assertEquals(VTK_POLY_VERTEX, VTKType.get(2));
        assertEquals(VTK_LINE, VTKType.get(3));
        assertEquals(VTK_POLY_LINE, VTKType.get(4));
        assertEquals(VTK_TRIANGLE, VTKType.get(5));
        assertEquals(VTK_TRIANGLE_STRIP, VTKType.get(6));
        assertEquals(VTK_POLYGON, VTKType.get(7));
        assertEquals(VTK_PIXEL, VTKType.get(8));
        assertEquals(VTK_QUAD, VTKType.get(9));
        assertEquals(VTK_TETRA, VTKType.get(10));
        assertEquals(VTK_VOXEL, VTKType.get(11));
        assertEquals(VTK_HEXAHEDRON, VTKType.get(12));
        assertEquals(VTK_WEDGE, VTKType.get(13));
        assertEquals(VTK_PYRAMID, VTKType.get(14));
        assertEquals(VTK_QUADRATIC_EDGE, VTKType.get(21));
        assertEquals(VTK_QUADRATIC_TRIANGLE, VTKType.get(22));
        assertEquals(VTK_QUADRATIC_QUAD, VTKType.get(23));
        assertEquals(VTK_QUADRATIC_TETRA, VTKType.get(24));
        assertEquals(VTK_QUADRATIC_HEXAHEDRON, VTKType.get(25));
    }
}