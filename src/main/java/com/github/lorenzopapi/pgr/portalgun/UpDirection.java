package com.github.lorenzopapi.pgr.portalgun;

import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;

public enum UpDirection implements IStringSerializable {
	UP("up"),
	DOWN("down"),
	WALL("wall");

	private final String name;

	UpDirection(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return this.name;
	}

	@Override
	public String getString() {
		return this.name;
	}

	public Direction toDirection() {
		if (this == UP) return Direction.UP;
		else if (this == DOWN) return Direction.DOWN;
		else throw new RuntimeException("Error, this shouldn't be happening what the actual fuck");
	}

	public static UpDirection fromDirection(Direction direction) {
		if (direction == Direction.UP) return UP;
		else if (direction == Direction.DOWN) return DOWN;
		else return WALL;
	}

}
