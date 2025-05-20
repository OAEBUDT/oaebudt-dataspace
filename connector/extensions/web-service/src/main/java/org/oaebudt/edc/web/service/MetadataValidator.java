package org.oaebudt.edc.web.service;


import java.net.URL;
import java.util.Map;
import java.util.regex.Pattern;

public class MetadataValidator {

    private static final Pattern EMAIL_REGEX = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE
    );


    public static void validate(Map<String, Object> metadata) {

        validateRequiredString(metadata, "legalOrganizationName");
        validateRequiredString(metadata, "countryOfOrganization");
        validateRequiredString(metadata, "contactPerson");
        validateRequiredString(metadata, "dataProcessingDescription");
        validateRequiredString(metadata, "qualityAssuranceMeasures");
        validateRequiredString(metadata, "dataLicensingTerms");

        validateUrl(metadata, "organizationWebsite");
        validateEmail(metadata, "contactEmail");

        validateLevel(metadata, "dataAccuracyLevel");
        validateLevel(metadata, "dataGenerationTransparencyLevel");
        validateLevel(metadata, "dataDeliveryReliabilityLevel");
        validateLevel(metadata, "dataFrequencyLevel");
        validateLevel(metadata, "dataGranularityLevel");
        validateLevel(metadata, "dataConsistencyLevel");
    }


    private static void validateRequiredString(Map<String, Object> map, String key) {
        if (!map.containsKey(key) || !(map.get(key) instanceof String) || ((String) map.get(key)).trim().isEmpty()) {
            throw new IllegalArgumentException(key + " is required and must be a non-empty string");
        }
    }

    private static void validateUrl(Map<String, Object> map, String key) {
        try {
            new URL((String) map.get(key));
        } catch (Exception e) {
            throw new IllegalArgumentException(key + " must be a valid URL");
        }
    }

    private static void validateEmail(Map<String, Object> map, String key) {
        String email = (String) map.get(key);
        if (email == null || !EMAIL_REGEX.matcher(email).matches()) {
            throw new IllegalArgumentException(key + " must be a valid email address");
        }
    }

    private static void validateLevel(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (!(value instanceof Integer) || ((int) value < 1 || (int) value > 3)) {
            throw new IllegalArgumentException(key + " must be an integer between 1 and 3");
        }
    }
}
