package com.minemaarten.signals.tileentity;

import net.minecraft.entity.item.EntityMinecart;

import com.minemaarten.signals.rail.network.NetworkSignal.EnumSignalType;

public class TileEntityPathSignal extends TileEntitySignalBase{

    private int pathingTimer;

    @Override
    public void update(){
        super.update();
        if(!world.isRemote && pathingTimer-- <= 0 && getForceMode() == EnumForceMode.NONE) {
            route();
        }
    }

    public void route(){
        pathingTimer = 20;
        /* RailWrapper neighborRail = getConnectedRail();
         if(neighborRail != null) {
             List<EntityMinecart> routingMinecarts = getNeighborMinecarts();
             if(!routingMinecarts.isEmpty()) {
                 Set<RailWrapper> rails = getRailsToNextBlockSection(neighborRail, getFacing());
                 List<EntityMinecart> cartsOnNextBlock = getMinecarts(world, rails);

                 //Fall back onto default Block Signal behaviour if there are any carts to be routed without a path. 
                 //This also will fill the routed carts with a path for the next stage.
                 for(EntityMinecart routingCart : routingMinecarts) {
                     if(routeCart(routingCart, getFacing(), false) == null) {
                         setLampStatus(cartsOnNextBlock.isEmpty() ? EnumLampStatus.GREEN : EnumLampStatus.RED);
                         setMessage("signals.signal_message.cart_without_destination");
                         Log.debug("[Path Signal] Cart routed without destination. Block signal behaviour.");
                         return;
                     }
                 }

                 //Don't allow the cart to proceed if there are carts on the block without a path.
                 for(EntityMinecart cartOnNextBlock : cartsOnNextBlock) {
                     if(getStoredPath(cartOnNextBlock) == null) {
                         setLampStatus(EnumLampStatus.RED);
                         BlockPos pos = cartOnNextBlock.getPosition();
                         setMessage("signals.signal_message.cart_on_track_without_destination", pos.getX(), pos.getY(), pos.getZ());
                         Log.debug("[Path Signal] Cart on rails without destination. Red signal. Cart: " + cartOnNextBlock.getPosition());
                         return;
                     }
                 }

                 //The positions of the track that should be kept free of carts for the carts to be allowed to continue.
                 Set<BlockPos> claimingPositions = new HashSet<BlockPos>();
                 for(EntityMinecart routingCart : routingMinecarts) {
                     claimingPositions.addAll(getToBeTraversedCoordinates(routingCart));
                 }

                 for(EntityMinecart cartOnNextBlock : cartsOnNextBlock) {
                     List<BlockPos> list = getToBeTraversedCoordinates(cartOnNextBlock);
                     for(BlockPos pos : list) {
                         if(claimingPositions.contains(pos)) {
                             setLampStatus(EnumLampStatus.RED);
                             BlockPos p = cartOnNextBlock.getPosition();
                             setMessage("signals.signal_message.cart_intersecting_path", p.getX(), p.getY(), p.getZ());
                             Log.debug("[Path Signal] Cart on rails intersecting the path of the routed cart. Red signal. Cart: " + cartOnNextBlock.getPosition());
                             return;
                         }
                     }
                 }
                 setLampStatus(EnumLampStatus.GREEN);

                 AStarRailNode path = getStoredPath(routingMinecarts.get(0));
                 if(path != null) updateSwitches(path, routingMinecarts.get(0), true);
             } else {
                 setMessage("");
                 // setMessage("signals.signal_message.standby");
                 setLampStatus(EnumLampStatus.YELLOW);
             }
         } else {
             setLampStatus(EnumLampStatus.YELLOW_BLINKING);
         }*/
    }

    @Override
    protected void onCartEnteringBlock(EntityMinecart cart){
        route();
    }

    @Override
    public EnumSignalType getSignalType(){
        return EnumSignalType.BLOCK; //throw new NotImplementedException("Path signals are not supported!");
    }

}
