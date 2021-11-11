package com.lindar.bingo90generator.service;

import com.lindar.bingo90generator.entity.Ticket;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.lindar.bingo90generator.constant.TicketConstants.*;

@Service
public class TicketService {

    private int[][] bingoTicket;

    private Random random = new Random();

    /**
     * Crete Bingo Tickets
     *
     * @param totalStripCount
     * @return
     */
    public List<Ticket> createBingoTickets(int totalStripCount) {

        List<Ticket> bingoStrips = new ArrayList<>();

        for (int i = 0; i < totalStripCount; i++) {
            bingoStrips.add(createSingleBingoTicket());
        }
        return bingoStrips;
    }

    /**
     * Create Single Bingo Ticket
     *
     * @return
     */
    public Ticket createSingleBingoTicket() {


        this.bingoTicket = new int[TICKET_ROWS][TICKET_COLUMNS];

        int[] rowNumbers = new int[TICKET_ROWS];

        /**
         * Listing all numbers
         */
        List<Integer> allBingoNumbersList = IntStream.rangeClosed(1, BINGO_90)
                .boxed().collect(Collectors.toList());

        /**
         * Gives initial values for storing list sof numbers
         */
        List<List<Integer>> columnNumberLists = new ArrayList<>();
        for (int i = 0; i < TICKET_COLUMNS; i++) {
            columnNumberLists.add(new ArrayList<>());
        }


        allBingoNumbersList.forEach(number -> {
            columnNumberLists.get(Math.min(number / 10, 8)).add(number);
        });

        columnNumberLists.forEach(list -> Collections.shuffle(list));

        generateColumnNumberLists(rowNumbers, allBingoNumbersList, columnNumberLists);

        generateFullNumberList(rowNumbers, allBingoNumbersList);
        this.sortTicketColumns();
        return new Ticket(bingoTicket);
    }

    /**
     * Generates Full Number List
     *
     * @param rowNumbersCounter
     * @param fullNumberList
     */
    private void generateFullNumberList(int[] rowNumbersCounter, List<Integer> fullNumberList) {
        fullNumberList.forEach(number -> {
            int columnId = Math.min(number / 10, 8);
            IntStream.range(0, TICKET_ROWS).boxed()
                    .filter(rowId -> bingoTicket[rowId][columnId] == 0 && rowNumbersCounter[rowId] < MAX_ROW_NUMBERS)
                    .findFirst()
                    .ifPresentOrElse(
                            rowId -> {
                                bingoTicket[rowId][columnId] = number;
                                rowNumbersCounter[rowId]++;
                            }, () -> {
                                int rowIdForNumberToBeAssigned = IntStream.range(0, rowNumbersCounter.length).boxed()
                                        .sorted(Collections.reverseOrder())
                                        .filter(rowId -> rowNumbersCounter[rowId] < MAX_ROW_NUMBERS)
                                        .findFirst().orElse(-1);
                                swapWithNumberFromDifferentRow(rowNumbersCounter, rowIdForNumberToBeAssigned, number, columnId);
                            });
        });
    }

    /**
     * Generate Column numbers Lists
     *
     * @param rowNumbersCounter
     * @param fullNumberList
     * @param columnNumberLists
     */
    private void generateColumnNumberLists(int[] rowNumbersCounter, List<Integer> fullNumberList, List<List<Integer>> columnNumberLists) {

        for (int columnId = 0; columnId < TICKET_COLUMNS; columnId++) {
            for (int ticketId = 0; ticketId < STRIP_TICKETS; ticketId++) {
                int randomTicketRowIndex;
                int rowId;
                do {
                    randomTicketRowIndex = random.nextInt(STRIP_ROWS);
                    rowId = ticketId * STRIP_ROWS + randomTicketRowIndex;
                } while (rowNumbersCounter[rowId] >= MAX_ROW_NUMBERS);

                bingoTicket[rowId][columnId] = columnNumberLists.get(columnId).get(0);
                rowNumbersCounter[rowId]++;
                fullNumberList.remove(columnNumberLists.get(columnId).get(0));
                int finalRowId = rowId;
                int finalColId = columnId;
                columnNumberLists.get(columnId).removeIf(number -> number == bingoTicket[finalRowId][finalColId]);
            }
        }
    }

    /**
     * Changes Different Rows with Numbers
     *
     * @param rowNumbersCounter
     * @param rowIdForNumberToBeAssigned
     * @param number
     * @param columnId
     */
    private void swapWithNumberFromDifferentRow(int[] rowNumbersCounter, int rowIdForNumberToBeAssigned, int number, int columnId) {

        AtomicBoolean isNumberAssignedToRow = new AtomicBoolean(false);
        IntStream.rangeClosed(0, rowNumbersCounter.length - 1).boxed()
                .sorted(Collections.reverseOrder())
                .peek(rowId -> IntStream.range(0, TICKET_COLUMNS).boxed()
                        .filter(colId -> rowNumbersCounter[rowId] == MAX_ROW_NUMBERS
                                && bingoTicket[rowId][colId] != 0 && bingoTicket[rowId][columnId] == 0
                                && bingoTicket[rowIdForNumberToBeAssigned][colId] == 0)
                        .findFirst().ifPresent(colId -> {
                            bingoTicket[rowId][columnId] = number;
                            bingoTicket[rowIdForNumberToBeAssigned][colId] = bingoTicket[rowId][colId];
                            bingoTicket[rowId][colId] = 0;
                            rowNumbersCounter[rowIdForNumberToBeAssigned]++;
                            isNumberAssignedToRow.set(true);
                        }))
                .anyMatch(n -> isNumberAssignedToRow.get());
    }

    /**
     * Sorts Ticket Columns
     */
    private void sortTicketColumns() {

        IntStream.range(0, STRIP_TICKETS).boxed()
                .forEach(stripId -> IntStream.range(0, TICKET_COLUMNS).boxed()
                        .forEach(rowId -> {
                            List<Integer> stripColumn = Arrays.asList(bingoTicket[STRIP_ROWS * stripId][rowId],
                                    bingoTicket[STRIP_ROWS * stripId + 1][rowId],
                                    bingoTicket[STRIP_ROWS * stripId + 2][rowId]);
                            stripColumn.sort((o1, o2) -> {
                                if (o1 == 0 || o2 == 0 || o1.equals(o2)) return 0;
                                if (o1 < o2) return -1;
                                return 1;
                            });
                            if (stripColumn.stream().filter(x -> x == 0).count() == 1 && stripColumn.get(1) == 0 &&
                                    stripColumn.get(0) > stripColumn.get(2)) {
                                Collections.swap(stripColumn, 0, 2);
                            }
                            bingoTicket[STRIP_ROWS * stripId][rowId] = stripColumn.get(0);
                            bingoTicket[STRIP_ROWS * stripId + 1][rowId] = stripColumn.get(1);
                            bingoTicket[STRIP_ROWS * stripId + 2][rowId] = stripColumn.get(2);
                        }));
    }

}
