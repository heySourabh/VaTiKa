package vatika.data;

import org.junit.Test;

import static org.junit.Assert.*;

public class PointTest {

    @Test
    public void distance() {
        Point p1 = new Point(1, 2, 5);
        Point p2 = new Point(23, 67, 98);

        assertEquals(p1.distance(p2), p2.distance(p1), 1e-15);
        assertEquals(115.57681428383462759, p1.distance(p2), 1e-15);
    }
}
