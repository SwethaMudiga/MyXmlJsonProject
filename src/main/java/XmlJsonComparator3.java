import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class XmlJsonComparator3 {
    public static void main(String[] args) {
        try {
            // Load the XML file
            File xmlFile = new File("src/main/java/xmlResponse.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document xmlDoc = dBuilder.parse(xmlFile);
            xmlDoc.getDocumentElement().normalize();

            // Load the JSON file
            ObjectMapper objectMapper = new ObjectMapper();
            File jsonFile = new File("src/main/java/jsonResponse.json");
            JsonNode jsonNode = objectMapper.readTree(jsonFile);

            // Compare XML and JSON data
            List<String> differences = new ArrayList<>();
            boolean areFilesDifferent = compareXMLWithJSON(xmlDoc.getDocumentElement(), jsonNode, differences);

            if (areFilesDifferent) {
                System.out.println("Differences found:");
                for (String diff : differences) {
                    System.out.println(diff);
                }
                System.out.println("The XML and JSON files are different.");
            } else {
                System.out.println("The XML and JSON files are the same.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean compareXMLWithJSON(Element xmlElement, JsonNode jsonNode, List<String> differences) {
        NodeList xmlChildren = xmlElement.getChildNodes();

        for (int i = 0; i < xmlChildren.getLength(); i++) {
            Node xmlChild = xmlChildren.item(i);

            if (xmlChild.getNodeType() == Node.ELEMENT_NODE) {
                String key = xmlChild.getNodeName();
                String xmlValue = xmlChild.getTextContent().trim();
                JsonNode jsonValue = jsonNode.get(key);

                if (jsonValue == null) {
                    differences.add("Key not found in JSON: " + key);
                } else {
                    if (jsonValue.isArray()) {
                        // Handle arrays
                        boolean areArraysDifferent = compareXMLArrayWithJSONArray(xmlChild, jsonValue, differences);
                        if (!areArraysDifferent) {
                            return false;
                        }
                    } else if (jsonValue.isObject()) {
                        // Handle objects
                        boolean areObjectsDifferent = compareXMLWithJSON((Element) xmlChild, jsonValue, differences);
                        if (!areObjectsDifferent) {
                            return false;
                        }
                    } else {
                        // Handle primitive values
                        String jsonTextValue = jsonValue.asText().trim();
                        if (!xmlValue.equals(jsonTextValue)) {
                            differences.add("Key: " + key + " - XML: " + xmlValue + ", JSON: " + jsonTextValue);
                        }
                    }
                }
            }
        }

        return differences.isEmpty();
    }

    private static boolean compareXMLArrayWithJSONArray(Node xmlNode, JsonNode jsonArray, List<String> differences) {
        NodeList xmlArrayChildren = xmlNode.getChildNodes();

        for (int i = 0; i < xmlArrayChildren.getLength(); i++) {
            Node xmlChild = xmlArrayChildren.item(i);

            if (xmlChild.getNodeType() == Node.ELEMENT_NODE) {
                String xmlValue = xmlChild.getTextContent().trim();

                boolean isMatchFound = false;

                for (JsonNode jsonElement : jsonArray) {
                    if (jsonElement.isObject()) {
                        // Handle objects
                        boolean areObjectsDifferent = compareXMLWithJSON((Element) xmlChild, jsonElement, differences);
                        if (!areObjectsDifferent) {
                            isMatchFound = true;
                            break;
                        }
                    } else {
                        // Handle primitive values
                        String jsonTextValue = jsonElement.asText().trim();
                        if (xmlValue.equals(jsonTextValue)) {
                            isMatchFound = true;
                            break;
                        }
                    }
                }

                if (!isMatchFound) {
                    differences.add("No matching element found in JSON array for key: " + xmlNode.getNodeName());
                }
            }
        }

        return differences.isEmpty();
    }
}
