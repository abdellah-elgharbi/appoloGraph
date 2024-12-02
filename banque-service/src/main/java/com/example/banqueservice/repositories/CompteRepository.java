package com.example.banqueservice.repositories;

import com.example.banqueservice.entities.Compte;
import com.example.banqueservice.entities.TypeCompte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CompteRepository extends JpaRepository<Compte, Long> {

    @Query("SELECT COALESCE(SUM(c.solde), 0) FROM Compte c")
    double sumSoldes();

    List<Compte> findByType(TypeCompte type);

}
