from models.fund_index_mapping import FundIndexMapping
from utils.fund_utils import FundUtils
from sqlalchemy.orm import Session
from models.fund import Fund

class FundService:
    """
    基金服务类，用于处理基金相关的业务逻辑
    """

    def __init__(self, db: Session):
        """
        初始化基金服务
        
        Args:
            db: 数据库会话
        """
        self.db = db

    def update_benchmark_by_expr(self, fund_code: str, benchmark_expr: str):
        benchmark_weights = FundUtils.parse_benchmark_weights(benchmark_expr)
        if not benchmark_weights:
            raise ValueError(f"无效的基准表达式: {benchmark_expr}")
        
        # 删除现有的基金指数映射记录
        self.db.query(FundIndexMapping).filter(FundIndexMapping.fund_code == fund_code).delete()
        
        # 去重并合并权重：相同 index_name 的记录合并，权重相加
        merged_weights = {}
        for item in benchmark_weights:
            index_code = item.get('index_code')
            index_name = item.get('index_name')
            weight = item.get('weight')
            
            if index_code and weight:
                # 使用 index_name 作为键进行去重
                if index_name in merged_weights:
                    # 如果已经存在相同的 index_name，权重相加
                    merged_weights[index_name]['weight'] += weight
                else:
                    # 如果不存在相同的 index_name，添加到字典中
                    merged_weights[index_name] = {
                        'index_code': index_code,
                        'weight': weight
                    }
        
        # 为合并后的每个基准权重创建新的映射记录
        for index_name, data in merged_weights.items():
            mapping = FundIndexMapping(
                fund_code=fund_code,
                index_code=data['index_code'],
                index_name=index_name,
                effective_weight=data['weight'],
                proxy_type='Index'  # 默认使用Index代理类型
            )
            self.db.add(mapping)
        
        self.db.commit()

    def update_benchmark_by_code(self, fund_code: str):
        fund = self.db.query(Fund).filter(Fund.fund_code == fund_code).first()
        if not fund:
            raise ValueError(f"基金 {fund_code} 不存在")

        self.update_benchmark_by_expr(fund_code, fund.benchmark)