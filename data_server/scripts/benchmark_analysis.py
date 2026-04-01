import pymysql
import re
from collections import Counter
import pandas as pd

# ========== 数据库配置 ==========
DB_HOST = "rm-7xvba9tp98k38d62q.mysql.rds.aliyuncs.com"
DB_PORT = 3306
DB_USER = "root"
DB_PASS = "Sunny520"
DB_NAME = "zty"
DB_CHARSET = "utf8mb4"

def parse_benchmark_expr(expr: str):
    def normalize_mul(expr: str) -> str:
        # 1. 基础清理
        new_expr = re.sub(r'[\x00-\x1f\x7f]', '', expr)
        new_expr = new_expr.replace('\u00a0', ' ').replace('\u200b', '')
        new_expr = re.sub(r'\([^()+-]*\)', '', new_expr)
        new_expr = new_expr.replace('经估值汇率调整的', '').replace('经估值汇率调整后的', '')
        new_expr = new_expr.replace('经汇率调整的', '').replace('经汇率调整后的', '')
        new_expr = new_expr.replace('经人民币汇率调整的', '')
        new_expr = new_expr.replace('人民币计价的', '')
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

    # 1. 预处理：标准化字符
    expr = expr.replace(" ", "").replace("＋", "+").replace("×", "*")
    expr = normalize_mul(expr)
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
        return [{"benchmark": final_benchmark, "weight": round(current_total_weight, 6)}]

    return _parse(expr)

# ========== 从数据库读取 benchmark ==========
def load_benchmarks_from_db():
    conn = pymysql.connect(
        host=DB_HOST,
        port=DB_PORT,
        user=DB_USER,
        password=DB_PASS,
        database=DB_NAME,
        charset=DB_CHARSET
    )

    sql = """
    SELECT benchmark
    FROM funds
    WHERE is_valid_fund = TRUE
      AND fund_type NOT IN (
        '货币型-普通货币', '货币型-浮动净值', '货币型',
        '混合型-绝对收益', 'Reits', '其他', '商品',
        '债券型-中短债', '债券型-长债',
        '债券型-混合一级', '债券型-混合二级',
        '指数型-海外股票', '指数型-固收',
        'FOF-稳健型', 'FOF-进取型', 'FOF-均衡型',
        'QDII-REITs','QDII-FOF','QDII-商品','QDII-纯债','QDII-混合债'
      )
    """

    with conn.cursor() as cursor:
        cursor.execute(sql)
        rows = cursor.fetchall()

    conn.close()
    return [r[0] for r in rows if r[0]]


# ========== 主流程 ==========
def main():
    benchmarks = load_benchmarks_from_db()
    # benchmarks = ["95%的沪深300指数", "5%的银行同业存款利率", "20%中国", "30%*fds", "20%xfdas", "fad30%", "fdax30%", "fdasX30%", "30%Xxfdas", "30%(fasd)", "30%x(fdas)", "30%X(fdas)", "(fdas)30%", "(fdsasx)x30%", "(fdsa)X30%"]

    parsed_count = 0
    failed = []
    index_counter = Counter()
    weight_records = []

    for b in benchmarks:
        items = parse_benchmark_expr(b)

        if not items:
            failed.append(b)
        else:
            parsed_count += 1
            for it in items:
                index_counter[it["benchmark"]] += 1
                weight_records.append({
                    "benchmark": it["benchmark"],
                    "weight": it["weight"]
                })

    # ==== 写 txt 汇总 ====
    txt_file = "benchmark_analysis.txt"
    with open(txt_file, "w", encoding="utf-8") as f:
        f.write("====== 解析结果汇总 ======\n")
        f.write(f"总 benchmark 数量: {len(benchmarks)}\n")
        f.write(f"成功解析数量: {parsed_count}\n")
        f.write(f"完全解析失败数量: {len(failed)}\n\n")

        f.write("====== 解析出的指数及出现次数 ======\n")
        for k, v in index_counter.most_common():
            f.write(f"{k}: {v}\n")

        f.write("\n====== 完全解析失败的 benchmark（前 30 条）======\n")
        for b in failed[:30]:
            f.write(f"{b}\n")
    # ==== 写 CSV 方便分析 ====
    # print(weight_records)
    csv_file = "benchmark_weights.csv"
    df = pd.DataFrame(weight_records)
    # 汇总每个指数出现次数和权重总和
    summary = df.groupby("benchmark").agg(
        count=("weight", "count"),
        total_weight=("weight", "sum")
    ).reset_index()
    summary = summary.sort_values(by="count", ascending=False)
    summary.to_csv(csv_file, index=False, encoding="utf-8-sig")

    print(f"解析完成，txt 已写入 {txt_file}, csv 已写入 {csv_file}")

if __name__ == "__main__":
    main()
