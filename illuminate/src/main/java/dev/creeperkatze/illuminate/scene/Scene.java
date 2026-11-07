package dev.creeperkatze.illuminate.scene;

import dev.creeperkatze.illuminate.game.GameEvent;
import dev.creeperkatze.illuminate.game.GameState;
import dev.creeperkatze.illuminate.scene.layer.AtmosphereLayer;
import dev.creeperkatze.illuminate.scene.layer.EnvironmentLayer;
import dev.creeperkatze.illuminate.scene.layer.EventLayer;
import dev.creeperkatze.illuminate.scene.layer.IdleLayer;
import dev.creeperkatze.illuminate.scene.layer.PortalLayer;
import dev.creeperkatze.illuminate.scene.layer.StatusEffectLayer;
import dev.creeperkatze.illuminate.scene.layer.SubmergedLayer;
import dev.creeperkatze.illuminate.scene.layer.VitalsLayer;

import java.util.List;

// Paints the world into a canvas. The environment is smoothed over time, effects on top animate themselves.
public final class Scene {
	private final Layer environment = new EnvironmentLayer();
	private final Layer idle = new IdleLayer();
	private final List<Layer> overlays = List.of(
			// Painted in this order, so later layers cover earlier ones
			new AtmosphereLayer(),
			new SubmergedLayer(),
			new PortalLayer(),
			new StatusEffectLayer(),
			new VitalsLayer(),
			new EventLayer());

	private final Canvas target = new Canvas();
	private final Canvas smoothed = new Canvas();
	private final Canvas output = new Canvas();
	private boolean first = true;
	private float menuDim;

	public Canvas render(GameState state, List<GameEvent> events, Moment moment) {
		for (GameEvent event : events) {
			overlays.forEach(layer -> layer.onEvent(event, moment));
		}

		if (state.inWorld()) {
			environment.paint(target, state, moment);
		} else {
			idle.paint(target, state, moment);
		}
		if (first) {
			smoothed.copyFrom(target);
			first = false;
		}
		float speed = 1.5F + (1.0F - moment.config().smoothing) * 16.0F;
		smoothed.approach(target, 1.0F - (float) Math.exp(-speed * moment.delta()));
		output.copyFrom(smoothed);

		if (state.inWorld() || moment.previewing()) {
			overlays.forEach(layer -> layer.paint(output, state, moment));
		}

		boolean dim = state.inWorld() && state.menuOpen() && moment.config().dimInMenus;
		menuDim = Shapes.approach(menuDim, dim ? 0.5F : 0.0F, 6.0F, moment.delta());
		float brightness = moment.config().brightness * (1.0F - menuDim);
		float saturation = moment.config().saturation;
		for (int i = 0; i < Canvas.SIZE; i++) {
			output.set(i, output.get(i).saturate(saturation).scale(brightness));
		}
		return output;
	}
}
