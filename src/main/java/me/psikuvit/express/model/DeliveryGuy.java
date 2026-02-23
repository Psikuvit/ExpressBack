package me.psikuvit.express.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "delivery_guys")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryGuy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false)
    private String car;

    @Embedded
    private Location location;

    @Column(nullable = false)
    private Boolean available = true;
}

