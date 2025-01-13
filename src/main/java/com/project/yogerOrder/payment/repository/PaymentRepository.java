package com.project.yogerOrder.payment.repository;

import com.project.yogerOrder.payment.dto.response.PaymentOrderDTO;
import com.project.yogerOrder.payment.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    boolean existsByPgPaymentId(String pgPaymentId);

    Optional<PaymentEntity> findByOrderId(Long orderId);

    //*
    //@Query("SELECT p, o FROM PaymentEntity p WHERE p.orderId IN (SELECT o.id FROM OrderEntity o WHERE o.productId = :productId)")
    @Query("SELECT new com.project.yogerOrder.payment.dto.response.PaymentOrderDTO(p, o) FROM PaymentEntity p JOIN OrderEntity o ON p.orderId = o.id WHERE o.productId = :productId")
    List<PaymentOrderDTO> findAllPaymentAndOrderByProductId(@Param("productId") Long productId);
    //*/
}
