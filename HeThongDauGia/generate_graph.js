const fs = require('fs');
const path = require('path');

const extractResultsPath = 'E:/HeThongDauGia/HeThongDauGia/.understand-anything/tmp/ua-file-extract-results-2.json';
const inputDataPath = 'E:/HeThongDauGia/HeThongDauGia/.understand-anything/tmp/ua-file-analyzer-input-2.json';
const outputDir = 'E:/HeThongDauGia/HeThongDauGia/.understand-anything/intermediate';

const extractData = JSON.parse(fs.readFileSync(extractResultsPath, 'utf8'));
const inputData = JSON.parse(fs.readFileSync(inputDataPath, 'utf8'));

const batchImportData = inputData.batchImportData || {};

const nodes = [];
const edges = [];

function getVietnameseSummary(name, category, typeStr) {
    name = name.toLowerCase();
    if (name.includes('dao') || name.includes('repository')) {
        return `Lớp xử lý truy xuất cơ sở dữ liệu (Data Access Object) cho ${typeStr}. Cung cấp các phương thức tương tác với DB.`;
    }
    if (name.includes('dto') || name.includes('request') || name.includes('response')) {
        return `Đối tượng truyền tải dữ liệu (DTO) đóng gói thông tin cho ${typeStr}. Được sử dụng để giao tiếp giữa các tầng.`;
    }
    if (name.includes('controller') || name.includes('router') || name.includes('handler')) {
        return `Thành phần điều hướng và xử lý yêu cầu (Controller/Router) cho ${typeStr}. Tiếp nhận và phản hồi các request từ client.`;
    }
    if (name.includes('service') || name.includes('engine')) {
        return `Lớp dịch vụ (Service) chứa logic nghiệp vụ cốt lõi cho ${typeStr}.`;
    }
    if (name.includes('entity') || name.includes('model')) {
        return `Thực thể dữ liệu (Entity) đại diện cho ${typeStr} trong hệ thống. Ánh xạ trực tiếp với cấu trúc cơ sở dữ liệu.`;
    }
    if (name.includes('factory')) {
        return `Mẫu thiết kế Factory chịu trách nhiệm khởi tạo các đối tượng ${typeStr} dựa trên các điều kiện đầu vào.`;
    }
    if (name.includes('test')) {
        return `Lớp kiểm thử (Test) chứa các kịch bản kiểm tra tính đúng đắn cho ${typeStr}.`;
    }
    if (name.includes('util') || name.includes('context')) {
        return `Thành phần tiện ích hỗ trợ, cung cấp các hàm hoặc trạng thái dùng chung cho ${typeStr}.`;
    }
    return `Tệp mã nguồn cung cấp chức năng liên quan đến ${typeStr} trong hệ thống.`;
}

function getTags(name) {
    const tags = [];
    name = name.toLowerCase();
    if (name.includes('test')) tags.push('test');
    if (name.includes('dao')) tags.push('database');
    if (name.includes('dto') || name.includes('request') || name.includes('response')) tags.push('data-model', 'serialization');
    if (name.includes('controller') || name.includes('router') || name.includes('handler')) tags.push('api-handler', 'entry-point');
    if (name.includes('service') || name.includes('engine')) tags.push('service');
    if (name.includes('entity') || name.includes('model')) tags.push('data-model');
    if (name.includes('factory')) tags.push('factory');
    if (name.includes('util')) tags.push('utility');
    
    if (tags.length === 0) tags.push('component', 'utility');
    
    // Ensure we have at least 3 tags
    if (tags.length < 3) {
        if (!tags.includes('component')) tags.push('component');
        if (!tags.includes('backend') && tags.length < 3) tags.push('backend');
        if (!tags.includes('java') && tags.length < 3) tags.push('java');
    }
    return tags.slice(0, 5);
}

function getComplexity(nonEmptyLines) {
    if (nonEmptyLines < 50) return 'simple';
    if (nonEmptyLines < 200) return 'moderate';
    return 'complex';
}

const fileNodes = new Set();
const allFuncNodes = new Map(); // name -> id

// First pass: create file nodes
for (const file of extractData.results) {
    const p = file.path;
    const baseName = path.basename(p);
    const summary = getVietnameseSummary(baseName, file.fileCategory, baseName);
    
    nodes.push({
        id: `file:${p}`,
        type: 'file',
        name: baseName,
        filePath: p,
        summary: summary,
        tags: getTags(baseName),
        complexity: getComplexity(file.nonEmptyLines || file.totalLines),
        languageNotes: "Mã nguồn Java với thiết kế hướng đối tượng."
    });
    fileNodes.add(`file:${p}`);

    // Create class nodes
    if (file.classes) {
        for (const cls of file.classes) {
            const lineCount = (cls.endLine - cls.startLine) + 1;
            const methodCount = cls.methods ? cls.methods.length : 0;
            const isExported = file.exports && file.exports.some(e => e.name === cls.name);
            
            if (lineCount >= 20 || methodCount >= 2 || isExported) {
                const clsId = `class:${p}:${cls.name}`;
                nodes.push({
                    id: clsId,
                    type: 'class',
                    name: cls.name,
                    filePath: p,
                    summary: `Lớp ${cls.name} định nghĩa cấu trúc và hành vi cho ${cls.name} trong hệ thống.`,
                    tags: getTags(cls.name),
                    complexity: getComplexity(lineCount),
                    lineRange: [cls.startLine, cls.endLine]
                });
                edges.push({
                    source: `file:${p}`,
                    target: clsId,
                    type: 'contains',
                    direction: 'forward',
                    weight: 1.0
                });
                if (isExported) {
                    edges.push({
                        source: `file:${p}`,
                        target: clsId,
                        type: 'exports',
                        direction: 'forward',
                        weight: 0.8
                    });
                }
            }
        }
    }

    // Create function nodes
    if (file.functions) {
        for (const func of file.functions) {
            const lineCount = (func.endLine - func.startLine) + 1;
            const isExported = file.exports && file.exports.some(e => e.name === func.name);
            
            if (lineCount >= 10 || isExported) {
                const funcId = `function:${p}:${func.name}`;
                allFuncNodes.set(func.name, funcId);
                nodes.push({
                    id: funcId,
                    type: 'function',
                    name: func.name,
                    filePath: p,
                    summary: `Hàm ${func.name} thực thi logic nghiệp vụ cụ thể cho ${baseName}.`,
                    tags: getTags(func.name),
                    complexity: getComplexity(lineCount),
                    lineRange: [func.startLine, func.endLine]
                });
                edges.push({
                    source: `file:${p}`,
                    target: funcId,
                    type: 'contains',
                    direction: 'forward',
                    weight: 1.0
                });
                if (isExported) {
                    edges.push({
                        source: `file:${p}`,
                        target: funcId,
                        type: 'exports',
                        direction: 'forward',
                        weight: 0.8
                    });
                }
            }
        }
    }

    // Imports edges
    const imports = batchImportData[p] || [];
    for (const imp of imports) {
        edges.push({
            source: `file:${p}`,
            target: `file:${imp}`,
            type: 'imports',
            direction: 'forward',
            weight: 0.7
        });
    }
    
    // tested_by edge heuristic
    if (p.includes('Test.java') || p.includes('/test/')) {
        let prodPath = p.replace('src/test/', 'src/main/').replace('Test.java', '.java');
        edges.push({
            source: `file:${p}`,
            target: `file:${prodPath}`,
            type: 'tested_by',
            direction: 'forward',
            weight: 0.5
        });
    }
}

// Split logic
const maxNodes = 60;
const maxEdges = 120;
const parts = Math.ceil(Math.max(nodes.length / maxNodes, edges.length / maxEdges));

if (parts <= 1) {
    const outPath = path.join(outputDir, `batch-2.json`);
    fs.writeFileSync(outPath, JSON.stringify({ nodes, edges }, null, 2));
    console.log(`Wrote 1 part to ${outPath}. Total nodes: ${nodes.length}, Total edges: ${edges.length}`);
} else {
    // Sort files alphabetically
    const files = extractData.results.map(f => f.path).sort();
    
    const chunkSize = Math.ceil(files.length / parts);
    for (let i = 0; i < parts; i++) {
        const chunkFiles = new Set(files.slice(i * chunkSize, (i + 1) * chunkSize));
        
        const partNodes = nodes.filter(n => chunkFiles.has(n.filePath));
        const partNodeIds = new Set(partNodes.map(n => n.id));
        
        const partEdges = edges.filter(e => {
            // Include edge if its source is in this part's nodes
            return partNodeIds.has(e.source) || chunkFiles.has(e.source.replace('file:', ''));
        const refinedEdges = edges.filter(e => {
            const parts = e.source.split(':');
            const sourcePath = parts.length >= 2 ? parts[1] : '';
            return chunkFiles.has(sourcePath);
        });

        const outPath = path.join(outputDir, `batch-2-part-${i + 1}.json`);
        fs.writeFileSync(outPath, JSON.stringify({ nodes: partNodes, edges: refinedEdges }, null, 2));
        console.log(`Wrote part ${i + 1} to ${outPath}. Nodes: ${partNodes.length}, Edges: ${refinedEdges.length}`);
    }
    console.log(`Split into ${parts} parts. Total nodes: ${nodes.length}, Total edges: ${edges.length}`);
}
