import json

with open("E:/HeThongDauGia/HeThongDauGia/.understand-anything/intermediate/scan-result.json", "r", encoding="utf-8") as f:
    scan_result = json.load(f)

import_map = scan_result.get("importMap", {})

merge_report = """Found 20 batch files (11 logical batches, 6 multi-part):
Input: 575 nodes, 968 edges

Fixed (8 corrections):
     5 - duplicate node IDs removed (kept last)
     2 - tested_by edges flipped
     1 - tested_by edges dropped

Tested-by linker:
     2 - tested_by edges produced
    24 - production nodes tagged "tested"

Could not fix (2 issues - needs agent review):
  - Edge config:src/main/resources/application-template.properties -> file:src/main/java/org/example/Main.java (configures): dropped, missing target 'file:src/main/java/org/example/Main.java'
  - Edge config:src/main/resources/application.properties -> file:src/main/java/org/example/Main.java (configures): dropped, missing target 'file:src/main/java/org/example/Main.java'

Output: 570 nodes, 957 edges

Imports edge recovery:
  Recovered 17 `imports` edges from importMap (153 entries scanned)

Written to E:\\HeThongDauGia\\HeThongDauGia\\.understand-anything\\intermediate\\assembled-graph.json (514 KB)"""

prompt = f"""Review the assembled graph at E:/HeThongDauGia/HeThongDauGia/.understand-anything/intermediate/assembled-graph.json.
Project root: E:/HeThongDauGia/HeThongDauGia
Batch files are at: E:/HeThongDauGia/HeThongDauGia/.understand-anything/intermediate/batch-*.json
Write review output to: E:/HeThongDauGia/HeThongDauGia/.understand-anything/intermediate/assemble-review.json

**Merge script report:**
```
{merge_report}
```

**Import map for cross-batch edge verification:**
```json
{json.dumps(import_map, indent=2)}
```
"""

with open("E:/HeThongDauGia/HeThongDauGia/phase3_prompt.txt", "w", encoding="utf-8") as f:
    f.write(prompt)
