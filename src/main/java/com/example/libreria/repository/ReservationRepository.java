package com.example.libreria.repository;

import com.example.libreria.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;


@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
   List<Reservation> findByUserId(Long userId);

   List<Reservation> findByStatus(Reservation.ReservationStatus status);

  // @Query ("SELECT r FROM Reservation  r WHERE  r.expectedReturnDate < :today  AND r.status = 'ACTIVE'")
    //List<Reservation> findOverdueReservations(@Param("today") LocalDate today); tengo miendo que esta falle pero opcion b por si no anda

    @Query ("SELECT r FROM Reservation  r WHERE  r.expectedReturnDate < CURRENT_DATE AND r.status = 'ACTIVE'")
    List<Reservation> findOverdueReservations();

    // TODO: Implementar los métodos de la reserva
}

