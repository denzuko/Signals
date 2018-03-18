package com.minemaarten.signals.rail.network.mc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import net.minecraft.block.BlockRailBase.EnumRailDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import com.minemaarten.signals.api.IRail;
import com.minemaarten.signals.capabilities.CapabilityMinecartDestination;
import com.minemaarten.signals.lib.Log;
import com.minemaarten.signals.network.NetworkHandler;
import com.minemaarten.signals.network.PacketUpdateMessage;
import com.minemaarten.signals.rail.RailManager;
import com.minemaarten.signals.rail.network.EnumHeading;
import com.minemaarten.signals.rail.network.RailRoute;
import com.minemaarten.signals.rail.network.RailRoute.RailRouteNode;
import com.minemaarten.signals.rail.network.Train;

public class MCTrain extends Train<MCPos>{

    private static Map<EnumSet<EnumHeading>, EnumRailDirection> DIRS_TO_RAIL_DIR = new HashMap<>(6);

    static {
        DIRS_TO_RAIL_DIR.put(EnumSet.of(EnumHeading.NORTH, EnumHeading.SOUTH), EnumRailDirection.NORTH_SOUTH);
        DIRS_TO_RAIL_DIR.put(EnumSet.of(EnumHeading.EAST, EnumHeading.WEST), EnumRailDirection.EAST_WEST);
        DIRS_TO_RAIL_DIR.put(EnumSet.of(EnumHeading.NORTH, EnumHeading.EAST), EnumRailDirection.NORTH_EAST);
        DIRS_TO_RAIL_DIR.put(EnumSet.of(EnumHeading.EAST, EnumHeading.SOUTH), EnumRailDirection.SOUTH_EAST);
        DIRS_TO_RAIL_DIR.put(EnumSet.of(EnumHeading.SOUTH, EnumHeading.WEST), EnumRailDirection.SOUTH_WEST);
        DIRS_TO_RAIL_DIR.put(EnumSet.of(EnumHeading.WEST, EnumHeading.NORTH), EnumRailDirection.NORTH_WEST);
    }

    private final Set<UUID> cartIDs;

    public MCTrain(List<EntityMinecart> carts){
        cartIDs = carts.stream().map(c -> c.getUniqueID()).collect(Collectors.toSet());
    }

    private List<EntityMinecart> getCarts(){
        //TODO cache
        return Arrays.stream(DimensionManager.getWorlds()).flatMap(w -> w.loadedEntityList.stream().filter(e -> e instanceof EntityMinecart && cartIDs.contains(e.getUniqueID()))).map(e -> (EntityMinecart)e).collect(Collectors.toList());
    }

    public void updatePositions(){
        Set<MCPos> positions = getCarts().stream().map(c -> new MCPos(c.world, c.getPosition())).collect(Collectors.toSet());
        if(!positions.isEmpty()) { //Update if any cart is loaded, currently.
            setPositions(RailNetworkManager.getInstance().getNetwork(), positions);
        }
    }

    @Override
    public RailRoute<MCPos> pathfind(MCPos start, EnumHeading dir){
        RailRoute<MCPos> path = null;
        for(EntityMinecart cart : getCarts()) {
            CapabilityMinecartDestination capability = cart.getCapability(CapabilityMinecartDestination.INSTANCE, null);
            String destination = capability.getCurrentDestination();
            Pattern destinationRegex = capability.getCurrentDestinationRegex();
            List<PacketUpdateMessage> messages = new ArrayList<>();
            if(!destination.isEmpty()) {
                //TODO messages.add(new PacketUpdateMessage(this, cart, "signals.message.routing_cart", destination));

                path = RailNetworkManager.getInstance().pathfind(start, cart, destinationRegex, dir);
                if(path == null) { //If there's no path
                    //        messages.add(new PacketUpdateMessage(this, cart, "signals.message.no_path_found"));
                } else {
                    //       messages.add(new PacketUpdateMessage(this, cart, "signals.message.path_found"));
                    break;
                }
            } else {
                //      messages.add(new PacketUpdateMessage(this, cart, "signals.message.no_destination"));
            }

            /*  if(submitMessages) {
                  for(PacketUpdateMessage message : messages) {
                      NetworkHandler.sendToAllAround(message, getWorld());
                  }
              }*/
            // capability.setPath(cart, path);

        }
        return path;
    }

    @Override
    protected void updateIntersection(RailRouteNode<MCPos> routeNode){
        World world = routeNode.pos.getWorld();
        BlockPos pos = routeNode.pos.getPos();
        IBlockState state = world.getBlockState(pos);
        IRail rail = RailManager.getInstance().getRail(world, pos, state);

        List<PacketUpdateMessage> messages = new ArrayList<>();
        EnumRailDirection requiredDir = DIRS_TO_RAIL_DIR.get(EnumSet.of(routeNode.dirIn, routeNode.dirOut));
        if(requiredDir != null) {
            if(rail.getValidDirections(world, pos, state).contains(requiredDir)) {
                rail.setDirection(world, pos, state, requiredDir);
                String[] args = {Integer.toString(pos.getX()), Integer.toString(pos.getY()), Integer.toString(pos.getZ()), "signals.dir." + routeNode.dirIn.toString().toLowerCase(), "signals.dir." + routeNode.dirOut.toString().toLowerCase()};
                //TODO  messages.add(new PacketUpdateMessage(this, cart, "signals.message.changing_junction", args));
            } else {
                Log.warning("Rail with state " + state + " does not allow setting dir " + requiredDir);
            }
        } else {
            Log.warning("Invalid routing node: " + routeNode); //TODO rail links?
        }

        for(PacketUpdateMessage message : messages) {
            NetworkHandler.sendToAllAround(message, world);
        }
    }

    @Override
    public boolean equals(Object obj){
        return obj instanceof MCTrain && ((MCTrain)obj).cartIDs.equals(cartIDs);
    }

    @Override
    public int hashCode(){
        return cartIDs.hashCode();
    }
}