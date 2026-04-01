import csv
import sys
import os

def get_config_dir():
    """获取配置目录路径（脚本所在目录的config子目录）"""
    script_dir = os.path.dirname(os.path.abspath(__file__))
    return os.path.join(script_dir, 'config')

class DataLoader:
    """数据加载器类，负责从不同文件中加载数据"""
    def __init__(self):
        """初始化数据加载器"""
        # 获取配置目录路径
        self.config_dir = get_config_dir()
        # 初始化数据字典
        self.stock_data_a = {}  # A股数据
        self.stock_data_kcb = {}  # 科创板数据
        self.stock_data_hk = {}  # 港股数据
        self.stock_data_us = {}  # 美股数据
        self.index_price_change = {}  # 指数代码 -> 涨跌幅
        self.fund_stock_allocation = {}  # 基金代码 -> 股票资产占比
        self.fund_index_mapping = {}  # 基金代码 -> 指数映射列表
        self.index_weights = {}  # 指数代码 -> 成分股权重列表
        self.fund_types = {}  # 基金代码 -> 基金类型
        # 市场优先级列表
        self.market_priority = [self.stock_data_a, self.stock_data_kcb, self.stock_data_hk, self.stock_data_us]
        # 市场名称列表
        self.market_names = ['A股', '科创板', '港股', '美股']
    
    def load_fund_types(self, file_path='fund_nav.csv'):
        """加载基金类型数据"""
        full_path = os.path.join(self.config_dir, file_path)
        with open(full_path, 'r', encoding='utf-8-sig') as f:
            reader = csv.reader(f)
            header = next(reader)
            
            fund_code_index = header.index('fund_code')
            fund_type_index = header.index('fund_type')
            
            for row in reader:
                if len(row) > max(fund_code_index, fund_type_index):
                    fund_code = row[fund_code_index]
                    fund_type = row[fund_type_index]
                    self.fund_types[fund_code] = fund_type
        print(f'基金类型数据共 {len(self.fund_types)} 只基金')
    
    def load_fund_asset_allocation(self, file_path='fund_asset_allocation.csv'):
        """加载基金资产配置数据"""
        full_path = os.path.join(self.config_dir, file_path)
        # 先加载基金类型数据
        self.load_fund_types()
        
        # 初始化一个临时字典来存储所有资产类型的占比
        temp_allocation = {}
        
        with open(full_path, 'r', encoding='utf-8-sig') as f:
            reader = csv.reader(f)
            header = next(reader)
            
            fund_code_index = header.index('fund_code')
            asset_type_index = header.index('asset_type')
            allocation_ratio_index = header.index('allocation_ratio')
            
            for row in reader:
                if len(row) > max(fund_code_index, asset_type_index, allocation_ratio_index):
                    fund_code = row[fund_code_index]
                    asset_type = row[asset_type_index]
                    try:
                        allocation_ratio = float(row[allocation_ratio_index])
                    except ValueError:
                        allocation_ratio = 0.0
                    
                    if fund_code not in temp_allocation:
                        temp_allocation[fund_code] = {'股票': 0.0, '其他': 0.0}
                    
                    if asset_type == '股票':
                        temp_allocation[fund_code]['股票'] = allocation_ratio
                    elif asset_type == '其他':
                        temp_allocation[fund_code]['其他'] = allocation_ratio
        
        # 计算最终的股票资产占比
        for fund_code, allocations in temp_allocation.items():
            fund_type = self.fund_types.get(fund_code, '')
            if fund_type == '指数型-股票':
                # 对于指数型-股票基金，股票资产占比 = 股票 + 其他
                self.fund_stock_allocation[fund_code] = allocations['股票'] + allocations['其他']
            else:
                # 其他基金类型，只计算股票类别
                self.fund_stock_allocation[fund_code] = allocations['股票']
        
        print(f'基金资产配置数据共 {len(self.fund_stock_allocation)} 只基金有股票资产占比')
    
    def load_fund_index_mapping(self, file_path='fund_index_mapping.csv'):
        """加载基金指数映射数据"""
        full_path = os.path.join(self.config_dir, file_path)
        with open(full_path, 'r', encoding='utf-8-sig') as f:
            reader = csv.reader(f)
            header = next(reader)
            
            fund_code_index = header.index('fund_code')
            index_code_index = header.index('index_code')
            effective_weight_index = header.index('effective_weight')
            
            for row in reader:
                if len(row) > max(fund_code_index, index_code_index, effective_weight_index):
                    fund_code = row[fund_code_index]
                    index_code = row[index_code_index]
                    try:
                        effective_weight = float(row[effective_weight_index])
                    except ValueError:
                        effective_weight = 0.0
                    
                    if fund_code not in self.fund_index_mapping:
                        self.fund_index_mapping[fund_code] = []
                    self.fund_index_mapping[fund_code].append({'index_code': index_code, 'effective_weight': effective_weight})
        print(f'基金指数映射数据共 {len(self.fund_index_mapping)} 只基金有指数映射')
    
    def load_index_weights(self, file_path='index_weights.csv'):
        """加载指数权重数据"""
        full_path = os.path.join(self.config_dir, file_path)
        with open(full_path, 'r', encoding='utf-8-sig') as f:
            reader = csv.reader(f)
            header = next(reader)
            
            index_code_index = header.index('index_code')
            stock_code_index = header.index('stock_code')
            weight_index = header.index('weight')
            
            for row in reader:
                if len(row) > max(index_code_index, stock_code_index, weight_index):
                    index_code = row[index_code_index]
                    stock_code = row[stock_code_index]
                    try:
                        weight = float(row[weight_index])
                    except ValueError:
                        weight = 0.0
                    
                    if index_code not in self.index_weights:
                        self.index_weights[index_code] = []
                    self.index_weights[index_code].append({'stock_code': stock_code, 'weight': weight})
        print(f'指数权重数据共 {len(self.index_weights)} 个指数')
    
    def load_index_data(self):
        """加载指数涨跌幅数据"""
        # 读取 index.csv 文件（上海指数）
        full_path = os.path.join(self.config_dir, 'index.csv')
        with open(full_path, 'r', encoding='utf-8-sig') as f:
            reader = csv.reader(f)
            header = next(reader)
            
            code_index = header.index('代码')
            change_index = header.index('涨跌幅')
            
            for row in reader:
                if len(row) > max(code_index, change_index):
                    full_code = row[code_index]
                    if len(full_code) == 8:
                        index_code = full_code[2:]
                    else:
                        index_code = full_code
                    try:
                        price_change = float(row[change_index])
                    except ValueError:
                        price_change = 0.0
                    self.index_price_change[index_code] = price_change
        print(f'index.csv 数据共 {len([k for k in self.index_price_change.keys() if len(k) == 6])} 个指数')
        
        # 读取 em_csindex.csv 文件（中证指数）
        full_path = os.path.join(self.config_dir, 'em_csindex.csv')
        with open(full_path, 'r', encoding='utf-8-sig') as f:
            reader = csv.reader(f)
            header = next(reader)
            
            code_index = header.index('代码')
            change_index = header.index('涨跌幅')
            
            for row in reader:
                if len(row) > max(code_index, change_index):
                    index_code = row[code_index]
                    try:
                        price_change = float(row[change_index])
                    except ValueError:
                        price_change = 0.0
                    self.index_price_change[index_code] = price_change
        print(f'em_csindex.csv 数据共 {len([k for k in self.index_price_change.keys() if not k.startswith("sh")])} 个指数')
        print(f'指数涨跌幅数据共 {len(self.index_price_change)} 个指数')
    
    def load_stock_data(self):
        """加载股票数据"""
        # 读取A股数据
        full_path = os.path.join(self.config_dir, 'stock.csv')
        with open(full_path, 'r', encoding='utf-8-sig') as f:
            reader = csv.reader(f)
            header = next(reader)
            
            code_index = header.index('代码')
            change_index = header.index('涨跌幅')
            
            for row in reader:
                if len(row) > max(code_index, change_index):
                    full_code = row[code_index]
                    if len(full_code) == 8:
                        stock_code = full_code[2:]
                    else:
                        print(f"警告：股票代码格式异常 - {full_code}")
                        stock_code = full_code
                    try:
                        price_change = float(row[change_index])
                    except ValueError:
                        price_change = 0.0
                    self.stock_data_a[stock_code] = price_change
        print(f'A股数据共 {len(self.stock_data_a)} 只股票')
        
        # 读取科创板数据
        full_path = os.path.join(self.config_dir, 'kcb_stock.csv')
        with open(full_path, 'r', encoding='utf-8-sig') as f:
            reader = csv.reader(f)
            header = next(reader)
            
            code_index = header.index('代码')
            change_index = header.index('涨跌幅')
            
            for row in reader:
                if len(row) > max(code_index, change_index):
                    full_code = row[code_index]
                    if len(full_code) == 8:
                        stock_code = full_code[2:]
                    else:
                        print(f"警告：股票代码格式异常 - {full_code}")
                        stock_code = full_code
                    try:
                        price_change = float(row[change_index])
                    except ValueError:
                        price_change = 0.0
                    self.stock_data_kcb[stock_code] = price_change
        print(f'科创板数据共 {len(self.stock_data_kcb)} 只股票')
        
        # 读取港股数据
        full_path = os.path.join(self.config_dir, 'hk_stock.csv')
        with open(full_path, 'r', encoding='utf-8-sig') as f:
            reader = csv.reader(f)
            header = next(reader)
            
            code_index = header.index('代码')
            change_index = header.index('涨跌幅')
            
            for row in reader:
                if len(row) > max(code_index, change_index):
                    stock_code = row[code_index]
                    try:
                        price_change = float(row[change_index])
                    except ValueError:
                        price_change = 0.0
                    self.stock_data_hk[stock_code] = price_change
        print(f'港股数据共 {len(self.stock_data_hk)} 只股票')
        
        # 读取美股数据
        full_path = os.path.join(self.config_dir, 'us_stock.csv')
        with open(full_path, 'r', encoding='utf-8-sig') as f:
            reader = csv.reader(f)
            header = next(reader)
            
            code_index = header.index('symbol')
            change_index = header.index('chg')
            
            for row in reader:
                if len(row) > max(code_index, change_index):
                    stock_code = row[code_index]
                    try:
                        price_change = float(row[change_index])
                    except ValueError:
                        price_change = 0.0
                    self.stock_data_us[stock_code] = price_change
        print(f'美股数据共 {len(self.stock_data_us)} 只股票')
    
    def load_all_data(self):
        """加载所有数据"""
        self.load_fund_asset_allocation()
        self.load_fund_index_mapping()
        self.load_index_weights()
        self.load_index_data()
        self.load_stock_data()

class FundNavEstimator:
    """基金估值计算器类，负责计算基金的估值"""
    def __init__(self, data_loader):
        """初始化基金估值计算器
        
        Args:
            data_loader: 数据加载器实例
        """
        self.data_loader = data_loader
        self.fund_results = {}  # 基金估值结果
        self.missing_stocks = []  # 缺失的股票代码
        self.fund_top10_weights = {}  # 基金十大持仓总权重
    
    def get_stock_price_change(self, stock_code):
        """
        按优先级查找股票涨跌幅
        优先级：A股 -> 科创板 -> 港股 -> 美股
        对于美股，如果找不到，尝试将下划线替换为点号再查找
        返回：(涨跌幅, 市场名称)
        """
        for i, market_data in enumerate(self.data_loader.market_priority):
            if stock_code in market_data:
                return market_data[stock_code], self.data_loader.market_names[i]
        
        # 如果在美股中找不到，尝试将下划线替换为点号
        if '_' in stock_code:
            alt_code = stock_code.replace('_', '.')
            if alt_code in self.data_loader.stock_data_us:
                return self.data_loader.stock_data_us[alt_code], self.data_loader.market_names[3]
        
        return 0.0, None
    
    def get_index_price_change(self, index_code):
        """
        获取指数涨跌幅
        优先从已读取的指数涨跌幅数据中查找
        如果找不到，再通过成分股计算
        返回：涨跌幅
        """
        # 优先从已读取的指数涨跌幅数据中查找
        if index_code in self.data_loader.index_price_change:
            return self.data_loader.index_price_change[index_code]
        
        # 如果找不到，通过成分股计算
        if index_code in self.data_loader.index_weights:
            index_nav_estimate = 0.0
            for stock_weight in self.data_loader.index_weights[index_code]:
                stock_code = stock_weight['stock_code']
                weight = stock_weight['weight'] / 100.0
                price_change, _ = self.get_stock_price_change(stock_code)
                index_nav_estimate += weight * price_change
            return index_nav_estimate
        
        return 0.0
    
    def is_stock_available(self, stock_code):
        """
        检查股票是否在任一市场中存在
        对于美股，如果找不到，尝试将下划线替换为点号再查找
        """
        for market_data in self.data_loader.market_priority:
            if stock_code in market_data:
                return True
        
        # 如果在美股中找不到，尝试将下划线替换为点号
        if '_' in stock_code:
            alt_code = stock_code.replace('_', '.')
            if alt_code in self.data_loader.stock_data_us:
                return True
        
        return False
    
    def calculate_fund_nav(self, test_fund_code=None):
        """计算基金估值"""
        # 获取配置目录路径
        config_dir = get_config_dir()
        full_path = os.path.join(config_dir, 'fund_holds_2025q4.csv')
        with open(full_path, 'r', encoding='utf-8-sig') as f:
            reader = csv.reader(f)
            header = next(reader)
            
            fund_code_index = header.index('fund_code')
            stock_code_index = header.index('stock_code')
            weight_index = header.index('weight')
            
            for row in reader:
                if len(row) > max(fund_code_index, stock_code_index, weight_index):
                    fund_code = row[fund_code_index]
                    stock_code = row[stock_code_index]
                    
                    # 如果指定了测试基金，只处理该基金
                    if test_fund_code and fund_code != test_fund_code:
                        continue
                    
                    try:
                        weight = float(row[weight_index])
                    except ValueError:
                        weight = 0.0
                    
                    price_change, market = self.get_stock_price_change(stock_code)
                    
                    if not self.is_stock_available(stock_code):
                        self.missing_stocks.append((fund_code, stock_code))
                    
                    # 十大持仓的股票贡献 = 持仓权重 × 股票涨跌幅
                    nav_contribution = weight * price_change
                    
                    if fund_code not in self.fund_results:
                        self.fund_results[fund_code] = {'nav_estimate': 0.0, 'weight': 0.0, 'actual_weight': 0.0, 'stock_allocation': 0.0, 'top10_contribution': 0.0, 'index_contribution': 0.0}
                    self.fund_results[fund_code]['nav_estimate'] += nav_contribution
                    self.fund_results[fund_code]['top10_contribution'] += nav_contribution
                    self.fund_results[fund_code]['weight'] += weight
                    self.fund_results[fund_code]['stock_allocation'] = self.data_loader.fund_stock_allocation.get(fund_code, 0.0)
                    if self.is_stock_available(stock_code):
                        self.fund_results[fund_code]['actual_weight'] += weight
                    
                    # 累计十大持仓总权重
                    if fund_code not in self.fund_top10_weights:
                        self.fund_top10_weights[fund_code] = 0.0
                    self.fund_top10_weights[fund_code] += weight
        
        # 计算剩余股票占比通过指数的估值
        for fund_code, result in self.fund_results.items():
            stock_allocation_ratio = self.data_loader.fund_stock_allocation.get(fund_code, 0.0)
            top10_weight = self.fund_top10_weights.get(fund_code, 0.0)
            
            # 剩余股票占比 = 100% - 十大持仓总权重
            remaining_stock_ratio = 100.0 - top10_weight
            
            if remaining_stock_ratio > 0 and fund_code in self.data_loader.fund_index_mapping:
                # 通过基金跟踪的指数计算剩余股票的估值
                for index_mapping in self.data_loader.fund_index_mapping[fund_code]:
                    index_code = index_mapping['index_code']
                    effective_weight = index_mapping['effective_weight']
                    
                    # 优先从指数涨跌幅数据中获取，找不到才通过成分股计算
                    index_nav_estimate = self.get_index_price_change(index_code)
                    print(f"指数 {index_code} 权重 {effective_weight} 估值: {index_nav_estimate}")
                    # 指数贡献 = 剩余股票占比 × 指数有效权重 × 指数估值
                    nav_contribution = remaining_stock_ratio * effective_weight * index_nav_estimate
                    result['nav_estimate'] += nav_contribution
                    result['index_contribution'] += nav_contribution
            
            # 总涨跌幅 = 股票涨跌幅 × 股票资产百分比
            # 因为权重是百分比，所以需要除以100来得到实际涨跌幅
            total_stock_return = result['nav_estimate'] / 100.0
            result['nav_estimate'] = total_stock_return * stock_allocation_ratio

class ResultWriter:
    """结果写入器类，负责将计算结果写入文件并打印"""
    def __init__(self, fund_nav_estimator):
        """初始化结果写入器
        
        Args:
            fund_nav_estimator: 基金估值计算器实例
        """
        self.fund_nav_estimator = fund_nav_estimator
    
    def write_fund_nav_estimate(self, file_path='fund_nav_estimate.csv'):
        """写入基金估值结果"""
        # 获取配置目录路径
        config_dir = get_config_dir()
        full_path = os.path.join(config_dir, file_path)
        with open(full_path, 'w', newline='', encoding='utf-8') as f:
            writer = csv.writer(f)
            writer.writerow(['fund_code', 'nav_estimate', 'weight', 'actual_weight', 'stock_allocation'])
            for fund_code, result in self.fund_nav_estimator.fund_results.items():
                writer.writerow([fund_code, result['nav_estimate'], round(result['weight'], 2), round(result['actual_weight'], 2), round(result['stock_allocation'], 4)])
        print("基金估值计算完成，结果已写入fund_nav_estimate.csv")
    
    def write_missing_stocks(self, file_path='missing_stocks.csv'):
        """写入缺失的股票代码"""
        missing_stocks = self.fund_nav_estimator.missing_stocks
        if missing_stocks:
            # 获取配置目录路径
            config_dir = get_config_dir()
            full_path = os.path.join(config_dir, file_path)
            with open(full_path, 'w', newline='', encoding='utf-8') as f:
                writer = csv.writer(f)
                writer.writerow(['fund_code', 'stock_code'])
                for fund_code, stock_code in missing_stocks:
                    writer.writerow([fund_code, stock_code])
            print(f"共找到 {len(missing_stocks)} 个缺失的股票代码和对应基金，已写入missing_stocks.csv")
        else:
            print("所有股票代码都已找到，无需生成missing_stocks.csv")
    
    def print_contribution_details(self):
        """打印基金涨跌幅贡献明细"""
        print("\n" + "="*80)
        print("基金涨跌幅贡献明细")
        print("="*80)
        print(f'{"基金代码":<12} {"十大持仓贡献":<15} {"指数贡献":<15} {"总贡献":<15} {"股票资产占比":<12}')
        print("-"*80)
        for fund_code, result in self.fund_nav_estimator.fund_results.items():
            top10_contrib = result['top10_contribution'] / 100.0 * result['stock_allocation']
            index_contrib = result['index_contribution'] / 100.0 * result['stock_allocation']
            total_contrib = result['nav_estimate']
            print(f"{fund_code:<12} {top10_contrib:>12.4f}% {index_contrib:>12.4f}% {total_contrib:>12.4f}% {result['stock_allocation']:>10.2f}%")
        print("="*80)

class Main:
    """主类，负责协调各个模块的运行"""
    def __init__(self):
        """初始化主类"""
        self.data_loader = DataLoader()  # 数据加载器实例
        self.fund_nav_estimator = None  # 基金估值计算器实例
        self.result_writer = None  # 结果写入器实例
    
    def run(self, test_fund_code=None):
        """运行主流程
        
        Args:
            test_fund_code: 可选，指定测试的基金代码
        """
        # 加载数据
        self.data_loader.load_all_data()
        
        # 初始化估值计算器
        self.fund_nav_estimator = FundNavEstimator(self.data_loader)
        
        # 计算基金估值
        self.fund_nav_estimator.calculate_fund_nav(test_fund_code)
        
        # 初始化结果写入器
        self.result_writer = ResultWriter(self.fund_nav_estimator)
        
        # 写入结果
        self.result_writer.write_fund_nav_estimate()
        self.result_writer.print_contribution_details()
        self.result_writer.write_missing_stocks()

if __name__ == "__main__":
    # 获取命令行参数，指定测试的基金代码
    test_fund_code = None
    if len(sys.argv) > 1:
        test_fund_code = sys.argv[1]
        print(f"指定测试基金: {test_fund_code}")
    
    # 运行主程序
    main = Main()
    main.run(test_fund_code)