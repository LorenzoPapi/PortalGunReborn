package com.github.lorenzopapi.pgr.handler;

import com.github.lorenzopapi.pgr.entity.PortalProjectileEntity;
import com.github.lorenzopapi.pgr.util.Reference;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.event.entity.EntityEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PGRChunkHandler implements ForgeChunkManager.LoadingValidationCallback {

	public static ConcurrentHashMap<UUID, List<ChunkPos>> entitiesLoadingChunks = new ConcurrentHashMap<>();

	@Override
	public void validateTickets(ServerWorld world, ForgeChunkManager.TicketHelper ticketHelper) {
		if (PGRConfig.COMMON.canPortalProjectilesChunkload.get()) {
			for (UUID id : ticketHelper.getEntityTickets().keySet()) {
				Entity ent = world.getEntityByUuid(id);
				if (ent instanceof PortalProjectileEntity) {
					PortalProjectileEntity ppe = (PortalProjectileEntity) ent;
					if (!entitiesLoadingChunks.containsKey(id)) {
						ticketHelper.removeAllTickets(id);
						entitiesLoadingChunks.put(id, new ArrayList<>());
					}
					ChunkPos chunk = new ChunkPos(ppe.getPosition());
					forceChunk(world, id, chunk, true);
					entitiesLoadingChunks.get(id).add(chunk);
					while (entitiesLoadingChunks.size() > 20) {
						Iterator<UUID> iterator = entitiesLoadingChunks.keySet().iterator();
						if (iterator.hasNext()) {
							UUID uuid = iterator.next();
							ticketHelper.removeAllTickets(uuid);
							entitiesLoadingChunks.remove(uuid);
							iterator.remove();
						}
					}
				}
			}
		}
	}

	public static void checkAndCreateTicket(EntityEvent.EnteringChunk e) {
		if (!e.getEntity().getEntityWorld().isRemote && e.getEntity() instanceof PortalProjectileEntity && e.getEntity().isAlive()) {
			PortalProjectileEntity ppe = (PortalProjectileEntity) e.getEntity();
			UUID id = ppe.getUniqueID();
			List<ChunkPos> positions = entitiesLoadingChunks.computeIfAbsent(id, k -> new ArrayList<>());
			if (e.getOldChunkX() != 0 && e.getOldChunkZ() != 0)
				forceChunk(e.getEntity().getCommandSource().getWorld(), id, e.getOldChunkX(), e.getOldChunkZ(), false);
			forceChunk(e.getEntity().getCommandSource().getWorld(), id, e.getNewChunkX(), e.getNewChunkZ(), true);
			positions.add(new ChunkPos(e.getNewChunkX(), e.getNewChunkZ()));
			positions.remove(new ChunkPos(e.getOldChunkX(), e.getOldChunkZ()));
		}
	}

	private static void forceChunk(ServerWorld world, UUID id, ChunkPos chunk, boolean add) {
		ForgeChunkManager.forceChunk(world, Reference.MODID, id, chunk.x, chunk.z, add, false);
	}

	private static void forceChunk(ServerWorld world, UUID id, int x, int y, boolean add) {
		forceChunk(world, id, new ChunkPos(x, y), add);
	}

	public static void removeTicket(PortalProjectileEntity ppe) {
		if (PGRConfig.COMMON.canPortalProjectilesChunkload.get()) {
			entitiesLoadingChunks.remove(ppe.getUniqueID());
		}
	}
}
