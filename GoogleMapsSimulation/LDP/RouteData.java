import java.util.List;

public class RouteData {
    private String state;
    private String cityOrigin;
    private String cityDestination;
    private double distance;
    private double speed;
    private double seaLevelGradient;

    public RouteData(String state, String cityOrigin, String cityDestination, double distance, double speed, double seaLevelGradient) {
        this.state = state;
        this.cityOrigin = cityOrigin;
        this.cityDestination = cityDestination;
        this.distance = distance;
        this.speed = speed;
        this.seaLevelGradient = seaLevelGradient;
    }
    
    // Getter methods
    public String getState() {
        return state;
    }

    public String getCityOrigin() {
        return cityOrigin;
    }

    public String getCityDestination() {
        return cityDestination;
    }

    public double getDistance() {
        return distance;
    }

    public double getSpeed() {
        return speed;
    }

    public double getSeaLevelGradient() {
        return seaLevelGradient;
    }

    @Override
    public String toString() {
        return "RouteData{" +
                "state='" + state + '\'' +
                ", cityOrigin='" + cityOrigin + '\'' +
                ", cityDestination='" + cityDestination + '\'' +
                ", distance=" + distance +
                ", speed=" + speed +
                ", seaLevelGradient=" + seaLevelGradient +
                '}';
    }
}