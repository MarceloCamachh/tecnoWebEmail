package com.example.tecnoWebEmail.Repository;

import com.example.tecnoWebEmail.Models.Supply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplyRepository extends JpaRepository<Supply, Long> {

    // Buscar insumo por su nombre
    Supply findByNombre(String nombre);

    // Encontrar por unidad de medida
    List<Supply> findByUnidadMedida(String unidadMedida);

}
