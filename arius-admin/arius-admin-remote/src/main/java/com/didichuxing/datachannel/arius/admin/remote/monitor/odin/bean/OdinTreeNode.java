package com.didichuxing.datachannel.arius.admin.remote.monitor.odin.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OdinTreeNode implements Serializable {

    private Integer            Id;

    private String             name;

    private String             ns;

    private Integer            depth;

    private String             iconSkin;

    private boolean            isParent;

    private String             category;

    private boolean            open;

    private List<OdinTreeNode> children;

}
