#!/bin/bash

HEADER='/*
 * Copyright 2026 Moshu
 *
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
 *
 * ---
 *
 * This software is also subject to the Commons Clause License Condition v1.0.
 * You may obtain a copy of the Commons Clause at:
 * https://commonsclause.com/
 */
'

directories=("src" "test")
for dir in "${directories[@]}"; do
    if [ -d "$dir" ]; then
        find "$dir" -name "*.java" | while read file; do
            if ! grep -q "Licensed under the Apache License, Version 2.0" "$file"; then
                if grep -q "PolyForm Noncommercial License 1.0.0" "$file"; then
                    echo "Replacing old header in $file"
                    # Remove the first block comment
                    sed -i '1,/\*\//d' "$file"
                fi
                echo -e "$HEADER\n$(cat "$file")" > "$file"
                echo "Added header to $file"
            else
                echo "Skipping $file, header already exists."
            fi
        done
    fi
done
