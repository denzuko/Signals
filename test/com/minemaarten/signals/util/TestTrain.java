package com.minemaarten.signals.util;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;

import com.minemaarten.signals.rail.network.EnumHeading;
import com.minemaarten.signals.rail.network.RailRoute;
import com.minemaarten.signals.rail.network.RailRoute.RailRouteNode;
import com.minemaarten.signals.rail.network.RailSection;
import com.minemaarten.signals.rail.network.Train;

public class TestTrain extends Train<Pos2D>{

    private final char trainID;

    public TestTrain(Set<Pos2D> positions, char trainID){
        setPositions(positions);
        this.trainID = trainID;
    }

    public void setPosition(Pos2D pos){
        setPositions(Collections.singleton(pos));
    }

    public void setClaimingSection(RailSection<Pos2D> section){
        claimedSections = Collections.singleton(section);
    }

    @Override
    public boolean equals(Object other){
        return other instanceof TestTrain && ((TestTrain)other).trainID == trainID;
    }

    @Override
    public int hashCode(){
        return trainID;
    }

    @Override
    public RailRoute<Pos2D> pathfind(Pos2D start, EnumHeading dir){
        throw new NotImplementedException("");
    }

    @Override
    protected void updateIntersection(RailRouteNode<Pos2D> rail){

    }
}
