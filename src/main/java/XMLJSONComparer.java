import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;

public class XMLJSONComparer {
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

            // Compare XML and JSON key-value pairs
            boolean areFilesDifferent = compareKeyValuePairs(xmlDoc.getDocumentElement(), jsonNode);

            if (areFilesDifferent) {
                System.out.println("The XML and JSON files are different.");
            } else {
                System.out.println("The XML and JSON files are the same.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean compareKeyValuePairs(Element xmlElement, JsonNode jsonNode) {
        NodeList xmlChildren = xmlElement.getChildNodes();
        boolean areDifferent = false;

        for (int i = 0; i < xmlChildren.getLength(); i++) {
            Node xmlChild = xmlChildren.item(i);

            if (xmlChild.getNodeType() == Node.ELEMENT_NODE) {
                String key = xmlChild.getNodeName();
                String xmlValue = xmlChild.getTextContent().trim();
                JsonNode jsonValue = jsonNode.get(key);

                if (jsonValue == null) {
                    System.out.println("Key not found in JSON: " + key);
                    areDifferent = true;
                } else {
                    if (jsonValue.isObject() && xmlChild.hasChildNodes()) {
                        if (compareKeyValuePairs((Element) xmlChild, jsonValue)) {
                            areDifferent = true;
                        }
                    } else {
                        String jsonTextValue = jsonValue.asText().trim();
                        if (!xmlValue.equals(jsonTextValue)) {
                            System.out.println("Key: " + key + " - XML: " + xmlValue + ", JSON: " + jsonTextValue);
                            areDifferent = true;
                        }
                    }
                }
            }
        }

        return areDifferent;
    }
}
