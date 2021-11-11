package com.lindar.bingo90generator;

import com.lindar.bingo90generator.entity.Ticket;
import com.lindar.bingo90generator.service.TicketService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;

@SpringBootApplication
public class Bingo90GeneratorApplication {

    private static final Logger logger = LogManager.getLogger(Bingo90GeneratorApplication.class);

    public static void main(String[] args) {

        ConfigurableApplicationContext context = SpringApplication.run(Bingo90GeneratorApplication.class, args);
        TicketService ticketService = context.getBean(TicketService.class);
        List<Ticket> ticketList = ticketService.createBingoTickets(5);
        ticketList.stream().forEach(ticket -> logger.info(ticket.toString()));
    }

}
