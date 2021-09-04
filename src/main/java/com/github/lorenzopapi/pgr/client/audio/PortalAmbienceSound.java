package com.github.lorenzopapi.pgr.client.audio;

import com.github.lorenzopapi.pgr.handler.PGRRegistry;
import com.github.lorenzopapi.pgr.portal.structure.PortalStructure;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.util.SoundCategory;

public class PortalAmbienceSound extends TickableSound {
	private final PortalStructure struct;

	public PortalAmbienceSound(PortalStructure struct) {
		super(PGRRegistry.PGRSounds.PORTAL_AMBIENCE, SoundCategory.BLOCKS);
		this.volume = 0.003F;
		this.struct = struct;
		this.x = struct.positions.get(0).getX();
		this.y = struct.positions.get(0).getY();
		this.z = struct.positions.get(0).getZ();
	}

	@Override
	public void tick() {
		if (struct.beingRemoved)
			this.finishPlaying();
	}
}
