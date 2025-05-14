package net.ipip.ipdb;

import java.util.Map;

/**
 * @copyright IPIP.net
 */
public class MetaData {
    public int Build;
    public int IPVersion;
    public int nodeCount;
    public Map<String, Integer> Languages;
    public String[] Fields;
    public int totalSize;

    public int getBuild() {
        return Build;
    }

    public void setBuild(int build) {
        Build = build;
    }

    public int getIPVersion() {
        return IPVersion;
    }

    public void setIPVersion(int IPVersion) {
        this.IPVersion = IPVersion;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
    }

    public Map<String, Integer> getLanguages() {
        return Languages;
    }

    public void setLanguages(Map<String, Integer> languages) {
        Languages = languages;
    }

    public String[] getFields() {
        return Fields;
    }

    public void setFields(String[] fields) {
        Fields = fields;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }
}