package vatika.data;

public class Point {
    public final double x;
    public final double y;
    public final double z;

    public Point(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double distance(Point p) {
        double dx = p.x - x;
        double dy = p.y - y;
        double dz = p.z - z;

        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
