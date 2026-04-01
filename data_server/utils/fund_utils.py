import re
import hashlib

class FundUtils:
    """
    基金工具类，用于处理基金相关的操作
    """
    
    @staticmethod
    def parse_benchmark_weights(benchmark_data):
        """
        解析基金的benchmark跟踪权重
        
        Args:
            benchmark_data: 包含benchmark权重数据的输入
            
        Returns:
            解析后的benchmark权重数据
        """
        def normalize_mul(expr: str) -> str:
            # 1. 基础清理
            new_expr = re.sub(r'[\x00-\x1f\x7f]', '', expr)
            new_expr = new_expr.replace('\u00a0', ' ').replace('\u200b', '')
            new_expr = re.sub(r'\([^()+-]*\)', '', new_expr)
            new_expr = new_expr.replace('经估值汇率调整的', '').replace('经估值汇率调整后的', '')
            new_expr = new_expr.replace('经汇率调整的', '').replace('经汇率调整后的', '')
            new_expr = new_expr.replace('经人民币汇率调整的', '')


            # --- 2. 定义核心组件 ---
            # 权重特征：数字 + %
            weight_pat = r'[0-9.]+%+'
            # 实体起点：字母、中文、左括号
            ent_start = r'[a-zA-Z\u4e00-\u9fa5(（]'
            # 实体终点：字母、中文、右括号
            ent_end = r'[a-zA-Z\u4e00-\u9fa5)）]'
            # 连接符集：x, X, 的, 以及空白字符 (这里不要带 + 号)
            conn_set = r'[xX的的\s]'

            # --- 3. 核心替换逻辑 ---
            
            # 情况 A: 权重在前 (例如: "30%MSCI", "30% x MSCI", "30%的MSCI")
            # 匹配：权重 + (0个或多个连接符) + (前瞻是实体起点)
            # 使用 {conn_set}* 来允许 0 到多个连接符
            new_expr = re.sub(
                fr'({weight_pat}){conn_set}*(?={ent_start})', 
                r'\1*', 
                new_expr
            )

            # 情况 B: 权重在后 (例如: "MSCI30%", "MSCI 30%", "中国的30%", "(A+B)30%")
            # 匹配：(后顾是实体终点) + (0个或多个连接符) + 权重
            new_expr = re.sub(
                fr'(?<={ent_end}){conn_set}*({weight_pat})', 
                r'*\1', 
                new_expr
            )

            # --- 4. 善后处理 ---
            # 移除可能产生的重复星号
            while '**' in new_expr:
                new_expr = new_expr.replace('**', '*')
            
            # 移除星号前后的多余空格（可选，视你后续解析需要而定）
            new_expr = re.sub(r'\s*\*\s*', '*', new_expr)
            
            return new_expr.strip()

        def normalize_benchmark(name: str) -> str:
            if not name:
                return name

            s = name.strip()

            # 1️⃣ 去掉所有括号及内容（中英文括号）
            s = re.sub(r'（.*?）|\(.*?\)', '', s)

            # 2️⃣ 去掉“涨跌幅 / 增长率”（可能在中间）
            s = re.sub(r'(涨跌幅|增长率)', '', s)

            # 3️⃣ 去掉“收益率”结尾
            s = re.sub(r'收益率$', '', s)

            # 4️⃣ 仅去掉中括号本身（不动内容）
            s = s.replace('[', '').replace(']', '')

            # 5️⃣ 多余空白清理
            s = re.sub(r'\s+', '', s)

            return s

        def get_top_level_split(s, sep):
            """寻找不在任何括号内的分隔符位置"""
            level = 0
            for i, char in enumerate(s):
                if char == '(':
                    level += 1
                elif char == ')':
                    level -= 1
                elif level == 0 and char == sep:
                    return i
            return -1

        def extract_weight_from_segment(s):
            """
            从片段中提取当前层级的权重。
            支持: "A*30%", "30%*A", "(A+B)*30%", "30%(A+B)", "A30%", "30%A"
            """
            s = s.strip()
            if not s: return s, 1.0
            
            # 匹配百分比的正则
            pct_pattern = r'(\d+(?:\.\d+)?%)'
            
            # 情况 A: 权重在右侧，如 (内容)*30% 或 (内容)30%
            # 利用正则匹配结尾的百分比，且前面不是数字（防止匹配到指数名里的数字）
            m_end = re.search(f'{pct_pattern}$', s)
            if m_end:
                weight_str = m_end.group(1)
                content = s[:m_end.start()].rstrip('*').strip()
                # 关键：确保剩下的 content 括号是平衡的，否则这个权重可能是括号内部的
                if content.count('(') == content.count(')'):
                    return content, float(weight_str.strip('%')) / 100

            # 情况 B: 权重在左侧，如 30%*(内容) 或 30%(内容)
            m_start = re.match(f'^{pct_pattern}', s)
            if m_start:
                weight_str = m_start.group(1)
                content = s[m_start.end():].lstrip('*').strip()
                if content.count('(') == content.count(')'):
                    return content, float(weight_str.strip('%')) / 100

            return s, 1.0
        
        def load_benchmark_mapping():
            import csv
            from pathlib import Path
            
            mapping = {}
            csv_path = Path(__file__).parent.parent / "scripts" / "config" / "benchmark_mapping.csv"
            
            if csv_path.exists():
                with open(csv_path, 'r', encoding='utf-8') as f:
                    reader = csv.reader(f, delimiter=',')
                    for row in reader:
                        if len(row) >= 2:
                            benchmark = row[0].strip()
                            index = row[1].strip()
                            if benchmark and index:
                                mapping[benchmark] = index
            
            return mapping
        
        # 加载基准映射
        benchmark_mapping = load_benchmark_mapping()
        
        def benchmark_to_index(benchmark: str) -> str:
            """
            将基准表达式转换为索引表达式
            """
            # 查找映射
            if benchmark in benchmark_mapping:
                return benchmark_mapping[benchmark]
            
            # 如果没有找到映射，使用哈希生成唯一的索引代码
            # 使用 SHA-256 哈希函数生成哈希值
            hash_obj = hashlib.sha256(benchmark.encode('utf-8'))
            # 取前 20 个字符作为索引代码
            hash_code = hash_obj.hexdigest()[:20]
            
            return hash_code

        def _parse(segment, outer_weight=1.0):
            segment = segment.strip()
            if not segment:
                return []

            # 1. 处理顶层加号（优先级最低）
            plus_idx = get_top_level_split(segment, '+')
            if plus_idx != -1:
                left = segment[:plus_idx]
                right = segment[plus_idx+1:]
                return _parse(left, outer_weight) + _parse(right, outer_weight)

            # 2. 提取当前层的权重（优先级中等）
            content, weight = extract_weight_from_segment(segment)
            current_total_weight = outer_weight * weight

            # 3. 处理括号剥离（优先级最高）
            # 如果内容被最外层括号包裹，如 (上证180*60%+深证100*40%)
            if content.startswith("(") and content.endswith(")"):
                inner = content[1:-1]
                # 检查剥掉的是否是配对的括号
                if inner.count('(') == inner.count(')'):
                    return _parse(inner, current_total_weight)

            # 4. 递归出口：如果是纯数字或空，过滤
            if not content or re.fullmatch(r'[\d\.\%\/年\*]+', content):
                return []

            # 5. 返回结果，清理掉可能残余的字符
            final_benchmark = content.strip('*').strip()
            final_benchmark = normalize_benchmark(final_benchmark)
            index_code = benchmark_to_index(final_benchmark)
            return [{"index_code": index_code, "index_name": final_benchmark, "weight": round(current_total_weight, 6)}]

        # 1. 预处理：标准化字符
        expr = benchmark_data.replace(" ", "").replace("＋", "+").replace("×", "*")
        expr = normalize_mul(expr)
        return _parse(expr)
