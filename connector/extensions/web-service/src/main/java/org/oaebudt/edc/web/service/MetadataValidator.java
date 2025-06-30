/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.oaebudt.edc.web.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class MetadataValidator {

    private static final Pattern EMAIL_REGEX = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE
    );

    public static ValidationResult validateMetadata(Map<String, Object> metadata) {
        ValidationResult result = ValidationResult.ok();

        List<ValidationResult> validationResults = new ArrayList<>();

        validationResults.add(validateRequiredString(metadata, "legalOrganizationName"));
        validationResults.add(validateRequiredString(metadata, "countryOfOrganization"));
        validationResults.add(validateRequiredString(metadata, "contactPerson"));
        validationResults.add(validateRequiredString(metadata, "dataProcessingDescription"));
        validationResults.add(validateRequiredString(metadata, "qualityAssuranceMeasures"));
        validationResults.add(validateRequiredString(metadata, "dataLicensingTerms"));

        validationResults.add(validateUrl(metadata, "organizationWebsite"));
        validationResults.add(validateEmail(metadata, "contactEmail"));

        validationResults.add(validateLevel(metadata, "dataAccuracyLevel"));
        validationResults.add(validateLevel(metadata, "dataGenerationTransparencyLevel"));
        validationResults.add(validateLevel(metadata, "dataDeliveryReliabilityLevel"));
        validationResults.add(validateLevel(metadata, "dataFrequencyLevel"));
        validationResults.add(validateLevel(metadata, "dataGranularityLevel"));
        validationResults.add(validateLevel(metadata, "dataConsistencyLevel"));

        return validationResults.stream().reduce(result, (a, b) -> {
            a.merge(b);
            return a;
        });
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
