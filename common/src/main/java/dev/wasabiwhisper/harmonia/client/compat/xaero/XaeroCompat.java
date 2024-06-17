package dev.wasabiwhisper.harmonia.client.compat.xaero;

import dev.wasabiwhisper.harmonia.client.ClientClaims;
import earth.terrarium.cadmus.common.claims.ClaimType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import xaero.map.MapProcessor;
import xaero.map.WorldMapSession;
import xaero.map.gui.GuiMap;
import xaero.map.gui.MapTileSelection;
import xaero.map.gui.dropdown.rightclick.RightClickOption;
import xaero.map.region.MapRegion;
import xaero.map.world.MapDimension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class XaeroCompat {
    private static final String LISTENER_ID = "xaeros";
    private static final XaeroClaimsManager manager = new XaeroClaimsManager();

    public XaeroCompat() {
    }

    public static void registerListener(ResourceKey<Level> dimension) {
        ClientClaims.get(dimension).addListener(LISTENER_ID, claims -> update(dimension, claims));
    }

    public static void removeListener(ResourceKey<Level> dimension) {
        ClientClaims.get(dimension).removeListener(LISTENER_ID);
        manager.clear(dimension);
        MapDimension mapDim = getMapDimension(dimension);
        if (mapDim != null) {
            mapDim.getHighlightHandler().clearCachedHashes();
        }
    }

    private static void update(ResourceKey<Level> dimension, Map<ChunkPos, ClientClaims.Entry> claims) {
        if (!Minecraft.getInstance().isSameThread()) return;
        manager.put(dimension, claims);
        MapProcessor mapProc = getMapProcessor();
        MapDimension mapDim = getMapDimension(dimension);
        if (mapDim != null) {
            int caveLayer = mapProc.getCurrentCaveLayer();
            Map<Integer, Integer> regions = getRegions(claims);
            for (var region : regions.entrySet()) {
                for (int regionOffsetX = -1; regionOffsetX < 2; ++regionOffsetX) {
                    for (int regionOffsetZ = -1; regionOffsetZ < 2; ++regionOffsetZ) {
                        if (regionOffsetX == 0 && regionOffsetZ == 0 || regionOffsetX * regionOffsetX != regionOffsetZ * regionOffsetZ) {
                            mapDim.getHighlightHandler().clearCachedHash(region.getKey() + regionOffsetX, region.getValue() + regionOffsetZ);
                            MapRegion mapRegion = mapDim.getLayeredMapRegions().getLeaf(caveLayer, region.getKey() + regionOffsetX, region.getValue() + regionOffsetZ);
                            if (mapRegion != null) {
                                synchronized(mapRegion) {
                                    if (mapRegion.canRequestReload_unsynced()) {
                                        if (mapRegion.getLoadState() == 2) {
                                            mapRegion.requestRefresh(mapProc);
                                        } else {
                                            mapProc.getMapSaveLoad().requestLoad(mapRegion, "Gui");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static MapProcessor getMapProcessor() {
        WorldMapSession session = WorldMapSession.getCurrentSession();
        return session.getMapProcessor();
    }

    private static MapDimension getMapDimension(ResourceKey<Level> dimension) {
        return getMapProcessor().getMapWorld().getDimension(dimension);
    }

    @NotNull
    private static Map<Integer, Integer> getRegions(Map<ChunkPos, ClientClaims.Entry> claims) {
        Map<Integer, Integer> regions = new HashMap<>();
        for (var chunk : claims.keySet()) {
            int regionX = chunk.getRegionX();
            int regionZ = chunk.getRegionZ();
            if (regions.containsKey(regionX) && regions.get(regionX).equals(regionZ)) {
                continue;
            } else {
                regions.put(regionX, regionZ);
            }
        }
        return regions;
    }

    public static XaeroHighlighter registerHighlighter() {
        return new XaeroHighlighter(manager);
    }

    public void addRightClickOptions(GuiMap screen, ArrayList<RightClickOption> options, MapTileSelection mapTileSelection, MapProcessor mapProcessor) {
        if (mapTileSelection != null) {
            if (mapProcessor.getMapWorld().isUsingCustomDimension()) {
                options.add(new RightClickOption("gui.xaero_cadmus_claim_selection_out_of_dimension", options.size(), screen) {
                    public void onAction(Screen screen) {
                    }
                });
                return;
            }

            boolean hasUnclaimed = false;
            boolean hasClaimed = false;
            boolean hasUnchunkloaded = false;
            boolean hasChunkloaded = false;

            Minecraft mc = Minecraft.getInstance();
            ResourceKey<Level> dimension = mc.level.dimension();

            boolean hasUnclaimPermission = false;

            int left = mapTileSelection.getLeft();
            int top = mapTileSelection.getTop();
            int right = mapTileSelection.getRight();
            int bottom = mapTileSelection.getBottom();
            int checkLeft = left;
            int checkTop = top;
            int checkRight = right;
            int checkBottom = bottom;


            int maxRequestLength = 32;
            if (checkRight - checkLeft >= maxRequestLength) {
                checkRight = checkLeft + maxRequestLength - 1;
            }

            if (checkBottom - checkTop >= maxRequestLength) {
                checkBottom = checkTop + maxRequestLength - 1;
            }

            for (int x = checkLeft; x <= checkRight; ++x) {
                for (int z = checkTop; z <= checkBottom; ++z) {
                    ClientClaims.Entry claim = manager.get(dimension, x, z);
                    hasUnclaimed = claim == null || hasUnclaimed;
                    if (claim != null) {
                        if (mc.player != null) {
                            hasUnclaimPermission = mc.player.hasPermissions(2) || hasUnclaimPermission;
                        }
                        boolean isClaimedByPlayer = claim.teamId().equals(ClientClaims.ID);
                        hasClaimed = isClaimedByPlayer || hasClaimed;
                        hasUnchunkloaded = (isClaimedByPlayer && claim.type() == ClaimType.CLAIMED) || hasUnchunkloaded;
                        hasChunkloaded = (isClaimedByPlayer && claim.type() == ClaimType.CHUNK_LOADED) || hasChunkloaded;
                    }
                }
                if (hasUnclaimed && hasClaimed && hasUnchunkloaded && hasChunkloaded) {
                    break;
                }
            }

            if (right > checkRight) {
                right = checkRight + 1;
            }

            if (bottom > checkBottom) {
                bottom = checkBottom + 1;
            }

            if (hasUnclaimed) {
                int _right = right;
                int _bottom = bottom;
                options.add(new RightClickOption("gui.harmonia.claim_chunks", options.size(), screen) {
                    @Override
                    public void onAction(Screen screen) {
                        manager.changeSelection(dimension, left, top, _right, _bottom, true, ClaimType.CLAIMED, false);
                    }
                });
            }
            if (hasClaimed) {
                int _right = right;
                int _bottom = bottom;
                options.add(new RightClickOption("gui.harmonia.unclaim_chunks", options.size(), screen) {
                    @Override
                    public void onAction(Screen screen) {
                        manager.changeSelection(dimension, left, top, _right, _bottom, false, null, false);
                    }
                });
            }
            if (hasUnclaimPermission) {
                int _right = right;
                int _bottom = bottom;
                boolean _hasUnclaimPermission = hasUnclaimPermission;
                options.add(new RightClickOption("gui.harmonia.admin_unclaim_chunks", options.size(), screen) {
                    @Override
                    public void onAction(Screen screen) {
                        manager.changeSelection(dimension, left, top, _right, _bottom, false, null, _hasUnclaimPermission);
                    }
                });
            }
            if (hasUnchunkloaded) {
                int _right = right;
                int _bottom = bottom;
                options.add(new RightClickOption("gui.harmonia.chunkload_chunks", options.size(), screen) {
                    @Override
                    public void onAction(Screen screen) {
                        manager.changeSelection(dimension, left, top, _right, _bottom, true, ClaimType.CHUNK_LOADED, false);
                    }
                });
            }
            if (hasChunkloaded) {
                int _right = right;
                int _bottom = bottom;
                options.add(new RightClickOption("gui.harmonia.unchunkload_chunks", options.size(), screen) {
                    @Override
                    public void onAction(Screen screen) {
                        manager.changeSelection(dimension, left, top, _right, _bottom, false, ClaimType.CLAIMED, false);
                    }
                });
            }
        }
    }
}
