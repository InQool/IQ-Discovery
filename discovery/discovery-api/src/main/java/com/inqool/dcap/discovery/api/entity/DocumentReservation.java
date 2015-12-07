package com.inqool.dcap.discovery.api.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author Lukas Jane (inQool) 17. 8. 2015.
 */
@Getter
@Setter
@Entity
public class DocumentReservation extends DocWithInventoryId {
    @Id
    @GeneratedValue
    @NotNull
    private int id;

    private String userId;

    private LocalDateTime date;

    @Column(columnDefinition="TEXT")
    private String reason;
}
