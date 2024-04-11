package com.example.bookmyshow.services;

import com.example.bookmyshow.exceptions.ShowSeatNotAvailableException;
import com.example.bookmyshow.models.ShowSeat;
import com.example.bookmyshow.models.ShowSeatState;
import com.example.bookmyshow.models.Ticket;
import com.example.bookmyshow.models.TicketStatus;
import com.example.bookmyshow.repositories.ShowRepository;
import com.example.bookmyshow.repositories.ShowSeatRepository;
import com.example.bookmyshow.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class TicketService {
    private ShowSeatRepository showSeatRepository;
    private UserRepository userRepository;
    private ShowRepository showRepository;

    @Autowired
    public TicketService(
            ShowSeatRepository showSeatRepository,
            UserRepository userRepository,
            ShowRepository showRepository
    ) {
        this.showSeatRepository = showSeatRepository;
        this.userRepository = userRepository;
        this.showRepository = showRepository;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Ticket bookTicket(
            Long showId,
            List<Long> showSeatIds,
            Long userId
    ) throws ShowSeatNotAvailableException {
        // Fetch given ShowSeats
        List<ShowSeat> showSeats = showSeatRepository.findByIdIn(showSeatIds);

        // Check if each of them are available
        for (ShowSeat showSeat: showSeats) {
            if (showSeat.getState() != ShowSeatState.AVAILABLE) {
                throw new ShowSeatNotAvailableException("ShowSeat ID: " +
                        showSeat.getId() + " not available.");
            }
        }

        // Update status to locked
        for (ShowSeat showSeat: showSeats) {
            showSeat.setState(ShowSeatState.LOCKED);
            showSeatRepository.save(showSeat);
        }

        // Return the Ticket object
        Ticket ticket = new Ticket();
        ticket.setTicketStatus(TicketStatus.PENDING);
        ticket.setBookedBy(userRepository.findById(userId).get());
        ticket.setShow(showRepository.findById(showId).get());
        ticket.setShowSeats(showSeats);
        ticket.setTimeOfBooking(new Date());


        return ticket;
    }
}
