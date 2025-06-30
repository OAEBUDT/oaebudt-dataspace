#!/bin/bash

# Apache 2.0 license header
read -r -d '' LICENSE_HEADER <<'EOF'
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
EOF

# Find all .java files excluding build, target, and generated folders
find . -type f -name "*.java" \
  ! -path "*/build/*" \
  ! -path "*/target/*" \
  ! -path "*/out/*" \
  | while read -r file; do
    # Skip files that already contain "Licensed under the Apache License"
    if grep -q "Licensed under the Apache License" "$file"; then
      echo "Skipping (already licensed): $file"
    else
      echo "Adding license to: $file"
      tmpfile=$(mktemp)
      echo "$LICENSE_HEADER" > "$tmpfile"
      echo "" >> "$tmpfile"
      cat "$file" >> "$tmpfile"
      mv "$tmpfile" "$file"
    fi
done

echo "✅ License headers added where needed."
