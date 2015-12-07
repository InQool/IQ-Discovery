package com.inqool.dcap.integration.desa2.preprocess;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * Created by Lukess on 7. 11. 2014.
 */
@Entity
@Table
public class Entry {
//    @Id
//    @GeneratedValue
//    @NotNull
//    @Column
//    private int id=0;

    @Id
    @NotNull
    @Column
    private String inventoryNumber;

    @Column
    private String path;

    public String getInventoryNumber() {
        return inventoryNumber;
    }

    public void setInventoryNumber(String inventoryNumber) {
        this.inventoryNumber = inventoryNumber;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
