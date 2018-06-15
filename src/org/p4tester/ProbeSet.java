package org.p4tester;

public abstract class ProbeSet {
    protected int match;
    ProbeSet(int match) {
        this.match = match;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof ProbeSet) {
            ProbeSet probeSet = (ProbeSet) obj;
            return probeSet.getMatch() == this.getMatch();
        }
        return false;
    }

    public int getMatch() {
        return match;
    }

    @Override
    public int hashCode() {
        return this.getMatch();
    }
}
