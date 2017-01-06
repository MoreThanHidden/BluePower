package com.bluepowermod.api.gate;

import com.bluepowermod.api.misc.IFace;
import uk.co.qmunity.lib.vec.IWorldLocation;

import java.util.Collection;

public interface IGate extends IWorldLocation, IFace {

    public Collection<IGateComponent> getComponents();

    public void addComponent(IGateComponent component);

    public IGateConnection bottom();

    public IGateConnection top();

    public IGateConnection left();

    public IGateConnection right();

    public IGateConnection front();

    public IGateConnection back();

    public IGateLogic<?> logic();

}
