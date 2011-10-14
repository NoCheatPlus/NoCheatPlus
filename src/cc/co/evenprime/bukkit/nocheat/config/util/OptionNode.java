package cc.co.evenprime.bukkit.nocheat.config.util;

import java.util.LinkedList;
import java.util.List;

public class OptionNode {

    public enum DataType {
        PARENT, STRING, BOOLEAN, INTEGER, LOGLEVEL, ACTIONLIST
    };

    private final String           name;
    private final List<OptionNode> children;
    private final OptionNode       parent;
    private final DataType         type;

    public OptionNode(String name, OptionNode parent, DataType type) {
        this.name = name;
        if(type == DataType.PARENT) {
            this.children = new LinkedList<OptionNode>();
        } else {
            this.children = null;
        }

        if(parent != null) {
            parent.addChild(this);
        }

        this.parent = parent;
        this.type = type;
    }

    private void addChild(OptionNode node) {
        if(this.type != DataType.PARENT) {
            throw new IllegalArgumentException("Can't a child to a leaf node.");
        }

        this.children.add(node);
    }

    public boolean isLeaf() {
        return this.type != DataType.PARENT;
    }

    public List<OptionNode> getChildren() {
        return this.children;
    }

    public String getName() {
        return name;
    }

    public OptionNode getParent() {
        return parent;
    }

    public DataType getType() {
        return type;
    }
}
