package ru.yandex.practicum.delivery.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.delivery.enums.DeliveryState;

import java.util.UUID;

@Entity
@Table(name = "deliveries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Embedded
    @AttributeOverrides({
    @AttributeOverride(name = "country", column = @Column(name = "from_country")),
    @AttributeOverride(name = "city", column = @Column(name = "from_city")),
    @AttributeOverride(name = "street", column = @Column(name = "from_street")),
    @AttributeOverride(name = "house", column = @Column(name = "from_house")),
    @AttributeOverride(name = "flat", column = @Column(name = "from_flat"))
    })
    private Address fromAddress;

    @Embedded
    @AttributeOverrides({
    @AttributeOverride(name = "country", column = @Column(name = "to_country")),
    @AttributeOverride(name = "city", column = @Column(name = "to_city")),
    @AttributeOverride(name = "street", column = @Column(name = "to_street")),
    @AttributeOverride(name = "house", column = @Column(name = "to_house")),
    @AttributeOverride(name = "flat", column = @Column(name = "to_flat"))
    })
    private Address toAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private DeliveryState state;
}