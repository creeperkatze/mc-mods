package dev.creeperkatze.exchange.dh;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Block states and biomes as Distant Horizons writes them into its id mappings
public final class DhStates {
	public static final String AIR = "AIR";
	public static final String DEFAULT_BIOME = "minecraft:plains";
	private static final String STATE_SEPARATOR = "_STATE_";
	private static final String ENTRY_SEPARATOR = "_DH-BSW_";

	private static final Map<String, BlockState> PARSED = new ConcurrentHashMap<>();
	private static final Map<Block, Map<String, BlockState>> STATES_BY_BLOCK = new ConcurrentHashMap<>();

	private DhStates() {
	}

	public static String entry(String biome, String block) {
		return biome + ENTRY_SEPARATOR + block;
	}

	public static String biome(String entry) {
		int separator = entry.indexOf(ENTRY_SEPARATOR);
		return separator < 0 ? DEFAULT_BIOME : entry.substring(0, separator);
	}

	public static String block(String entry) {
		int separator = entry.indexOf(ENTRY_SEPARATOR);
		return separator < 0 ? AIR : entry.substring(separator + ENTRY_SEPARATOR.length());
	}

	public static String serialize(BlockState state) {
		if (state.isAir()) {
			return AIR;
		}
		return BuiltInRegistries.BLOCK.getKey(state.getBlock()) + STATE_SEPARATOR + properties(state);
	}

	public static BlockState parse(String block) {
		return PARSED.computeIfAbsent(block, DhStates::find);
	}

	private static BlockState find(String block) {
		if (block.isEmpty() || block.equals(AIR)) {
			return Blocks.AIR.defaultBlockState();
		}
		int separator = block.indexOf(STATE_SEPARATOR);
		String id = separator < 0 ? block : block.substring(0, separator);
		Identifier identifier = Identifier.tryParse(id);
		Block found = identifier == null ? null : BuiltInRegistries.BLOCK.getOptional(identifier).orElse(null);
		if (found == null) {
			return Blocks.AIR.defaultBlockState();
		}
		if (separator < 0) {
			return found.defaultBlockState();
		}
		String properties = block.substring(separator + STATE_SEPARATOR.length());
		Map<String, BlockState> states = STATES_BY_BLOCK.computeIfAbsent(found, DhStates::statesByProperties);
		return states.getOrDefault(properties, found.defaultBlockState());
	}

	private static Map<String, BlockState> statesByProperties(Block block) {
		Map<String, BlockState> states = new HashMap<>();
		for (BlockState state : block.getStateDefinition().getPossibleStates()) {
			states.put(properties(state), state);
		}
		return states;
	}

	// Sorted by name, with toString values like Distant Horizons
	private static String properties(BlockState state) {
		List<Property<?>> properties = new ArrayList<>(state.getProperties());
		properties.sort(Comparator.comparing(Property::getName));
		StringBuilder builder = new StringBuilder();
		for (Property<?> property : properties) {
			builder.append('{').append(property.getName()).append(':').append(state.getValue(property)).append('}');
		}
		return builder.toString();
	}
}
