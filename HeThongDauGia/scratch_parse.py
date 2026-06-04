import json

with open('.understand-anything/tmp/ua-file-extract-results-1.json', 'r', encoding='utf-8') as f:
    data = json.load(f)

for result in data.get('results', []):
    path = result.get('path')
    lines = result.get('nonEmptyLines')
    funcs = len(result.get('functions', []))
    classes = len(result.get('classes', []))
    print(f"{path}: {lines} lines, {funcs} funcs, {classes} classes")
