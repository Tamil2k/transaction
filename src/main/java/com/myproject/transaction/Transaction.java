package com.myproject.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Represents transaction object
 */
public class Transaction {

	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

	private final String transactionId;
	private final String fromAccountId;
	private final String toAccountId;
	private final LocalDateTime createdAt;
	private final BigDecimal amount;
	private final Type transactionType;
	// Can be null, if it is not reversal transaction
	private final String relatedTransaction;

	public Transaction(final String transactionId, final String fromAccountId, final String toAccountId,
			final LocalDateTime createdAt, final BigDecimal amount, final Type transactionType,
			final String relatedTransaction) {
		this.transactionId = transactionId;
		this.fromAccountId = fromAccountId;
		this.toAccountId = toAccountId;
		this.createdAt = createdAt;
		this.amount = amount;
		this.transactionType = transactionType;
		this.relatedTransaction = relatedTransaction;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public String getFromAccountId() {
		return fromAccountId;
	}

	public String getToAccountId() {
		return toAccountId;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public Type getTransactionType() {
		return transactionType;
	}

	public String getRelatedTransaction() {
		return relatedTransaction;
	}

	/**
	 * Parses array of elements into a transaction
	 *
	 * @param elements
	 *            expected to be an array of arbitrary number of elements, but
	 *            only 6 first will be considered and represent 'transactionId',
	 *            'fromAccountId', 'toAccountId', 'createdAt', 'amount',
	 *            'transactionType'. This constructor consider
	 *            'relatedTransaction' to be null
	 */
	public Transaction(final String[] elements) {
		this(elements, null);
	}

	/**
	 * Parses array of elements into a transaction
	 *
	 * @param elements
	 *            expected to be an array of arbitrary number of elements, but
	 *            only 6 first will be considered and represent 'transactionId',
	 *            'fromAccountId', 'toAccountId', createdAt', 'amount',
	 *            'transactionType' and 'relatedTransaction' is 7th element
	 * @param relatedTransactionElement
	 *            represents 'relatedTransaction' and can be null
	 */
	public Transaction(final String[] elements, final String relatedTransactionElement) {
		this(elements[0].trim(), elements[1].trim(), elements[2].trim(),
				LocalDateTime.parse(elements[3].trim(), DATE_TIME_FORMATTER), new BigDecimal(elements[4].trim()),
				Type.valueOf(elements[5].trim()),
				Optional.ofNullable(relatedTransactionElement).map(String::trim).orElse(null));
	}

	@Override
	public String toString() {
		return "Transaction{" + "transactionId='" + transactionId + '\'' + ", fromAccountId='" + fromAccountId + '\''
				+ ", toAccountId='" + toAccountId + '\'' + ", createdAt=" + createdAt + ", amount=" + amount
				+ ", transactionType=" + transactionType + ", relatedTransaction='" + relatedTransaction + '\'' + '}';
	}

	/**
	 * Transaction type
	 */
	public enum Type {
		PAYMENT, REVERSAL
	}
}
