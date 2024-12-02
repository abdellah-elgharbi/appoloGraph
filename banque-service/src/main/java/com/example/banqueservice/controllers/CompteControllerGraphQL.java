package com.example.banqueservice.controllers;


import com.example.banqueservice.entities.Compte;
import com.example.banqueservice.entities.TypeCompte;
import com.example.banqueservice.repositories.CompteRepository;
import lombok.AllArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
@AllArgsConstructor
public class CompteControllerGraphQL {
    private CompteRepository compteRepository;

    @QueryMapping
    public List<Compte> allComptes() {
        return compteRepository.findAll();
    }

    @QueryMapping
    public Compte compteById(@Argument Long id) {
        Compte compte = compteRepository.findById(id).orElse(null);
        if (compte == null) throw new RuntimeException(String.format("Compte %s not found", id));
        else return compte;
    }

    @MutationMapping
    public Compte saveCompte(@Argument Compte compte) {
        return compteRepository.save(compte);
    }

    @QueryMapping
    public Map<String, Object> totalSolde() {
        long count = compteRepository.count();
        double sum = compteRepository.sumSoldes();
        double average = count > 0 ? sum / count : 0;

        return Map.of(
                "count", count,
                "sum", sum,
                "average", average
        );
    }


    @QueryMapping
    public List<Compte> findByType(@Argument TypeCompte type) {
        return compteRepository.findByType(type);
    }

    // Delete compte by ID
    @MutationMapping
    public String deleteCompte(@Argument Long id) {
        if (compteRepository.existsById(id)) {
            compteRepository.deleteById(id);
            return "success";
        } else {
            throw new RuntimeException("Compte not found");
        }
    }


}