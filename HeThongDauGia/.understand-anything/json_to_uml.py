import json
import re

with open('knowledge-graph.json', 'r', encoding='utf-8') as f:
    data = json.load(f)

# Hàm trích xuất tên thư mục (package/module) từ ID của node
def extract_module(node_id):
    # Tìm đoạn đường dẫn sau src/main/java/ 
    match = re.search(r'src/main/java/([^:]+)/[^/:]+\.\w+', node_id)
    if match:
        # Đổi xuyệt (/) thành chấm (.) cho đúng chuẩn package
        return match.group(1).replace('/', '.') 
    return None

node_to_module = {}

# Quét tất cả các node để phân loại xem nó thuộc module nào
for node in data.get('nodes', []):
    node_id = node.get('id', '')
    module = extract_module(node_id)
    if module:
        node_to_module[node_id] = module

edges = set()

# Quét các cạnh để tìm luồng giao tiếp giữa các module
for edge in data.get('edges', []):
    src_module = node_to_module.get(edge.get('source'))
    tgt_module = node_to_module.get(edge.get('target'))
    
    # Chỉ lưu các liên kết chéo giữa 2 module khác nhau
    if src_module and tgt_module and src_module != tgt_module:
        edges.add((src_module, tgt_module))

# Sinh cú pháp Flowchart cho Mermaid
print("flowchart TD")
print("    %% Các thành phần hệ thống")

modules = list(set(node_to_module.values()))
for i, mod in enumerate(modules):
    print(f'    M{i}["{mod}"]')

print("\n    %% Chiều tương tác giữa các thành phần")
for src, tgt in edges:
    print(f"    M{modules.index(src)} --> M{modules.index(tgt)}")