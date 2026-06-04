import json
import os
import math

input_data_path = "E:/HeThongDauGia/HeThongDauGia/.understand-anything/tmp/ua-file-analyzer-input-10.json"
results_data_path = "E:/HeThongDauGia/HeThongDauGia/.understand-anything/tmp/ua-file-extract-results-10.json"
output_path = "E:/HeThongDauGia/HeThongDauGia/.understand-anything/intermediate/batch-10.json"

with open(input_data_path, 'r', encoding='utf-8') as f:
    input_data = json.load(f)

with open(results_data_path, 'r', encoding='utf-8') as f:
    results_data = json.load(f)

batch_import_data = input_data.get("batchImportData", {})

nodes = []
edges = []

for file_result in results_data.get("results", []):
    path = file_result.get("path")
    cat = file_result.get("fileCategory")
    lines = file_result.get("nonEmptyLines", 0)
    
    complexity = "simple"
    if lines > 200:
        complexity = "complex"
    elif lines >= 50:
        complexity = "moderate"
        
    node_type = "file"
    if cat == "config":
        node_type = "config"
    elif cat == "docs":
        node_type = "document"
    elif cat == "infra":
        if "Dockerfile" in path or "docker-compose" in path or "manifests" in path:
            node_type = "service"
        elif ".github" in path or ".gitlab" in path or "Jenkins" in path:
            node_type = "pipeline"
        elif ".tf" in path or "Vagrant" in path or "CloudFormation" in path:
            node_type = "resource"
        else:
            node_type = "service"
    elif cat == "data":
        if ".sql" in path:
            node_type = "table"
        elif ".graphql" in path or ".proto" in path or ".prisma" in path:
            node_type = "schema"
        elif "openapi" in path.lower() or "swagger" in path.lower():
            node_type = "endpoint"
        else:
            node_type = "table"
            
    tags = []
    summary = "Tập tin trong dự án."
    
    basename = os.path.basename(path)
    if "test" in path.lower() or "spec" in path.lower():
        tags = ["test"]
        summary = f"Tập tin kiểm thử (test) cho {basename.replace('Test.java', '')}."
    elif ".fxml" in path:
        tags = ["ui-view", "markup"]
        summary = f"Tập tin giao diện người dùng FXML định nghĩa bố cục cho {basename.replace('.fxml', '')}."
    elif ".properties" in path:
        tags = ["configuration"]
        summary = "Tập tin cấu hình các thuộc tính của ứng dụng."
    elif ".css" in path:
        tags = ["markup", "styling"]
        summary = "Tập tin CSS định dạng giao diện cho ứng dụng."
    elif "MANIFEST.MF" in path:
        tags = ["configuration"]
        summary = "Tập tin MANIFEST chứa metadata cho việc đóng gói JAR."
    else:
        tags = ["file"]
        
    file_id = f"{node_type}:{path}"
    
    nodes.append({
        "id": file_id,
        "type": node_type,
        "name": basename,
        "filePath": path,
        "summary": summary,
        "tags": tags,
        "complexity": complexity
    })
    
    if path in batch_import_data:
        for imp_path in batch_import_data[path]:
            edges.append({
                "source": file_id,
                "target": f"file:{imp_path}",
                "type": "imports",
                "direction": "forward",
                "weight": 0.7
            })
            
    if cat == "code":
        for func in file_result.get("functions", []):
            f_lines = func.get("endLine", 0) - func.get("startLine", 0) + 1
            if f_lines >= 10:
                f_id = f"function:{path}:{func.get('name')}"
                nodes.append({
                    "id": f_id,
                    "type": "function",
                    "name": func.get("name"),
                    "summary": f"Phương thức kiểm thử {func.get('name')}.",
                    "tags": ["test", "function"],
                    "complexity": "simple" if f_lines < 20 else "moderate",
                    "lineRange": [func.get("startLine"), func.get("endLine")]
                })
                edges.append({
                    "source": file_id,
                    "target": f_id,
                    "type": "contains",
                    "direction": "forward",
                    "weight": 1.0
                })
        
        for cls in file_result.get("classes", []):
            c_lines = cls.get("endLine", 0) - cls.get("startLine", 0) + 1
            if c_lines >= 20 or len(cls.get("methods", [])) >= 2:
                c_id = f"class:{path}:{cls.get('name')}"
                nodes.append({
                    "id": c_id,
                    "type": "class",
                    "name": cls.get("name"),
                    "summary": f"Lớp kiểm thử {cls.get('name')} chứa các test case.",
                    "tags": ["test", "class"],
                    "complexity": "simple" if c_lines < 50 else "moderate",
                    "lineRange": [cls.get("startLine"), cls.get("endLine")]
                })
                edges.append({
                    "source": file_id,
                    "target": c_id,
                    "type": "contains",
                    "direction": "forward",
                    "weight": 1.0
                })

node_count = len(nodes)
edge_count = len(edges)

os.makedirs(os.path.dirname(output_path), exist_ok=True)

with open(output_path, 'w', encoding='utf-8') as f:
    json.dump({"nodes": nodes, "edges": edges}, f, indent=2, ensure_ascii=False)
print(f"Wrote single part with {node_count} nodes and {edge_count} edges.")
