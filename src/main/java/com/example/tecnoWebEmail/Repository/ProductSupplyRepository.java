package com.example.tecnoWebEmail.Repository;

import com.example.tecnoWebEmail.Models.ProductSupply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductSupplyRepository extends JpaRepository<ProductSupply, Long> {

    // Lista todas las relaciones de un producto, con product y supply inicializados
    @Query("""
           select ps
           from ProductSupply ps
           join fetch ps.supply s
           join fetch ps.product p
           where p.id = :productId
           """)
    List<ProductSupply> findAllByProductIdFetchAll(@Param("productId") Long productId);

    // Trae UNA relación específica (productId + supplyId), con fetch
    @Query("""
           select ps
           from ProductSupply ps
           join fetch ps.supply s
           join fetch ps.product p
           where p.id = :productId and s.id = :supplyId
           """)
    Optional<ProductSupply> findByProductIdAndSupplyIdFetch(@Param("productId") Long productId,
                                                            @Param("supplyId") Long supplyId);

    // Borra por IDs (más directo y eficiente)
    @Modifying
    @Query("delete from ProductSupply ps where ps.product.id = :productId and ps.supply.id = :supplyId")
    int deleteByProductIdAndSupplyId(@Param("productId") Long productId,
                                     @Param("supplyId") Long supplyId);

    // Lista todos los productos que usan un insumo, con fetch para evitar LAZY
    @Query("""
           select ps
           from ProductSupply ps
           join fetch ps.product p
           join fetch ps.supply s
           where s.id = :supplyId
           """)
    List<ProductSupply> findAllBySupplyIdFetchAll(@Param("supplyId") Long supplyId);
}
