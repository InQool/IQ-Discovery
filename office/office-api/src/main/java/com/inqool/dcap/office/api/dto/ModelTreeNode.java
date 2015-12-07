package com.inqool.dcap.office.api.dto;

import com.inqool.dcap.integration.model.ZdoModel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to model document tree for indexing documents to solr
 * @author Lukas Jane (inQool) 9. 7. 2015.
 */
@Getter
@Setter
public class ModelTreeNode {
    private ZdoModel model;
    private List<ModelTreeNode> children = new ArrayList<>();
}
