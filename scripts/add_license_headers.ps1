$header = @"
/*
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

"@

$directories = @("src", "test")
foreach ($dir in $directories) {
    if (Test-Path $dir) {
        Get-ChildItem -Path $dir -Filter "*.java" -Recurse | ForEach-Object {
            $content = [System.IO.File]::ReadAllText($_.FullName, [System.Text.Encoding]::UTF8)
            if ($content -notlike "*Licensed under the Apache License, Version 2.0*") {
                if ($content -like "*PolyForm Noncommercial License 1.0.0*") {
                    Write-Host "Replacing old header in $($_.FullName)"
                    if ($content.StartsWith("/*")) {
                        $endIndex = $content.IndexOf("*/")
                        if ($endIndex -ne -1) {
                            $content = $content.Substring($endIndex + 2).TrimStart()
                        }
                    }
                }
                $newContent = $header + "`r`n" + $content
                [System.IO.File]::WriteAllText($_.FullName, $newContent, (New-Object System.Text.UTF8Encoding($false)))
                Write-Host "Added header to $($_.FullName)"
            } else {
                Write-Host "Skipping $($_.FullName), header already exists."
            }
        }
    }
}
