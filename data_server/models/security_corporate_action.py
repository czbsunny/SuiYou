from datetime import datetime
from sqlalchemy import Column, String, DateTime, Date, Numeric, BigInteger, ForeignKey, Index
from models.base import Base


class SecurityCorporateAction(Base):
    __tablename__ = 'security_corporate_actions'

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    security_id = Column(BigInteger, ForeignKey('securities.id'), index=True)

    action_type = Column(String(20), nullable=False)

    base_qty = Column(Numeric(20, 4), default=1)
    target_qty = Column(Numeric(20, 4), default=0)

    target_security_id = Column(BigInteger, ForeignKey('securities.id'), nullable=True)

    cash_amount = Column(Numeric(20, 4), default=0)
    currency = Column(String(10))

    ex_date = Column(Date, nullable=False, index=True)
    record_date = Column(Date)
    pay_date = Column(Date)

    description = Column(String(500))

    created_at = Column(DateTime, default=datetime.now)
    updated_at = Column(DateTime, default=datetime.now, onupdate=datetime.now)

    __table_args__ = (
        Index('idx_security_ex_date', 'security_id', 'ex_date'),
    )

    def __repr__(self):
        return f"<SecurityCorporateAction(security_id={self.security_id}, action_type={self.action_type}, ex_date={self.ex_date})>"
