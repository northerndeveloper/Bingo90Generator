package com.lindar.bingo90generator;

import com.lindar.bingo90generator.entity.Ticket;
import com.lindar.bingo90generator.service.TicketService;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Bingo90GeneratorApplicationTests {

    private static Ticket singleBingoStrip;
    private static Map<Integer, List<int[]>> tickets;
    private static final int EXPECTED_TOTAL_NUMBERS = 90;
    private static final int EXPECTED_TOTAL_NUMBERS_PER_ROW = 5;
    private static final int EXPECTED_TOTAL_NUMBERS_PER_TICKET = 15;
    private static final int EXPECTED_NUMBER_OF_TICKETS = 6;
    private static final int EXPECTED_ROWS_PER_TICKET = 3;


    @BeforeClass
    public static void generateBingoTicket() {

        ConfigurableApplicationContext context = SpringApplication.run(Bingo90GeneratorApplication.class);
        TicketService ticketService = context.getBean(TicketService.class);
        ticketService.createBingoTickets(2);

        singleBingoStrip = ticketService.createSingleBingoTicket();
        AtomicInteger index = new AtomicInteger(0);
        tickets = Arrays.stream(singleBingoStrip.getBingoStrip())
                .collect(Collectors.groupingBy(row -> List.of(0,1,2,3,4,5).get(index.getAndIncrement() / 3)));
    }

    @Test
    public void ticketHasNumbers1to90() {
        List<Integer> bingoStripNumbers = Arrays.stream(singleBingoStrip.getBingoStrip())
                .flatMapToInt(Arrays::stream)
                .filter(n -> n > 0)
                .sorted()
                .boxed()
                .collect(Collectors.toList());

        assertEquals(EXPECTED_TOTAL_NUMBERS, bingoStripNumbers.size());
        assertEquals(IntStream.rangeClosed(1, EXPECTED_TOTAL_NUMBERS).boxed().collect(Collectors.toList()), bingoStripNumbers);
    }

    @Test
    public void rowConsistsOf5Numbers() {
        List<List<Integer>> rowNumbers = Arrays.stream(singleBingoStrip.getBingoStrip())
                .map(arr -> Arrays.stream(arr).filter(x -> x > 0).boxed().collect(Collectors.toList()))
                .collect(Collectors.toList());

        rowNumbers.forEach(row -> assertEquals(EXPECTED_TOTAL_NUMBERS_PER_ROW, row.size()));
    }

    @Test
    public void ticketConsistsOf6StripsWith3Rows() {
        assertEquals(EXPECTED_NUMBER_OF_TICKETS, tickets.entrySet().size());
        tickets.forEach((ticketId, ticketRows) -> assertEquals(EXPECTED_ROWS_PER_TICKET, ticketRows.size()));
    }

    @Test
    public void stripConsistOf15Numbers() {
        tickets.forEach((ticketId, ticket) -> {
            List<Integer> ticketNumbers = ticket
                    .stream()
                    .flatMapToInt(Arrays::stream)
                    .boxed()
                    .filter(x -> x > 0)
                    .collect(Collectors.toList());
            assertEquals(EXPECTED_TOTAL_NUMBERS_PER_TICKET, ticketNumbers.size());
        });
    }

}
