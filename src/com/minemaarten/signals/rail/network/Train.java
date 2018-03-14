package com.minemaarten.signals.rail.network;

import java.util.Set;

public abstract class Train<TPos> {

    /**
     * The positions the train is on.
     * This may be a single position for a cart, or multiple if actually a train.
     * @return
     */
    public abstract Set<TPos> getPositions();
}