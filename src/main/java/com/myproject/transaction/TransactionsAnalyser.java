package com.myproject.transaction;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Tamil.rajendran
 *
 */
public class TransactionsAnalyser {

	private static final String CSV_DATA_FILE_NAME = "transactions.csv";

	private final List<Transaction> transactions;

	/**
	 * Initiates transaction analyser for the .csv file located at the path. As
	 * we expect .csv data file to be in a valid format, we do not perform
	 * additional validations here
	 *
	 * @param path
	 *            path to .csv file with data
	 */
	public TransactionsAnalyser(final Path path) {
		try (Stream<String> lines = Files.lines(path)) {
			// In fact here, we can preprocess transactions and group those by
			// 'accountId' for quick access, but we leave performance
			// optimisations out for now and keep it simple
			transactions = lines
					// First line in the .csv data file contain headers, but as
					// we know files structure already, we are skipping a first
					// line
					.skip(1)
					// Split every line from file and get an array of elements
					// which represent data we need
					.map(line -> line.split(","))
					// Map every array of elements to a transaction object
					.map(elements -> {
						if (elements.length == 6) {
							// When it is a PAYMENT transaction we might not
							// have last 'relatedTransaction' element, so length
							// is 6 and 'relatedTransaction' is null
							return new Transaction(elements);
						} else if (elements.length == 7) {
							// When it is a REVERSAL transaction we should have
							// 'relatedTransaction' element, so length is 7 and
							// 'relatedTransaction' is 7th element
							return new Transaction(elements, elements[6]);
						} else {
							throw new IllegalArgumentException(
									"Could not read .csv file, as encountered unexpected number of elements in the line: "
											+ Arrays.toString(elements));
						}
					}).collect(Collectors.toList());
		} catch (IOException ioe) {
			throw new IllegalArgumentException("Could not read .csv file at the following path: " + path);
		}

		// This is for debugging purposes, could be removed later
		// System.out.println("Transactions");
		// transactions.forEach(System.out::println);
	}

	
	/**
	 * Calculates relativeBalance for paymentTransactionsInRange
	 * @param paymentTransactionsInRange
	 * @param accountId
	 * @return relativeBalance
	 */
	
	private BigDecimal calculateRealtiveBalance(final List<Transaction> paymentTransactionsInRange,
			final String accountId) {

		final BigDecimal relativeBalance = paymentTransactionsInRange.stream().reduce(BigDecimal.ZERO,
				(final BigDecimal bigDecimal, final Transaction transaction) -> {
					// If 'accountId' is 'fromAccountId' we subtract
					if (accountId.equals(transaction.getFromAccountId())) {
						return bigDecimal.subtract(transaction.getAmount());
					}
					// If 'accountId' is 'fromAccountId' we add
					if (accountId.equals(transaction.getToAccountId())) {
						return bigDecimal.add(transaction.getAmount());
					}
					return bigDecimal;
				}, BigDecimal::add);

		return relativeBalance;

	}

	/**
	 * Find transactions within timeframe
	 * @param paymentAndReversalGroupedTransactions
	 * @param from
	 * @param to
	 * @return 
	 */
	private List<Transaction> findTransactionsInRange(
			final Map<Transaction.Type, List<Transaction>> paymentAndReversalGroupedTransactions,
			final LocalDateTime from, final LocalDateTime to) {

		// Use Optional, to avoid potential NullPointerException, if there is no
		// REVERSAL transactions for passed account
		final List<String> relatedTransactionIds = Optional
				.ofNullable(paymentAndReversalGroupedTransactions.get(Transaction.Type.REVERSAL))
				.orElse(new ArrayList<>()).stream()
				// Get related transaction ids of reversal transactions
				.map(Transaction::getRelatedTransaction).collect(Collectors.toList());

		// Use Optional, to avoid potential NullPointerException, if there is no
		// PAYMENT transactions for passed account
		final List<Transaction> paymentTransactionsInRange = Optional
				.ofNullable(paymentAndReversalGroupedTransactions.get(Transaction.Type.PAYMENT))
				.orElse(new ArrayList<>()).stream()
				// Filter out transactions which are out of range (consider
				// range to be inclusive [from, to])
				.filter(transaction -> !from.isAfter(transaction.getCreatedAt())
						&& !to.isBefore(transaction.getCreatedAt()))
				// Remove from the list transactions which have related reversal
				// transactions
				.filter(transaction -> !relatedTransactionIds.contains(transaction.getTransactionId()))
				.collect(Collectors.toList());

		return paymentTransactionsInRange;

	}

	/**
	 * Find all relativetransactions for the acountid
	 * @param accountId
	 * @return
	 */
	private Map<Transaction.Type, List<Transaction>> findRelativeTransactions(final String accountId) {

		final Map<Transaction.Type, List<Transaction>> paymentAndReversalGroupedTransactions = transactions.stream()
				// Find transactions which has 'accountId' either in
				// fromAccountId or toAccountId
				.filter(transaction -> accountId.equals(transaction.getFromAccountId())
						|| accountId.equals(transaction.getToAccountId()))
				// Now split found transactions into two groups PAYMENT and
				// REVERSAL
				.collect(Collectors.groupingBy(Transaction::getTransactionType, Collectors.toList()));

		return paymentAndReversalGroupedTransactions;
	}

	/**
	 * @param accountId
	 * @param from
	 * @param to
	 * @return
	 */
	public TransactionsAnalysis analise(final String accountId, final LocalDateTime from, final LocalDateTime to) {

		final Map<Transaction.Type, List<Transaction>> paymentAndReversalGroupedTransactions = findRelativeTransactions(
				accountId);

		// Use Optional, to avoid potential NullPointerException, if there is no
		// PAYMENT transactions for passed account
		final List<Transaction> paymentTransactionsInRange = findTransactionsInRange(
				paymentAndReversalGroupedTransactions, from, to);

		// Calculate relative balance
		final BigDecimal relativeBalance = calculateRealtiveBalance(paymentTransactionsInRange, accountId);

		return new TransactionsAnalysis(relativeBalance, paymentTransactionsInRange);
	}

	public static void main(final String[] args) throws Exception {
		// There should be 3 arguments to the application .csv data file,
		// account id, from date, to date
		if (args == null || args.length < 3) {
			throw new IllegalArgumentException(
					"Invalid number of arguments supplied, there should be 3 arguments to the application: account id, from date, to date"
							+ "\nHere is an example of the correct run: java -jar transactions-analyser.jar \"ACC334455\" \"20/10/2018 12:00:00\" \"20/10/2018 19:00:00\"");
		}

		// Attempt to resolve path to the .csv data file, .csv data file is
		// expected to be in the same directory as a .jar file with name
		// transactions.csv
		final URL csvUrl = TransactionsAnalyser.class.getClassLoader().getResource(CSV_DATA_FILE_NAME);
		if (csvUrl == null) {
			throw new IllegalArgumentException("Could not locate a .csv file at the following url: " + csvUrl);
		}
		System.out.println(csvUrl);
		final Path csvPath = Paths.get(csvUrl.toURI()).normalize();
		final Path path = csvPath.isAbsolute() ? csvPath.normalize()
				: Paths.get(System.getProperty("user.dir")).resolve(csvPath).normalize();
		if (!path.toFile().exists()) {
			throw new IllegalArgumentException("Could not locate a .csv file at the following path: " + path);
		}

		// No validation for account id, it is already a string
		final String accountId = args[0].trim();

		// We expect dates to be passed in 'dd/MM/yyyy HH:mm:ss', there is no
		// additional validation here for now, can be added if necessary
		final LocalDateTime from = LocalDateTime.parse(args[1].trim(), Transaction.DATE_TIME_FORMATTER);

		// We expect dates to be passed in 'dd/MM/yyyy HH:mm:ss', there is no
		// additional validation here for now, can be added if necessary
		final LocalDateTime to = LocalDateTime.parse(args[2].trim(), Transaction.DATE_TIME_FORMATTER);

		// Initialize transaction analyser with the .csv data file
		final TransactionsAnalysis transactionsAnalysis = new TransactionsAnalyser(path).analise(accountId, from, to);

		System.out.println("Relative balance for the period is: " + transactionsAnalysis.getRelativeBalance());
		System.out.println(
				"Number of transactions included is: " + transactionsAnalysis.getPaymentTransactionsInRange().size());
	}
}
