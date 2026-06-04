import json
import os

project_root = r"E:\HeThongDauGia\HeThongDauGia"

with open(os.path.join(project_root, ".understand-anything", "intermediate", "scan-result.json"), "r", encoding="utf-8") as f:
    scan_result = json.load(f)

source_files = [f["path"] for f in scan_result.get("files", [])]

input_data = {
    "projectRoot": project_root.replace("\\", "/"),
    "sourceFilePaths": source_files,
    "gitCommitHash": "unknown"
}

input_path = os.path.join(project_root, ".understand-anything", "tmp", "fingerprints-input.json")
with open(input_path, "w", encoding="utf-8") as f:
    json.dump(input_data, f)
