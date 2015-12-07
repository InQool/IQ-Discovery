package com.inqool.dcap;

import com.inqool.dcap.integration.model.ZdoGroup;
import com.inqool.dcap.integration.model.ZdoType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukas Jane (inQool) 6. 8. 2015.
 */
@Getter
@Setter
public class DebugDTO implements Comparable<DebugDTO>{
    private String invId;
    private String url;
    private String type;
    private String group;
    private List<DebugDTO> children = new ArrayList<>();

    @Override
    public int compareTo(DebugDTO that) {
        String o1t = this.type;
        String o2t = that.type;
        if(o1t == null && o2t == null) return 0;
        if(o1t == null) return -1;
        if(o2t == null) return 1;

        if(o1t.equals(o2t)) {
            String o1s = this.group;
            String o2s = that.group;
            if(o1s == null && o2s == null) return 0;
            if(o1s == null) return -1;
            if(o2s == null) return 1;
            if(o1s.equals(o2s)) {
                return this.url.compareTo(that.url);
            }
            if(o1s.equals(ZdoGroup.KDR.name())) return 1;
            if(o2s.equals(ZdoGroup.KDR.name())) return -1;
            if(o1s.equals(ZdoGroup.ZDO_CONCEPT.name())) return 1;
            if(o2s.equals(ZdoGroup.ZDO_CONCEPT.name())) return -1;
            if(o1s.equals(ZdoGroup.ZDO.name())) return 1;
            if(o2s.equals(ZdoGroup.ZDO.name())) return -1;
            if(o1s.equals(ZdoGroup.UNPUBLISHED.name())) return 1;
            if(o2s.equals(ZdoGroup.UNPUBLISHED.name())) return -1;
            if(o1s.equals(ZdoGroup.DISCARDED.name())) return 1;
            if(o2s.equals(ZdoGroup.DISCARDED.name())) return -1;
            if(o1s.equals(ZdoGroup.BACH.name())) return 1;
            if(o2s.equals(ZdoGroup.BACH.name())) return -1;
            if(o1s.equals(ZdoGroup.DEMUS.name())) return 1;
            if(o2s.equals(ZdoGroup.DEMUS.name())) return -1;
            if(o1s.equals(ZdoGroup.EXTERNAL.name())) return 1;
            if(o2s.equals(ZdoGroup.EXTERNAL.name())) return -1;
        }

        if(o1t.equals(ZdoType.periodical.name())) return 1;
        if(o2t.equals(ZdoType.periodical.name())) return -1;
        if(o1t.equals(ZdoType.monograph.name())) return 1;
        if(o2t.equals(ZdoType.monograph.name())) return -1;

        if(o1t.equals(ZdoType.cho.name())) return 1;
        if(o2t.equals(ZdoType.cho.name())) return -1;

        if(o1t.equals(ZdoType.bornDigital.name())) return 1;
        if(o2t.equals(ZdoType.bornDigital.name())) return -1;

        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DebugDTO debugDTO = (DebugDTO) o;

        if (!children.equals(debugDTO.children)) return false;
        if (group != null ? !group.equals(debugDTO.group) : debugDTO.group != null) return false;
        if (invId != null ? !invId.equals(debugDTO.invId) : debugDTO.invId != null) return false;
        if (!type.equals(debugDTO.type)) return false;
        if (!url.equals(debugDTO.url)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = invId != null ? invId.hashCode() : 0;
        result = 31 * result + url.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + (group != null ? group.hashCode() : 0);
        result = 31 * result + children.hashCode();
        return result;
    }
}
