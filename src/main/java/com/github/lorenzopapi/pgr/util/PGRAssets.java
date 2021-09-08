package com.github.lorenzopapi.pgr.util;

import com.github.lorenzopapi.pgr.rendering.shader.PGRShaders;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

public class PGRAssets extends ReloadListener<Pair<String, String>> {
	public static final IFutureReloadListener INSTANCE = new PGRAssets();
	
	@Override
	protected Pair<String, String> prepare(IResourceManager resourceManagerIn, IProfiler profilerIn) {
		try {
			String vert = new String(readFully(getStream(resourceManagerIn, new ResourceLocation("portalgunreborn:shaders/portal_shader.vert"))));
			String frag = new String(readFully(getStream(resourceManagerIn, new ResourceLocation("portalgunreborn:shaders/portal_shader.frag"))));
			return Pair.of(vert, frag);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		return null;
	}
	
	@Override
	protected void apply(Pair<String, String> objectIn, IResourceManager resourceManagerIn, IProfiler profilerIn) {
		PGRShaders.reload(objectIn);
	}
	
	private static InputStream getStream(IResourceManager manager, ResourceLocation location) {
		try {
			IResource resource = manager.getResource(location);
			if (resource == null) return null;
			else return resource.getInputStream();
		} catch (Throwable err) {
			return null;
		}
	}
	
	private static byte[] readFully(InputStream stream) throws IOException {
		ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
		int b;
		while (((b = stream.read()) != -1)) stream1.write(b);
		byte[] bytes = stream1.toByteArray();
		stream1.close();
		stream1.flush();
		return bytes;
	}
}
