import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class XMLtoJSONConverter {
    public static void main(String[] args) {
        try {

            File xmlFile = new File("src/main/java/xmlResponse.xml");
            XmlMapper xmlMapper = new XmlMapper();
            JsonNode jsonNodeFromXml = xmlMapper.readTree(xmlFile);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            String jsonString = objectMapper.writeValueAsString(jsonNodeFromXml);

            File jsonOutputFile = new File("src/main/java/outputJson.json");
            objectMapper.writeValue(jsonOutputFile, jsonNodeFromXml);

            System.out.println("JSON content has been written to " + jsonOutputFile.getAbsolutePath());

            File referenceJsonFile = new File("src/main/java/jsonResponse.json"); // Replace with the path to your reference JSON file
            JsonNode jsonNodeFromReference = objectMapper.readTree(referenceJsonFile);

            // Compare the two json files and print the differences
            boolean areEqual = compareJsonNodes(jsonNodeFromXml, jsonNodeFromReference);

            if (areEqual) {
                System.out.println("The JSON files are identical.");
            } else {
                System.out.println("The JSON files are different.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean compareJsonNodes(JsonNode node1, JsonNode node2) {
        if (!node1.equals(node2)) {
            System.out.println("Differences found:");
            printJsonNodeDifferences("", node1, node2);
            return false;
        }
        return true;
    }

    private static void printJsonNodeDifferences(String currentPath, JsonNode node1, JsonNode node2) {
        if (node1.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields1 = node1.fields();
            Iterator<Map.Entry<String, JsonNode>> fields2 = node2.fields();

            while (fields1.hasNext() && fields2.hasNext()) {
                Map.Entry<String, JsonNode> field1 = fields1.next();
                Map.Entry<String, JsonNode> field2 = fields2.next();

                String key = field1.getKey();

                if (!key.equals(field2.getKey())) {
                    System.out.println("Key mismatch: " + currentPath + key + " vs. " + currentPath + field2.getKey());
                } else {
                    printJsonNodeDifferences(currentPath + key + ".", field1.getValue(), field2.getValue());
                }
            }
        } else if (node1.isArray() && node2.isArray()) {
            int size1 = node1.size();
            int size2 = node2.size();
            int commonSize = Math.min(size1, size2);

            for (int i = 0; i < commonSize; i++) {
                printJsonNodeDifferences(currentPath + "[" + i + "].", node1.get(i), node2.get(i));
            }

            for (int i = commonSize; i < size1; i++) {
                System.out.println("Key mismatch: " + currentPath + "[" + i + "] does not exist in the second JSON.");
            }

            for (int i = commonSize; i < size2; i++) {
                System.out.println("Key mismatch: " + currentPath + "[" + i + "] does not exist in the first JSON.");
            }
        } else if (!node1.equals(node2)) {
            System.out.println("Value mismatch: " + currentPath + node1.toString() + " vs. " + currentPath + node2.toString());
        }
    }
}
