import java.util.List;

public class CityData {
    private String cityName;
    private List<String> values; // Assuming the values from 1 to 48 are integers

    public CityData(String cityName, List<String> values) {
        this.cityName = cityName;
        this.values = values;
    }
    
    public String getCityName() {
        return cityName;
    }

    public List<String> getValues() {
        return values;
    }

    @Override
    public String toString() {
        return "CityData{" +
                "cityName='" + cityName + '\'' +
                ", values=" + values +
                '}';
    }
}