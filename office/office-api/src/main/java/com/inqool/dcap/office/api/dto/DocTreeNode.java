package com.inqool.dcap.office.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukas Jane (inQool) 19. 3. 2015.
 */
@Getter
@Setter
@NoArgsConstructor
public class DocTreeNode {
    private String title;
    private String id;
    private Boolean validToPublish;
    private List<DocTreeNode> children = new ArrayList<>();

    public DocTreeNode(String title, String id) {
        this.title = title;
        this.id = id;
    }
}
