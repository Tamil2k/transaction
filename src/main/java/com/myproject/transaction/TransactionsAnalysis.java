package com.myproject.transaction;

import java.math.BigDecimal;
import java.util.List;

/**
 * Represents transaction analysis
 */
public class TransactionsAnalysis {

	private final BigDecimal relativeBalance;
	private final List<Transaction> paymentTransactionsInRange;

	public TransactionsAnalysis(final BigDecimal relativeBalance, final List<Transaction> paymentTransactionsInRange) {
		this.relativeBalance = relativeBalance;
		this.paymentTransactionsInRange = paymentTransactionsInRange;
	}

	public BigDecimal getRelativeBalance() {
		return relativeBalance;
	}

	public List<Transaction> getPaymentTransactionsInRange() {
		return paymentTransactionsInRange;
	}

	@Override
	public String toString() {
		return "TransactionAnalysis{" + "relativeBalance=" + relativeBalance + ", paymentTransactionsInRange="
				+ paymentTransactionsInRange + '}';
	}
}
