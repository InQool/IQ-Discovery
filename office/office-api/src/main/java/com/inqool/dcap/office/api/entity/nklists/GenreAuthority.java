package com.inqool.dcap.office.api.entity.nklists;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

/**
 * @author Lukas Jane (inQool) 18. 8. 2015.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties("fieldHandler")
@Cacheable
@BatchSize(size = 100)
@Entity
public class GenreAuthority {
    @Id
    /*@GeneratedValue*/
    @NotNull
    private String name;
    private String code;
}
