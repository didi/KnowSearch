package com.didichuxing.datachannel.arius.admin.remote.monitor.odin.bean;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
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
