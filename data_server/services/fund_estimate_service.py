from datetime import datetime, timedelta
from typing import List, Dict, Optional
import logging
from elasticsearch import Elasticsearch

logger = logging.getLogger(__name__)


class FundEstimateService:
    """
    基金预估涨跌服务类
    负责从ES获取基金预估涨跌数据，并提供缓存机制
    """
    
    def __init__(self, es: Optional[Elasticsearch] = None, cache_expire_minutes: int = 5):
        """
        初始化基金预估涨跌服务
        
        Args:
            es: Elasticsearch客户端，如果为None则自动创建
            cache_expire_minutes: 缓存过期时间（分钟），默认5分钟
        """
        self.es = None
        if es is None:
            try:
                self.es = Elasticsearch("http://localhost:9200", request_timeout=5)
                if not self.es.ping():
                    raise RuntimeError("ES ping failed")
                logger.info("ES客户端初始化成功")

            except Exception:
                logger.exception("ES客户端初始化失败")
                self.es = None
        else:
            self.es = es
        
        self.cache_expire_minutes = cache_expire_minutes
        self.cache: Dict[str, Dict] = {}
        self.cache_time: Dict[str, datetime] = {}
        self.index_name = "fund_change_latest"
    
    def get_fund_estimates(self, fund_codes: List[str]) -> Dict[str, Dict]:
        """
        获取基金预估涨跌数据
        
        先从缓存中查找，如果缓存中没有或已过期，则从ES查询
        
        Args:
            fund_codes: 基金代码列表
            
        Returns:
            基金预估涨跌数据字典，格式: {基金代码: {fund_id, change, timestamp}}
        """
        if not fund_codes:
            return {}
        
        result = {}
        uncached_codes = []
        
        # 先从缓存中获取数据
        for fund_code in fund_codes:
            if self._is_cache_valid(fund_code):
                result[fund_code] = self.cache[fund_code]
                logger.debug(f"从缓存获取基金 {fund_code} 的预估涨跌数据")
            else:
                uncached_codes.append(fund_code)
        
        # 从ES查询未缓存的数据
        if uncached_codes:
            es_data = self._fetch_from_es(uncached_codes)
            
            # 更新缓存
            for fund_code, data in es_data.items():
                self.cache[fund_code] = data
                self.cache_time[fund_code] = datetime.now()
                result[fund_code] = data
                logger.debug(f"从ES获取并缓存基金 {fund_code} 的预估涨跌数据")
        
        logger.info(f"获取 {len(result)} 只基金的预估涨跌数据（缓存: {len(result) - len(uncached_codes)}, ES: {len(es_data) if uncached_codes else 0}）")
        return result
    
    def _is_cache_valid(self, fund_code: str) -> bool:
        """
        检查缓存是否有效
        
        Args:
            fund_code: 基金代码
            
        Returns:
            缓存是否有效
        """
        if fund_code not in self.cache or fund_code not in self.cache_time:
            return False
        
        cache_age = datetime.now() - self.cache_time[fund_code]
        return cache_age < timedelta(minutes=self.cache_expire_minutes)
    
    def _fetch_from_es(self, fund_codes: List[str]) -> Dict[str, Dict]:
        """
        从ES批量获取基金预估涨跌数据
        
        Args:
            fund_codes: 基金代码列表
            
        Returns:
            基金预估涨跌数据字典
        """
        if not self.es:
            logger.error("ES客户端未初始化")
            return {}
        
        # 检查索引是否存在
        if not self.es.indices.exists(index=self.index_name):
            logger.warning(f"ES索引 {self.index_name} 不存在")
            return {}
        
        result = {}
        
        # 批量查询基金涨跌数据
        for fund_code in fund_codes:
            try:
                response = self.es.get(index=self.index_name, id=fund_code)
                if response['found']:
                    source = response['_source']
                    result[fund_code] = {
                        'fund_id': source['fund_id'],
                        'change': source['change'],
                        'timestamp': source['timestamp']
                    }
            except Exception as e:
                # 如果某个基金不存在，跳过
                logger.debug(f"基金 {fund_code} 在ES中未找到: {str(e)}")
                continue
        
        return result
    
    def clear_cache(self, fund_code: Optional[str] = None):
        """
        清除缓存
        
        Args:
            fund_code: 基金代码，如果为None则清除所有缓存
        """
        if fund_code:
            if fund_code in self.cache:
                del self.cache[fund_code]
            if fund_code in self.cache_time:
                del self.cache_time[fund_code]
            logger.info(f"清除基金 {fund_code} 的缓存")
        else:
            self.cache.clear()
            self.cache_time.clear()
            logger.info("清除所有基金预估涨跌缓存")
    
    def get_cache_stats(self) -> Dict:
        """
        获取缓存统计信息
        
        Returns:
            缓存统计信息字典
        """
        valid_count = sum(1 for code in self.cache if self._is_cache_valid(code))
        expired_count = len(self.cache) - valid_count
        
        return {
            'total_cached': len(self.cache),
            'valid_count': valid_count,
            'expired_count': expired_count,
            'cache_expire_minutes': self.cache_expire_minutes
        }

# 创建一个全局的基金处理器实例，方便API层调用
fund_estimate_service = FundEstimateService()