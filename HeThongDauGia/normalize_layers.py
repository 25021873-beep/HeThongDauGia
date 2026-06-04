import json
import os

project_root = r"E:\HeThongDauGia\HeThongDauGia"
layers_path = os.path.join(project_root, ".understand-anything", "intermediate", "layers.json")

with open(layers_path, "r", encoding="utf-8") as f:
    data = json.load(f)

# 1. Unwrap envelope
if isinstance(data, dict) and "layers" in data:
    layers = data["layers"]
else:
    layers = data

# Normalization loop
normalized = []
for i, layer in enumerate(layers):
    # 2. Rename legacy fields
    if "title" in layer and "name" not in layer:
        layer["name"] = layer["title"]
        del layer["title"]
        
    # 3. Synthesize missing IDs
    if "id" not in layer or not layer["id"]:
        layer["id"] = f"layer-{i+1}"
        
    # 4. Convert file paths & 5. Drop dangling refs
    valid_files = []
    for fp in layer.get("files", []):
        # make relative and trim leading /
        clean_path = fp.replace("\\", "/").lstrip("/")
        
        # Check if file exists in project_root
        abs_path = os.path.join(project_root, clean_path)
        if os.path.exists(abs_path) or os.path.exists(os.path.join(project_root, "src", "main", "java", clean_path)): # the graph might have `src/main/java...`
            # Wait, `clean_path` should already be relative to project root as per the graph.
            # I will just check if `os.path.exists` from project_root
            if os.path.exists(os.path.join(project_root, clean_path)):
                valid_files.append(clean_path)
    
    layer["files"] = valid_files
    normalized.append(layer)

# write back as a clean list or dict? The prompt says "into a final `layers` array" but also "if layers are inside envelope". The final should just be the object with `layers` array, or just the array. We will write `{"layers": normalized}` for consistency.
with open(layers_path, "w", encoding="utf-8") as f:
    json.dump({"layers": normalized}, f, indent=2, ensure_ascii=False)
