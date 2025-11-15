package com.example.libreria.service;

import com.example.libreria.dto.ReservationRequestDTO;
import com.example.libreria.dto.ReservationResponseDTO;
import com.example.libreria.dto.ReturnBookRequestDTO;
import com.example.libreria.model.Book;
import com.example.libreria.model.Reservation;
import com.example.libreria.model.User;
import com.example.libreria.repository.BookRepository;
import com.example.libreria.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {
    
    @Mock
    private ReservationRepository reservationRepository;
    
    @Mock
    private BookRepository bookRepository;
    
    @Mock
    private BookService bookService;
    
    @Mock
    private UserService userService;
    
    @InjectMocks
    private ReservationService reservationService;
    
    private User testUser;
    private Book testBook;
    private Reservation testReservation;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Juan Pérez");
        testUser.setEmail("juan@example.com");
        
        testBook = new Book();
        testBook.setExternalId(258027L);
        testBook.setTitle("The Lord of the Rings");
        testBook.setPrice(new BigDecimal("15.99"));
        testBook.setStockQuantity(10);
        testBook.setAvailableQuantity(5);
        
        testReservation = new Reservation();
        testReservation.setId(1L);
        testReservation.setUser(testUser);
        testReservation.setBook(testBook);
        testReservation.setRentalDays(7);
        testReservation.setStartDate(LocalDate.now());
        testReservation.setExpectedReturnDate(LocalDate.now().plusDays(7));
        testReservation.setDailyRate(new BigDecimal("15.99"));
        testReservation.setTotalFee(new BigDecimal("111.93"));
        testReservation.setStatus(Reservation.ReservationStatus.ACTIVE);
        testReservation.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateReservation_Success() {
        // TODO: Implementar el test de creación de reserva exitosa


        ReservationRequestDTO requestDTO = new ReservationRequestDTO();
        requestDTO.setUserId(1L);
        requestDTO.setBookExternalId(258027L);
        requestDTO.setRentalDays(7);
        requestDTO.setStartDate(LocalDate.now());


        when(bookRepository.findByExternalId(258027L)).thenReturn(Optional.of(testBook));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);


        ReservationResponseDTO result = reservationService.createReservation(requestDTO);


        assertNotNull(result);
        assertEquals(testUser.getId(), result.getUserId());
        assertEquals(testBook.getExternalId(), result.getBookExternalId());
        assertEquals(7, result.getRentalDays());
        assertEquals(Reservation.ReservationStatus.ACTIVE, result.getStatus());


        verify(bookRepository, times(1)).findByExternalId(258027L);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(bookService, times(1)).updateStock(eq(258027L), anyInt());
    }

    @Test
    void testCreateReservation_BookNotAvailable() {
        // TODO: Implementar el test de creación de reserva cuando el libro no está disponible


        ReservationRequestDTO requestDTO = new ReservationRequestDTO();
        requestDTO.setUserId(1L);
        requestDTO.setBookExternalId(258027L);
        requestDTO.setRentalDays(7);
        requestDTO.setStartDate(LocalDate.now());

        Book unavailableBook = new Book();
        unavailableBook.setExternalId(258027L);
        unavailableBook.setTitle("The Lord of the Rings");
        unavailableBook.setPrice(new BigDecimal("15.99"));
        unavailableBook.setStockQuantity(10);
        unavailableBook.setAvailableQuantity(0);


        when(bookRepository.findByExternalId(258027L)).thenReturn(Optional.of(unavailableBook));


        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reservationService.createReservation(requestDTO);
        });

        assertTrue(exception.getMessage().contains("no está disponible"));

        verify(reservationRepository, never()).save(any(Reservation.class));
        verify(bookService, never()).updateStock(anyLong(), anyInt());
    }

    @Test
    void testReturnBook_OnTime() {
        // TODO: Implementar el test de devolución de libro en tiempo


        Reservation activeReservation = new Reservation();
        activeReservation.setId(1L);
        activeReservation.setUser(testUser);
        activeReservation.setBook(testBook);
        activeReservation.setRentalDays(7);
        activeReservation.setStartDate(LocalDate.now().minusDays(5));
        activeReservation.setExpectedReturnDate(LocalDate.now().plusDays(2));
        activeReservation.setDailyRate(new BigDecimal("15.99"));
        activeReservation.setTotalFee(new BigDecimal("111.93"));
        activeReservation.setLateFee(BigDecimal.ZERO);
        activeReservation.setStatus(Reservation.ReservationStatus.ACTIVE);
        activeReservation.setCreatedAt(LocalDateTime.now());

        ReturnBookRequestDTO returnRequest = new ReturnBookRequestDTO();
        returnRequest.setReturnDate(LocalDate.now());

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(activeReservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(activeReservation);


        ReservationResponseDTO result = reservationService.returnBook(1L, returnRequest);


        assertNotNull(result);
        assertEquals(Reservation.ReservationStatus.RETURNED, result.getStatus());
        assertEquals(0, result.getLateFee().compareTo(BigDecimal.ZERO));
        assertNotNull(result.getActualReturnDate());

        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(bookService, times(1)).updateStock(eq(258027L), anyInt());
    }

    @Test
    void testReturnBook_Overdue() {
        // TODO: Implementar el test de devolución de libro con retraso


        Reservation activeReservation = new Reservation();
        activeReservation.setId(1L);
        activeReservation.setUser(testUser);
        activeReservation.setBook(testBook);
        activeReservation.setRentalDays(7);
        activeReservation.setStartDate(LocalDate.now().minusDays(10));
        activeReservation.setExpectedReturnDate(LocalDate.now().minusDays(3));
        activeReservation.setDailyRate(new BigDecimal("15.99"));
        activeReservation.setTotalFee(new BigDecimal("111.93"));
        activeReservation.setLateFee(BigDecimal.ZERO);
        activeReservation.setStatus(Reservation.ReservationStatus.ACTIVE);
        activeReservation.setCreatedAt(LocalDateTime.now());

        ReturnBookRequestDTO returnRequest = new ReturnBookRequestDTO();
        returnRequest.setReturnDate(LocalDate.now());

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(activeReservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(activeReservation);


        ReservationResponseDTO result = reservationService.returnBook(1L, returnRequest);


        assertNotNull(result);
        assertEquals(Reservation.ReservationStatus.RETURNED, result.getStatus());

        BigDecimal expectedLateFee = new BigDecimal("15.99")
                .multiply(new BigDecimal("0.15"))
                .multiply(new BigDecimal("3"))
                .setScale(2, RoundingMode.HALF_UP);

        assertEquals(expectedLateFee, result.getLateFee());

        BigDecimal expectedTotal = new BigDecimal("111.93").add(expectedLateFee);
        assertEquals(expectedTotal, result.getTotalFee());

        assertNotNull(result.getActualReturnDate());

        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(bookService, times(1)).updateStock(eq(258027L), anyInt());
    }
    
    @Test
    void testGetReservationById_Success() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        
        ReservationResponseDTO result = reservationService.getReservationById(1L);
        
        assertNotNull(result);
        assertEquals(testReservation.getId(), result.getId());
    }
    
    @Test
    void testGetAllReservations() {
        Reservation reservation2 = new Reservation();
        reservation2.setId(2L);
        
        when(reservationRepository.findAll()).thenReturn(Arrays.asList(testReservation, reservation2));
        
        List<ReservationResponseDTO> result = reservationService.getAllReservations();
        
        assertNotNull(result);
        assertEquals(2, result.size());
    }
    
    @Test
    void testGetReservationsByUserId() {
        when(reservationRepository.findByUserId(1L)).thenReturn(Arrays.asList(testReservation));
        
        List<ReservationResponseDTO> result = reservationService.getReservationsByUserId(1L);
        
        assertNotNull(result);
        assertEquals(1, result.size());
    }
    
    @Test
    void testGetActiveReservations() {
        when(reservationRepository.findByStatus(Reservation.ReservationStatus.ACTIVE))
                .thenReturn(Arrays.asList(testReservation));
        
        List<ReservationResponseDTO> result = reservationService.getActiveReservations();
        
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}

