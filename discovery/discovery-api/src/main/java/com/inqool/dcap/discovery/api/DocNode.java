package com.inqool.dcap.discovery.api;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukas Jane (inQool) 28. 5. 2015.
 */
@Getter
@Setter
public class DocNode implements Comparable<DocNode> {
    private String id;
    private String title;
    private String inventoryId;
    private String created;
    private List<DocNode> children = new ArrayList<>();

    public DocNode(String id, String title, String inventoryId) {
        this(id, title, inventoryId, null);
    }
    public DocNode(String id, String title, String inventoryId, String created) {
        this.id = id;
        this.title = title;
        this.inventoryId = inventoryId;
        this.created = created;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DocNode docNode = (DocNode) o;

        if (children != null ? !children.equals(docNode.children) : docNode.children != null) return false;
        if (!id.equals(docNode.id)) return false;
        if (!title.equals(docNode.title)) return false;
        if (!inventoryId.equals(docNode.inventoryId)) return false;
        if (!created.equals(docNode.created)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + inventoryId.hashCode();
        result = 31 * result + created.hashCode();
        result = 31 * result + (children != null ? children.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(DocNode o) {
        return title.compareTo(o.title);
    }
}
