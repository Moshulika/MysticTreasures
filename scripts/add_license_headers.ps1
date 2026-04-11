$header = @"
/*
 * This software is licensed under the PolyForm Noncommercial License 1.0.0.
 * You may obtain a copy of the License at:
 * https://polyformproject.org/licenses/noncommercial/1.0.0
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 */

"@

Get-ChildItem -Path "src" -Filter "*.java" -Recurse | ForEach-Object {
    $content = [System.IO.File]::ReadAllText($_.FullName, [System.Text.Encoding]::UTF8)
    if ($content -notlike "*PolyForm Noncommercial License 1.0.0*") {
        $newContent = $header + "`r`n" + $content
        [System.IO.File]::WriteAllText($_.FullName, $newContent, (New-Object System.Text.UTF8Encoding($false)))
        Write-Host "Added header to $($_.FullName)"
    } else {
        Write-Host "Skipping $($_.FullName), header already exists."
    }
}
