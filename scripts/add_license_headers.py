import os

HEADER = """/*
 * This software is licensed under the PolyForm Noncommercial License 1.0.0.
 * You may obtain a copy of the License at:
 * https://polyformproject.org/licenses/noncommercial/1.0.0
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 */

"""

def add_header(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    if "PolyForm Noncommercial License 1.0.0" in content:
        print(f"Skipping {file_path}, header already exists.")
        return

    with open(file_path, 'w', encoding='utf-8', newline='') as f:
        f.write(HEADER + content)
    print(f"Added header to {file_path}")

def main():
    src_dir = 'src'
    for root, dirs, files in os.walk(src_dir):
        for file in files:
            if file.endswith('.java'):
                add_header(os.path.join(root, file))

if __name__ == "__main__":
    main()
