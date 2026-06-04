import json
import os
import datetime

project_root = r"E:\HeThongDauGia\HeThongDauGia"
base_dir = os.path.join(project_root, ".understand-anything", "intermediate")

# Load components
with open(os.path.join(base_dir, "assembled-graph.json"), "r", encoding="utf-8") as f:
    graph = json.load(f)
    
with open(os.path.join(base_dir, "layers.json"), "r", encoding="utf-8") as f:
    layers_data = json.load(f)
    if isinstance(layers_data, dict) and "layers" in layers_data:
        layers = layers_data["layers"]
    else:
        layers = layers_data

with open(os.path.join(base_dir, "tour.json"), "r", encoding="utf-8") as f:
    tour_data = json.load(f)
    if isinstance(tour_data, dict) and "steps" in tour_data:
        tour = tour_data["steps"]
    elif isinstance(tour_data, dict) and "tour" in tour_data:
        tour = tour_data["tour"]
    else:
        tour = tour_data

with open(os.path.join(base_dir, "scan-result.json"), "r", encoding="utf-8") as f:
    scan_result = json.load(f)

# Collect all valid node IDs
valid_node_ids = set()
for n in graph.get("nodes", []):
    valid_node_ids.add(n.get("id"))

# Normalize Tour
normalized_tour = []
for i, step in enumerate(tour):
    if "nodesToInspect" in step and "nodeIds" not in step:
        step["nodeIds"] = step["nodesToInspect"]
        del step["nodesToInspect"]
    if "whyItMatters" in step and "description" not in step:
        step["description"] = step["whyItMatters"]
        del step["whyItMatters"]
    
    clean_node_ids = []
    for nid in step.get("nodeIds", []):
        if ":" not in nid:
            nid = "file:" + nid.replace("\\", "/").lstrip("/")
        if nid in valid_node_ids:
            clean_node_ids.append(nid)
    
    step["nodeIds"] = clean_node_ids
    if "order" not in step:
        step["order"] = i + 1
    normalized_tour.append(step)

normalized_tour.sort(key=lambda x: x.get("order", 0))

# Ensure layers have nodeIds correctly formatted
for layer in layers:
    # the layer initially had `files`, we mapped `files` in normalize_layers.py but did not make them `file:` prefixed!
    # Wait! `layers` should have `nodeIds` instead of `files`.
    if "files" in layer:
        layer["nodeIds"] = []
        for fpath in layer["files"]:
            nid = "file:" + fpath.replace("\\", "/").lstrip("/")
            if nid in valid_node_ids:
                layer["nodeIds"].append(nid)
        del layer["files"]
    else:
        # Just validate existing nodeIds
        clean_node_ids = []
        for nid in layer.get("nodeIds", []):
            if ":" not in nid:
                nid = "file:" + nid.replace("\\", "/").lstrip("/")
            if nid in valid_node_ids:
                clean_node_ids.append(nid)
        layer["nodeIds"] = clean_node_ids

# Assemble final object
final_graph = {
    "version": "1.0.0",
    "project": {
        "name": scan_result.get("name", "HeThongDauGia"),
        "languages": scan_result.get("languages", []),
        "frameworks": scan_result.get("frameworks", []),
        "description": scan_result.get("description", ""),
        "analyzedAt": datetime.datetime.utcnow().isoformat() + "Z",
        "gitCommitHash": "unknown"
    },
    "nodes": graph.get("nodes", []),
    "edges": graph.get("edges", []),
    "layers": layers,
    "tour": normalized_tour
}

with open(os.path.join(base_dir, "assembled-graph.json"), "w", encoding="utf-8") as f:
    json.dump(final_graph, f, indent=2, ensure_ascii=False)

print("Assembly complete.")
