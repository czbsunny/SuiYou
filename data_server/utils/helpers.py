import re

def standardize_symbol(symbol: str, asset_type: str = 'stock') -> str:
    if not symbol: return ""
    
    # 统一转为大写处理，并移除可能存在的 .SI .SH .SZ .HK 后缀
    s = str(symbol).strip().upper().split('.')[0]

    # 已经是标准格式的不再处理
    if s.startswith(('SH.', 'SZ.', 'BJ.', 'IDX.', 'FX.', 'HK.', 'US.')):
        return s.lower()

    if asset_type == 'stock':
        # 1. 首先通过“字母检测”判定美股
        # 如果包含任何英文字母，直接判定为美股 (US)
        if re.search('[a-zA-Z]', s):
            return f"us.{s}".lower()

        # 2. 如果是纯数字，根据长度判定市场
        length = len(s)

        # --- A 股市场 (全是 6 位) ---
        if length == 6:
            # 北京 (43, 83, 87, 88, 920)
            if s.startswith(('43', '83', '87', '88', '920')): 
                return f"bj.{s}".lower()
            
            # 上海 (60x, 688)
            if s.startswith(('6')): 
                return f"sh.{s}".lower()
            
            # 深圳 (000, 002, 300, 200)
            if s.startswith(('0', '3', '2')): 
                return f"sz.{s}".lower()
            
            return f"sz.{s}".lower() # 无法确定的 6 位默认归为深圳

        # --- 港股市场 (通常补齐为 5 位) ---
        if length == 5:
            return f"hk.{s}".lower()

        # --- 特殊情况：未补齐的港股 (如 "700") ---
        if length < 5:
            # 自动补齐为 5 位
            return f"hk.{s.zfill(5)}".lower()

    if asset_type == 'index':
        # 指数逻辑保持之前的分类即可，因为指数代码冲突较少
        if s.startswith(('801', '802')): return f"idx.sw{s}".lower()
        if s.startswith('000') and len(s) == 6: return f"idx.sh{s}".lower()
        if s.startswith('399'): return f"idx.sz{s}".lower()
        return f"idx.{s}".lower()

    return s.lower()

def is_cdr(symbol):
    pure_code = str(symbol).split('.')[-1] # 处理 sh.688399 这种带前缀的情况
    return pure_code.startswith('689')


def is_trading_day(date_obj):
    """
    判断是否为交易日
    简单版本：只判断是否为周一到周五（0-4）
    复杂版本可以在此基础上添加节假日判断
    
    Args:
        date_obj: datetime.date 对象
        
    Returns:
        bool: 是否为交易日
    """
    weekday = date_obj.weekday()
    return 0 <= weekday <= 4