package org.oaebudt.edc.web.service;

import java.net.URI;
import java.util.Map;
import java.util.regex.Pattern;

public class MetadataValidator {

    private static final Pattern EMAIL_REGEX = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE
    );

    public static ValidationResult validate(Map<String, Object> metadata) {
        ValidationResult result = ValidationResult.ok();

        result.merge(validateRequiredString(metadata, "legalOrganizationName"));
        result.merge(validateRequiredString(metadata, "countryOfOrganization"));
        result.merge(validateRequiredString(metadata, "contactPerson"));
        result.merge(validateRequiredString(metadata, "dataProcessingDescription"));
        result.merge(validateRequiredString(metadata, "qualityAssuranceMeasures"));
        result.merge(validateRequiredString(metadata, "dataLicensingTerms"));

        result.merge(validateUrl(metadata, "organizationWebsite"));
        result.merge(validateEmail(metadata, "contactEmail"));

        result.merge(validateLevel(metadata, "dataAccuracyLevel"));
        result.merge(validateLevel(metadata, "dataGenerationTransparencyLevel"));
        result.merge(validateLevel(metadata, "dataDeliveryReliabilityLevel"));
        result.merge(validateLevel(metadata, "dataFrequencyLevel"));
        result.merge(validateLevel(metadata, "dataGranularityLevel"));
        result.merge(validateLevel(metadata, "dataConsistencyLevel"));

        return result;
    }


    private static ValidationResult validateRequiredString(Map<String, Object> map, String key) {
        if (!map.containsKey(key) || !(map.get(key) instanceof String) || ((String) map.get(key)).trim().isEmpty()) {
            return ValidationResult.fail(key + " is required and must be a non-empty string");
        }
        return ValidationResult.ok();
    }

    private static ValidationResult validateUrl(Map<String, Object> map, String key) {
        try {
            URI.create((String) map.get(key)).toURL();
        } catch (Exception e) {
            return ValidationResult.fail(key + " must be a valid URL");
        }
        return ValidationResult.ok();
    }

    private static ValidationResult validateEmail(Map<String, Object> map, String key) {
        String email = (String) map.get(key);
        if (email == null || !EMAIL_REGEX.matcher(email).matches()) {
            return ValidationResult.fail(key + " must be a valid email address");
        }
        return ValidationResult.ok();
    }

    private static ValidationResult validateLevel(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (!(value instanceof Integer) || ((int) value < 1 || (int) value > 3)) {
            return ValidationResult.fail(key + " must be an integer between 1 and 3");
        }
        return ValidationResult.ok();
    }
}
