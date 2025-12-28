package use_case.util;

public class MathUtil {
    public static int roundByFactor(int value, int factor) {
        return Math.round((float) value / factor) * factor;
    }

    public static int floorByFactor(double value, int factor) {
        return (int) (Math.floor(value / factor) * factor);
    }

    public static int ceilByFactor(double value, int factor) {
        return (int) (Math.ceil(value / factor) * factor);
    }
}