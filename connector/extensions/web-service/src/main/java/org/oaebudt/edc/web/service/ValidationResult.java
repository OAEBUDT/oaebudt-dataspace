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

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {

    private final List<String> errors = new ArrayList<>();

    public void addError(String message) {
        errors.add(message);
    }

    public void merge(ValidationResult other) {
        errors.addAll(other.getErrors());
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public boolean failed() {
        return !isValid();
    }

    public List<String> getErrors() {
        return errors;
    }

    public static ValidationResult ok() {
        return new ValidationResult();
    }

    public static ValidationResult fail(String message) {
        ValidationResult result = new ValidationResult();
        result.addError(message);
        return result;
    }
}
