import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class LDPtxt {

    static List<RouteData> routeDataList = new ArrayList<>();
    static List<CityData> cityDataList = new ArrayList<>();

    public static void main(String[] args) {
        String txtFilePath1 = "Gowtham_Florida_Cities.txt"; // Change to the actual path of your Cities file
        String txtFilePath2 = "Gowtham_Florida_Weather.txt"; // Change to the actual path of your Weather file

        String[] user = txtFilePath1.split("_");

        boolean citiesDataFormatPassed = checkCitiesDataFormat(txtFilePath1);
        boolean weatherDataFormatPassed = checkWeatherDataFormat(txtFilePath2);

        if (citiesDataFormatPassed) {
            System.out.println(user[0] + " has submitted Cities Data in an acceptable format for " + user[1] + " state in Comma Seperated Values");
            System.out.println("Cities");
            System.out.println("------");
            cityDataList.forEach(cityData -> System.out.println(cityData.getCityName()));
            System.out.println("------");
            System.out.println(routeDataList.size() + " Entries are added to Route Data List");
        }
        if (weatherDataFormatPassed) {
            System.out.println(user[0] + " has submitted Weather Data in an acceptable format for " + user[1] + " state in Comma Seperated Values");
            System.out.println(cityDataList.size() + " Entries are added to Weather Data List");
        }
    }

    private static boolean checkCitiesDataFormat(String txtFilePath) {
        boolean formatPassed = true;
        boolean returnformatPassed = true;

        try (Scanner scanner = new Scanner(new File(txtFilePath))) {
            String headerLine = scanner.nextLine();
            if (headerLine != null) {
                String[] headers = headerLine.split(",");
                String line;

                Set<String> column2Values = new HashSet<>();
                Set<String> column3Values = new HashSet<>();

                while (scanner.hasNextLine()) {
                    line = scanner.nextLine();
                    String[] record = line.split(",");

                    if (record.length == headers.length) {
                        String column2Value = record[1].trim();
                        String column3Value = record[2].trim();

                        switch (headers[1].trim()) {
                            case "City_Origin":
                                formatPassed = checkString(column2Value, headers[1].trim());
                                if (!formatPassed) returnformatPassed = false;
                                break;
                            default:
                                System.out.println("Unexpected column: " + headers[1].trim());
                                returnformatPassed = false;
                                break;
                        }

                        switch (headers[2].trim()) {
                            case "City_Destination":
                                formatPassed = checkString(column3Value, headers[2].trim());
                                if (!formatPassed) returnformatPassed = false;
                                break;
                            default:
                                System.out.println("Unexpected column: " + headers[2].trim());
                                returnformatPassed = false;
                                break;
                        }

                        column2Values.add(column2Value);
                        column3Values.add(column3Value);
                    } else {
                        System.out.println("Incorrect number of columns in the record.");
                        returnformatPassed = false;
                    }

                    RouteData routeData = new RouteData(record[0], record[1], record[2],
                            Double.parseDouble(record[3]), Double.parseDouble(record[4]),
                            Double.parseDouble(record[5]));
                    routeDataList.add(routeData);
                }

                // Check if every unique value in column2 is mapped with every unique value in column3
                for (String cityOrigin : column2Values) {
                    for (String cityDestination : column3Values) {
                        if (!cityOrigin.equals(cityDestination)) {
                            boolean isMapped = false;

                            for (RouteData routeData : routeDataList) {
                                if (routeData.getCityOrigin().equals(cityOrigin) && routeData.getCityDestination().equals(cityDestination)) {
                                    isMapped = true;
                                    break;
                                }
                            }

                            if (!isMapped) {
                                System.out.println("City mapping issue: " + cityOrigin + " is not mapped to " + cityDestination);
                                returnformatPassed = false;
                            }
                        }
                    }
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            returnformatPassed = false;
        }

        return returnformatPassed;
    }

    private static boolean checkWeatherDataFormat(String txtFilePath) {
    boolean formatPassed = true;
    boolean returnformatPassed = true;

    try (Scanner scanner = new Scanner(new File(txtFilePath))) {
        String headerLine = scanner.nextLine();
        if (headerLine != null) {
            String[] expectedHeaders = {
                "City Name", "11/11/2023 12AM-01AM", "11/11/2023 01AM-02AM",
       "11/11/2023 02AM-03AM", "11/11/2023 03AM-04AM", "11/11/2023 04AM-05AM",
       "11/11/2023 05AM-06AM", "11/11/2023 06AM-07AM", "11/11/2023 07AM-08AM",
       "11/11/2023 08AM-09AM", "11/11/2023 09AM-10AM", "11/11/2023 10AM-11AM",
       "11/11/2023 11AM-12PM", "11/11/2023 12PM-01PM", "11/11/2023 01PM-02PM",
       "11/11/2023 02PM-03PM", "11/11/2023 03PM-04PM", "11/11/2023 04PM-05PM",
       "11/11/2023 05PM-06PM", "11/11/2023 06PM-07PM", "11/11/2023 07PM-08PM",
       "11/11/2023 08PM-09PM", "11/11/2023 09PM-10PM", "11/11/2023 10PM-11PM",
       "11/11/2023 11PM-12AM", "11/12/2023 12AM-01AM", "11/12/2023 01AM-02AM",
       "11/12/2023 02AM-03AM", "11/12/2023 03AM-04AM", "11/12/2023 04AM-05AM",
       "11/12/2023 05AM-06AM", "11/12/2023 06AM-07AM", "11/12/2023 07AM-08AM",
       "11/12/2023 08AM-09AM", "11/12/2023 09AM-10AM", "11/12/2023 10AM-11AM",
       "11/12/2023 11AM-12PM", "11/12/2023 12PM-01PM", "11/12/2023 01PM-02PM",
       "11/12/2023 02PM-03PM", "11/12/2023 03PM-04PM", "11/12/2023 04PM-05PM",
       "11/12/2023 05PM-06PM", "11/12/2023 06PM-07PM", "11/12/2023 07PM-08PM",
       "11/12/2023 08PM-09PM", "11/12/2023 09PM-10PM", "11/12/2023 10PM-11PM",
       "11/12/2023 11PM-12AM"
            };
            String[] headers = headerLine.split(",");

            // Check if the column names match
            if (!Arrays.equals(expectedHeaders, headers)) {
                System.out.println("Column names mismatch in the Weather file.");
                returnformatPassed = false;
            }

            String line;
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                String[] record = line.split(",");
                for (int i = 0; i <= 48; i++) {
                    String columnName = headers[i].trim();
                    String value = record[i].trim();
                    formatPassed = checkString(value, columnName);
                    if (!formatPassed) returnformatPassed = false;
                }

                CityData cityData = new CityData(record[0], Arrays.asList(record));
                cityDataList.add(cityData);
            }
        }
    } catch (FileNotFoundException e) {
        e.printStackTrace();
        formatPassed = false;
    }

    return returnformatPassed;
}

    private static boolean checkString(String value, String columnName) {
        if (value == null || value.trim().isEmpty()) {
            System.out.println("Null or empty value found in column " + columnName);
            return false;
        } else if (!value.matches("[a-zA-Z ]+")) {
            System.out.println("Mismatch in data type for column " + columnName + ": Expected String, Actual " + value);
            return false;
        }
        return true;
    }
}
