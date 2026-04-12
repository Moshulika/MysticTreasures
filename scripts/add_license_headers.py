import os

HEADER = """/*
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

"""

OLD_HEADER_START = "/*\n * This software is licensed under the PolyForm Noncommercial License 1.0.0."
OLD_HEADER_END = "*/"

def update_header(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    if "Licensed under the Apache License, Version 2.0" in content:
        print(f"Skipping {file_path}, new header already exists.")
        return

    # Check if old header exists and remove it
    if "PolyForm Noncommercial License 1.0.0" in content:
        print(f"Replacing old header in {file_path}")
        # Simple removal of the first block comment if it matches
        if content.startswith("/*"):
            end_index = content.find("*/")
            if end_index != -1:
                content = content[end_index + 2:].lstrip()

    with open(file_path, 'w', encoding='utf-8', newline='') as f:
        f.write(HEADER + content)
    print(f"Added new header to {file_path}")

def main():
    src_dir = 'src'
    test_dir = 'test'
    for directory in [src_dir, test_dir]:
        if not os.path.exists(directory):
            continue
        for root, dirs, files in os.walk(directory):
            for file in files:
                if file.endswith('.java'):
                    update_header(os.path.join(root, file))

if __name__ == "__main__":
    main()
