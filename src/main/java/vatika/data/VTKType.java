package vatika.data;

import java.util.Arrays;

public enum VTKType {
    VTK_VERTEX(1),
    VTK_POLY_VERTEX(2),
    VTK_LINE(3),
    VTK_POLY_LINE(4),
    VTK_TRIANGLE(5),
    VTK_TRIANGLE_STRIP(6),
    VTK_POLYGON(7),
    VTK_PIXEL(8),
    VTK_QUAD(9),
    VTK_TETRA(10),
    VTK_VOXEL(11),
    VTK_HEXAHEDRON(12),
    VTK_WEDGE(13),
    VTK_PYRAMID(14),
    VTK_QUADRATIC_EDGE(21),
    VTK_QUADRATIC_TRIANGLE(22),
    VTK_QUADRATIC_QUAD(23),
    VTK_QUADRATIC_TETRA(24),
    VTK_QUADRATIC_HEXAHEDRON(25);

    public final int ID;

    VTKType(int id) {
        this.ID = id;
    }

    public static VTKType get(int id) {
        return Arrays.stream(VTKType.values())
                .filter(e -> e.ID == id)
                .findAny().orElseThrow();
    }
}
