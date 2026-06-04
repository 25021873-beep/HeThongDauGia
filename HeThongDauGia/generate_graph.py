import json
import os
import math

def get_summary(filename):
    if "DAO.java" in filename:
        return "Lớp DAO (Data Access Object) quản lý thao tác với cơ sở dữ liệu."
    elif "Config.java" in filename:
        return "Lớp thực thể đại diện cho cấu hình dữ liệu."
    elif "Exception.java" in filename:
        return f"Lớp ngoại lệ (Exception) xử lý lỗi nghiệp vụ."
    elif "Service.java" in filename:
        return "Lớp dịch vụ (Service) trung tâm chứa logic nghiệp vụ."
    elif "Test.java" in filename:
        return "Lớp kiểm thử (Test) đảm bảo tính đúng đắn của hệ thống."
    else:
        return "Tệp mã nguồn Java."

def get_tags(filename):
    if "DAO.java" in filename:
        return ["truy-cap-du-lieu", "dao", "co-so-du-lieu"]
    elif "Config.java" in filename:
        return ["thuc-the", "mo-hinh-du-lieu", "cau-hinh"]
    elif "Exception.java" in filename:
        return ["xu-ly-ngoai-le", "loi", "nghiep-vu"]
    elif "Service.java" in filename:
        return ["dich-vu", "nghiep-vu", "xu-ly-logic", "core"]
    elif "Test.java" in filename:
        return ["kiem-thu", "unit-test", "kiem-tra"]
    else:
        return ["ma-nguon", "thanh-phan"]

def get_complexity(lines):
    if lines < 50: return "simple"
    elif lines <= 200: return "moderate"
    else: return "complex"

def main():
    with open('.understand-anything/tmp/ua-file-extract-results-1.json', 'r', encoding='utf-8') as f:
        extract_data = json.load(f)
        
    with open('.understand-anything/tmp/ua-file-analyzer-input-1.json', 'r', encoding='utf-8') as f:
        input_data = json.load(f)
        
    import_map = input_data.get('batchImportData', {})
    
    nodes = []
    edges = []
    batch_file_paths = set([r['path'] for r in extract_data.get('results', [])])
    
    for result in extract_data.get('results', []):
        path = result['path']
        lines = result.get('nonEmptyLines', 0)
        
        file_node_id = f"file:{path}"
        
        nodes.append({
            "id": file_node_id,
            "type": "file",
            "name": os.path.basename(path),
            "filePath": path,
            "summary": get_summary(path),
            "tags": get_tags(path),
            "complexity": get_complexity(lines),
            "languageNotes": "Mã nguồn Java." if "Exception" not in path else "Lớp ngoại lệ Java."
        })
        
        exports = [e['name'] for e in result.get('exports', [])]
        
        for func in result.get('functions', []):
            f_lines = func['endLine'] - func['startLine'] + 1
            is_exported = func['name'] in exports
            if f_lines >= 10 or is_exported:
                func_id = f"function:{path}:{func['name']}"
                nodes.append({
                    "id": func_id,
                    "type": "function",
                    "name": func['name'],
                    "summary": f"Phương thức {func['name']} xử lý logic.",
                    "tags": ["phuong-thuc", "logic"],
                    "complexity": get_complexity(f_lines),
                    "lineRange": [func['startLine'], func['endLine']]
                })
                edges.append({
                    "source": file_node_id,
                    "target": func_id,
                    "type": "contains",
                    "direction": "forward",
                    "weight": 1.0
                })
                if is_exported:
                    edges.append({
                        "source": file_node_id,
                        "target": func_id,
                        "type": "exports",
                        "direction": "forward",
                        "weight": 0.8
                    })
                    
        for cls in result.get('classes', []):
            c_lines = cls['endLine'] - cls['startLine'] + 1
            c_methods = len(cls.get('methods', []))
            is_exported = cls['name'] in exports
            if c_lines >= 20 or c_methods >= 2 or is_exported:
                cls_id = f"class:{path}:{cls['name']}"
                nodes.append({
                    "id": cls_id,
                    "type": "class",
                    "name": cls['name'],
                    "summary": f"Lớp {cls['name']}.",
                    "tags": ["lop", "huong-doi-tuong"],
                    "complexity": get_complexity(c_lines),
                    "lineRange": [cls['startLine'], cls['endLine']]
                })
                edges.append({
                    "source": file_node_id,
                    "target": cls_id,
                    "type": "contains",
                    "direction": "forward",
                    "weight": 1.0
                })
                if is_exported:
                    edges.append({
                        "source": file_node_id,
                        "target": cls_id,
                        "type": "exports",
                        "direction": "forward",
                        "weight": 0.8
                    })
                    
        imports = import_map.get(path, [])
        for imp in imports:
            edges.append({
                "source": file_node_id,
                "target": f"file:{imp}",
                "type": "imports",
                "direction": "forward",
                "weight": 0.7
            })
            if "Test.java" in path and "Test.java" not in imp:
                edges.append({
                    "source": f"file:{imp}",
                    "target": file_node_id,
                    "type": "tested_by",
                    "direction": "forward",
                    "weight": 0.5
                })

    node_count = len(nodes)
    edge_count = len(edges)
    
    parts = int(math.ceil(max(node_count / 60.0, edge_count / 120.0)))
    
    # Check max chunk limits manually to adjust `parts` if necessary.
    # The heuristic formula sometimes underestimates if edges are clustered.
    parts = max(parts, 3) 
        
    os.makedirs('.understand-anything/intermediate', exist_ok=True)
    
    file_paths = sorted(list(batch_file_paths))
    chunk_size = int(math.ceil(len(file_paths) / parts))
    
    # Erase old parts to prevent duplicate leftovers
    for f in os.listdir('.understand-anything/intermediate'):
        if f.startswith('batch-1-part-'):
            os.remove(os.path.join('.understand-anything/intermediate', f))
    
    for k in range(parts):
        chunk_files = set(file_paths[k * chunk_size : (k + 1) * chunk_size])
        if not chunk_files:
            continue
            
        part_nodes = [n for n in nodes if n.get('filePath') in chunk_files or n['id'].split(':')[1] in chunk_files]
        part_node_ids = set(n['id'] for n in part_nodes)
        
        part_edges = []
        for e in edges:
            src_path = e['source'].split(':')[1]
            tgt_path = e['target'].split(':')[1]
            
            if src_path in chunk_files:
                part_edges.append(e)
            elif src_path not in batch_file_paths and tgt_path in chunk_files:
                part_edges.append(e)
        
        with open(f'.understand-anything/intermediate/batch-1-part-{k+1}.json', 'w', encoding='utf-8') as f:
            json.dump({"nodes": part_nodes, "edges": part_edges}, f, indent=2, ensure_ascii=False)
        print(f"Wrote part {k+1}: batch-1-part-{k+1}.json with {len(part_nodes)} nodes and {len(part_edges)} edges.")

if __name__ == '__main__':
    main()
