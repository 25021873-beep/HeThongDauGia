import os
import json

base_dir = r"C:\Users\DELL\.gemini\config\plugins"
skill_dir = os.path.join(base_dir, "skills", "understand")
agents_dir = os.path.join(base_dir, "agents")

# 1. Base agent definition
with open(os.path.join(agents_dir, "architecture-analyzer.md"), "r", encoding="utf-8") as f:
    prompt = f.read()

prompt += "\n\n## Language Context\n"

# 2. Languages
langs = ["css", "fxml", "java", "json", "mf", "properties", "txt", "unknown", "xml", "yaml"]
for lang in langs:
    lang_file = os.path.join(skill_dir, "languages", f"{lang}.md")
    if os.path.exists(lang_file):
        with open(lang_file, "r", encoding="utf-8") as f:
            prompt += f.read() + "\n\n"

# 3. Frameworks
frameworks = ["javafx", "mysql"]
for fw in frameworks:
    fw_file = os.path.join(skill_dir, "frameworks", f"{fw}.md")
    if os.path.exists(fw_file):
        with open(fw_file, "r", encoding="utf-8") as f:
            prompt += f.read() + "\n\n"

# 4. Locale
locale_file = os.path.join(skill_dir, "locales", "vi.md")
if os.path.exists(locale_file):
    prompt += "\n\n## Output Language Guidelines\n"
    with open(locale_file, "r", encoding="utf-8") as f:
        prompt += f.read() + "\n\n"

# Save the system prompt
with open(r"E:\HeThongDauGia\HeThongDauGia\arch_analyzer_sys_prompt.txt", "w", encoding="utf-8") as f:
    f.write(prompt)

# Build the dispatch prompt
with open(r"E:\HeThongDauGia\HeThongDauGia\.understand-anything\intermediate\assembled-graph.json", "r", encoding="utf-8") as f:
    graph = json.load(f)

nodes = graph.get("nodes", [])
edges = graph.get("edges", [])

file_level_types = {'file', 'config', 'document', 'service', 'pipeline', 'table', 'schema', 'resource', 'endpoint'}
file_nodes = []
for n in nodes:
    if n.get("type") in file_level_types:
        fn = {
            "id": n.get("id"),
            "type": n.get("type"),
            "name": n.get("name"),
            "filePath": n.get("filePath"),
            "summary": n.get("summary"),
            "tags": n.get("tags")
        }
        file_nodes.append(fn)

import_edges = [e for e in edges if e.get("type") == "imports"]

dispatch_prompt = f"""Analyze this codebase's structure to identify architectural layers.
Project root: E:/HeThongDauGia/HeThongDauGia
Write output to: E:/HeThongDauGia/HeThongDauGia/.understand-anything/intermediate/layers.json
Project: HeThongDauGia - Không có mô tả nào được cung cấp cho dự án này.

**Additional context from main session:**
Frameworks detected: JavaFX, MySQL
Directory tree (top 2 levels):
(Look at the nodes filePath to infer directory tree)

Use the directory tree, language context, and framework addendums (appended in your system prompt) to inform layer assignments.
Language directive: Generate all textual content (summaries, descriptions, tags, titles, languageNotes, languageLesson) in vi. Maintain technical accuracy while using natural, native-level phrasing in the target language.

File nodes:
```json
{json.dumps(file_nodes, indent=2)}
```

Import edges:
```json
{json.dumps(import_edges, indent=2)}
```

All edges:
```json
{json.dumps(edges, indent=2)}
```
"""

with open(r"E:\HeThongDauGia\HeThongDauGia\arch_analyzer_dispatch.txt", "w", encoding="utf-8") as f:
    f.write(dispatch_prompt)
