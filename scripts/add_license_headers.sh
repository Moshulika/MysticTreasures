#!/bin/bash

HEADER='/*
 * This software is licensed under the PolyForm Noncommercial License 1.0.0.
 * You may obtain a copy of the License at:
 * https://polyformproject.org/licenses/noncommercial/1.0.0
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 */
'

find src -name "*.java" | while read file; do
    if ! grep -q "PolyForm Noncommercial License 1.0.0" "$file"; then
        echo -e "$HEADER\n$(cat "$file")" > "$file"
        echo "Added header to $file"
    else
        echo "Skipping $file, header already exists."
    fi
done
