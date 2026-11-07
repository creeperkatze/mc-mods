package dev.creeperkatze.illuminate.scene;

// How a biome feels on the lights: a base colour, an accent and the way the accent moves through the room.
// Underground biomes keep their feel in the dark instead of turning into a cave.
public record BiomeFeel(String name, Rgb base, Rgb accent, Motion motion, float strength, boolean underground) {
	public enum Motion {
		// Broad, slow waves between base and accent
		CALM,
		// Patches of sun falling through leaves
		DAPPLED,
		// Light rippling on water
		SHIMMER,
		// Warm air rising
		HEAT,
		// Single cells catching the light, like snow and ice
		GLINT,
		// Soft spots drifting up, like petals or spores
		MOTES,
		// Slow deep breaths
		PULSE;

		// How much of the accent shows in a cell at a time, from 0 to 1
		public float amount(int index, float u, float v, float time) {
			return switch (this) {
				case CALM -> 0.5F + 0.5F * (float) Math.sin(time * 0.25F + u * 2.5F + v * 1.5F);
				case DAPPLED -> Shapes.smoothstep(0.55F, 0.8F, Shapes.noise(u * 5.0F + time * 0.35F, v * 3.5F - time * 0.15F));
				case SHIMMER -> 0.5F + 0.5F * (float) (Math.sin(u * 9.0F + time * 1.2F) * Math.sin(v * 7.0F - time * 0.9F + u * 3.0F));
				case HEAT -> 0.5F + 0.5F * (float) Math.sin(v * 10.0F + time * 1.6F + Shapes.noise(u * 3.0F, time * 0.3F) * 4.0F);
				case GLINT -> (float) Math.pow(Math.max(0.0, Math.sin(time * 0.9F + Shapes.hash(index, 7) * 40.0F)), 24.0);
				case MOTES -> motes(u, v, time);
				case PULSE -> (float) Math.pow(Math.max(0.0, Math.sin(time * 0.5F)), 6.0);
			};
		}

		private static float motes(float u, float v, float time) {
			float amount = 0.0F;
			for (int mote = 0; mote < 4; mote++) {
				float mu = Shapes.noise(time * 0.08F + mote * 13.0F, mote * 7.0F);
				float mv = 1.1F - ((time * 0.04F + Shapes.hash(mote, 3)) % 1.0F) * 1.2F;
				amount += Shapes.gaussian(u, mu, 0.01F) * Shapes.gaussian(v, mv, 0.02F);
			}
			return Math.min(amount, 1.0F);
		}
	}
}
