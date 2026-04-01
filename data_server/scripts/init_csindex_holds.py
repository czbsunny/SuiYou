#!/usr/bin/env python3
# -*- coding:utf-8 -*-
"""
初始化中证指数权重脚本
从中证指数官网下载指数权重数据并保存到数据库
"""

import logging
import sys
import os
import requests
import pandas as pd
from datetime import datetime
from pathlib import Path

# 添加项目根目录到 Python 搜索路径
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from database.init_db import SessionLocal
from models.index_weights import IndexWeight

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# 中证指数权重数据下载URL模板
CSINDEX_WEIGHT_URL_TEMPLATE = "https://oss-ch.csindex.com.cn/static/html/csindex/public/uploads/file/autofile/closeweight/{index_code}closeweight.xls"

# 配置文件路径
CONFIG_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'config')
CSINDEX_CONFIG_FILE = os.path.join(CONFIG_DIR, 'csindex.csv')

# 临时数据目录
TEMP_DATA_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'temp_data')


def get_index_codes():
    """
    从配置文件读取需要初始化的指数代码列表
    """
    index_codes = []
    
    if not os.path.exists(CSINDEX_CONFIG_FILE):
        logger.error(f"配置文件不存在: {CSINDEX_CONFIG_FILE}")
        return index_codes
    
    try:
        with open(CSINDEX_CONFIG_FILE, 'r', encoding='utf-8') as f:
            for line in f:
                line = line.strip()
                if line and not line.startswith('index_code'):
                    # 移除引号
                    index_code = line.strip('"')
                    if index_code:
                        index_codes.append(index_code)
        
        logger.info(f"从配置文件读取到 {len(index_codes)} 个指数代码")
        return index_codes
    except Exception as e:
        logger.error(f"读取配置文件失败: {str(e)}")
        return index_codes


def download_index_weight(index_code):
    """
    下载指定指数的权重数据
    返回: (success, file_path_or_error_message)
    """
    url = CSINDEX_WEIGHT_URL_TEMPLATE.format(index_code=index_code)
    file_name = f"{index_code}_closeweight.xls"
    file_path = os.path.join(TEMP_DATA_DIR, file_name)
    
    try:
        logger.info(f"开始下载指数 {index_code} 的权重数据: {url}")
        
        # 发送请求
        response = requests.get(url, timeout=30)
        
        # 检查响应状态
        if response.status_code != 200:
            return False, f"下载失败，HTTP状态码: {response.status_code}"
        
        # 检查内容长度
        content_length = len(response.content)
        if content_length < 1000:
            return False, f"下载内容过小，可能数据不存在"
        
        # 保存文件
        with open(file_path, 'wb') as f:
            f.write(response.content)
        
        logger.info(f"指数 {index_code} 权重数据下载成功，文件大小: {content_length} 字节")
        return True, file_path
        
    except requests.exceptions.Timeout:
        return False, "下载超时"
    except requests.exceptions.RequestException as e:
        return False, f"下载请求失败: {str(e)}"
    except Exception as e:
        return False, f"下载失败: {str(e)}"


def parse_xls_file(file_path):
    """
    解析XLS文件，提取权重数据
    返回: DataFrame 或 None
    """
    try:
        df = pd.read_excel(file_path, dtype=str)
        
        # 检查必要的列是否存在
        required_columns = ['日期Date', '指数代码 Index Code', '成份券代码Constituent Code', '权重(%)weight']
        
        # 获取实际的列名
        actual_columns = df.columns.tolist()
        
        # 检查是否包含必要的列
        missing_columns = [col for col in required_columns if col not in actual_columns]
        if missing_columns:
            logger.warning(f"文件 {file_path} 缺少必要的列: {missing_columns}")
            logger.info(f"实际列名: {actual_columns}")
            return None
        
        # 确保权重列转换为浮点数
        if '权重(%)weight' in df.columns:
            df['权重(%)weight'] = pd.to_numeric(df['权重(%)weight'], errors='coerce')
        
        logger.info(f"成功解析文件 {file_path}，共 {len(df)} 条记录")
        return df
        
    except Exception as e:
        logger.error(f"解析文件 {file_path} 失败: {str(e)}")
        return None


def save_to_database(df, index_code):
    """
    将解析后的数据保存到数据库
    返回: (success_count, error_message)
    """
    db = SessionLocal()
    success_count = 0
    skipped_count = 0
    
    try:
        # 遍历DataFrame中的每一行
        for _, row in df.iterrows():
            try:
                # 解析日期
                date_str = row['日期Date']
                if pd.isna(date_str):
                    continue
                
                # 处理日期格式
                if isinstance(date_str, str):
                    # 尝试多种日期格式
                    try:
                        # 先尝试 YYYY-MM-DD 格式
                        effective_date = datetime.strptime(date_str, '%Y-%m-%d').date()
                    except ValueError:
                        try:
                            # 再尝试 YYYYMMDD 格式
                            effective_date = datetime.strptime(date_str, '%Y%m%d').date()
                        except ValueError:
                            # 其他格式处理
                            logger.warning(f"日期格式解析失败: {date_str}")
                            continue
                else:
                    effective_date = date_str.date() if hasattr(date_str, 'date') else date_str
                
                # 获取关键字段
                # 使用传入的index_code，避免从Excel中解析可能导致的格式问题
                idx_code = index_code
                stk_code = str(row['成份券代码Constituent Code']).strip()
                
                # 检查是否已存在相同记录
                existing = db.query(IndexWeight).filter(
                    IndexWeight.index_code == idx_code,
                    IndexWeight.stock_code == stk_code,
                    IndexWeight.effective_date == effective_date
                ).first()
                
                if existing:
                    skipped_count += 1
                    continue
                
                # 创建IndexWeight对象
                index_weight = IndexWeight(
                    index_code=idx_code,
                    index_name=str(row.get('指数名称 Index Name', '')).strip(),
                    index_name_en=str(row.get('指数英文名称Index Name(Eng)', '')).strip(),
                    stock_code=stk_code,
                    stock_name=str(row.get('成份券名称Constituent Name', '')).strip(),
                    stock_name_en=str(row.get('成份券英文名称Constituent Name(Eng)', '')).strip(),
                    exchange=str(row.get('交易所Exchange', '')).strip(),
                    exchange_en=str(row.get('交易所英文名称Exchange(Eng)', '')).strip(),
                    weight=float(row['权重(%)weight']) if pd.notna(row['权重(%)weight']) else None,
                    effective_date=effective_date,
                    source='csindex'
                )
                
                # 添加到数据库
                db.add(index_weight)
                success_count += 1
                
            except Exception as e:
                logger.warning(f"保存单条记录失败: {str(e)}")
                continue
        
        # 提交事务
        db.commit()
        logger.info(f"指数 {index_code} 数据保存成功，新增 {success_count} 条记录，跳过 {skipped_count} 条重复记录")
        return success_count, None
        
    except Exception as e:
        db.rollback()
        error_msg = f"保存数据到数据库失败: {str(e)}"
        logger.error(error_msg)
        return 0, error_msg
    finally:
        db.close()


def init_csindex_weights():
    """
    初始化中证指数权重数据
    """
    logger.info("=" * 60)
    logger.info("开始初始化中证指数权重数据")
    logger.info("=" * 60)
    
    # 确保临时数据目录存在
    Path(TEMP_DATA_DIR).mkdir(parents=True, exist_ok=True)
    
    # 获取需要初始化的指数代码列表
    index_codes = get_index_codes()
    
    if not index_codes:
        logger.error("没有获取到需要初始化的指数代码")
        return
    
    # 统计信息
    total_count = len(index_codes)
    success_count = 0
    download_failed_count = 0
    parse_failed_count = 0
    save_failed_count = 0
    
    # 遍历每个指数代码
    for i, index_code in enumerate(index_codes, 1):
        logger.info(f"\n处理进度: [{i}/{total_count}] 指数代码: {index_code}")
        logger.info("-" * 60)
        
        # 下载权重数据
        download_success, result = download_index_weight(index_code)
        
        if not download_success:
            logger.warning(f"指数 {index_code} 下载失败: {result}")
            download_failed_count += 1
            continue
        
        file_path = result
        
        # 解析XLS文件
        df = parse_xls_file(file_path)
        
        if df is None or df.empty:
            logger.warning(f"指数 {index_code} 解析失败或数据为空")
            parse_failed_count += 1
            continue
        
        # 保存到数据库
        saved_count, error_msg = save_to_database(df, index_code)
        
        if error_msg:
            logger.error(f"指数 {index_code} 保存失败: {error_msg}")
            save_failed_count += 1
        else:
            success_count += 1
    
    # 输出统计结果
    logger.info("\n" + "=" * 60)
    logger.info("初始化完成统计")
    logger.info("=" * 60)
    logger.info(f"总指数数量: {total_count}")
    logger.info(f"成功: {success_count}")
    logger.info(f"下载失败: {download_failed_count}")
    logger.info(f"解析失败: {parse_failed_count}")
    logger.info(f"保存失败: {save_failed_count}")
    logger.info("=" * 60)


if __name__ == "__main__":
    init_csindex_weights()
