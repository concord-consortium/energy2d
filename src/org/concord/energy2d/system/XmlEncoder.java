package org.concord.energy2d.system;

import java.awt.geom.Rectangle2D;
import java.util.List;

import org.concord.energy2d.model.Anemometer;
import org.concord.energy2d.model.Cloud;
import org.concord.energy2d.model.Constants;
import org.concord.energy2d.model.Model2D;
import org.concord.energy2d.model.Part;
import org.concord.energy2d.model.Thermometer;
import org.concord.energy2d.model.Thermostat;
import org.concord.energy2d.model.Tree;
import org.concord.energy2d.view.View2D;

/**
 * @author Charles Xie
 * 
 */
class XmlEncoder {

	private System2D box;

	XmlEncoder(System2D box) {
		this.box = box;
	}

	String encode() {

		StringBuffer sb = new StringBuffer(1000);
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<state>\n");

		// links

		sb.append("<links>\n");
		if (box.getNextSimulation() != null) {
			sb.append("<next_sim>" + box.getNextSimulation() + "</next_sim>\n");
		}
		if (box.getPreviousSimulation() != null) {
			sb.append("<prev_sim>" + box.getPreviousSimulation() + "</prev_sim>\n");
		}
		sb.append("</links>\n");

		// model properties

		sb.append("<model>\n");

		if (box.model.getLx() != 10) {
			sb.append("<model_width>" + box.model.getLx() + "</model_width>\n");
		}
		if (box.model.getLy() != 10) {
			sb.append("<model_height>" + box.model.getLy() + "</model_height>\n");
		}
		sb.append("<timestep>" + box.model.getTimeStep() + "</timestep>\n");
		if (box.measure.getInterval() != 100) {
			sb.append("<measurement_interval>" + box.measure.getInterval() + "</measurement_interval>\n");
		}
		if (box.control.getInterval() != 100) {
			sb.append("<control_interval>" + box.control.getInterval() + "</control_interval>\n");
		}
		if (box.repaint.getInterval() != 20) {
			sb.append("<viewupdate_interval>" + box.repaint.getInterval() + "</viewupdate_interval>\n");
		}

		List<Task> tasks = box.taskManager.getCustomTasks();
		if (tasks != null && !tasks.isEmpty()) {
			sb.append("<tasks>\n");
			for (Task t : tasks) {
				sb.append(t.toXml() + "\n");
			}
			sb.append("</tasks>\n");
		}

		if (box.model.isSunny()) {
			sb.append("<sunny>true</sunny>");
		}
		sb.append("<sun_angle>" + box.model.getSunAngle() + "</sun_angle>\n");
		sb.append("<solar_power_density>" + box.model.getSolarPowerDensity() + "</solar_power_density>\n");
		sb.append("<solar_ray_count>" + box.model.getSolarRayCount() + "</solar_ray_count>\n");
		sb.append("<solar_ray_speed>" + box.model.getSolarRaySpeed() + "</solar_ray_speed>\n");
		sb.append("<photon_emission_interval>" + box.model.getPhotonEmissionInterval() + "</photon_emission_interval>\n");

		sb.append("<z_heat_diffusivity>" + box.model.getZHeatDiffusivity() + "</z_heat_diffusivity>");

		if (!box.model.isConvective()) {
			sb.append("<convective>false</convective>\n");
		}
		if (box.model.getBackgroundConductivity() != Constants.AIR_THERMAL_CONDUCTIVITY) {
			sb.append("<background_conductivity>" + box.model.getBackgroundConductivity() + "</background_conductivity>\n");
		}
		if (box.model.getBackgroundDensity() != Constants.AIR_DENSITY) {
			sb.append("<background_density>" + box.model.getBackgroundDensity() + "</background_density>\n");
		}
		if (box.model.getBackgroundSpecificHeat() != Constants.AIR_SPECIFIC_HEAT) {
			sb.append("<background_specific_heat>" + box.model.getBackgroundSpecificHeat() + "</background_specific_heat>\n");
		}
		if (box.model.getBackgroundTemperature() != 0) {
			sb.append("<background_temperature>" + box.model.getBackgroundTemperature() + "</background_temperature>\n");
		}
		if (box.model.getBackgroundViscosity() != Constants.AIR_VISCOSITY) {
			sb.append("<background_viscosity>" + box.model.getBackgroundViscosity() + "</background_viscosity>\n");
		}
		if (box.model.getGravityType() != Model2D.GRAVITY_UNIFORM) {
			sb.append("<gravity_type>" + box.model.getGravityType() + "</gravity_type>\n");
		}
		sb.append("<thermal_buoyancy>" + box.model.getThermalBuoyancy() + "</thermal_buoyancy>\n");
		sb.append("<buoyancy_approximation>" + box.model.getBuoyancyApproximation() + "</buoyancy_approximation>\n");

		sb.append("<boundary>\n");
		sb.append(box.model.getThermalBoundary().toXml());
		sb.append(box.model.getMassBoundary().toXml());
		sb.append("</boundary>\n");

		sb.append("<structure>\n");
		List<Part> parts = box.model.getParts();
		if (!parts.isEmpty()) {
			for (Part p : parts) {
				sb.append(p.toXml());
			}
		}
		sb.append("</structure>\n");

		// environment
		sb.append("<environment>\n");
		List<Cloud> clouds = box.model.getClouds();
		if (clouds != null) {
			for (Cloud c : clouds) {
				sb.append(c.toXml() + "\n");
			}
		}
		List<Tree> trees = box.model.getTrees();
		if (trees != null) {
			for (Tree t : trees) {
				sb.append(t.toXml() + "\n");
			}
		}
		sb.append("</environment>\n");

		// sensors
		sb.append("<sensor>\n");
		List<Thermometer> thermometers = box.model.getThermometers();
		if (!thermometers.isEmpty()) {
			for (Thermometer t : thermometers) {
				sb.append(t.toXml() + "\n");
			}
		}
		List<Anemometer> anemometers = box.model.getAnemometers();
		if (!anemometers.isEmpty()) {
			for (Anemometer a : anemometers) {
				sb.append(a.toXml() + "\n");
			}
		}
		sb.append("</sensor>\n");

		// controllers
		sb.append("<controller>\n");
		List<Thermostat> thermostats = box.model.getThermostats();
		if (thermostats != null) {
			for (Thermostat t : thermostats) {
				sb.append(t.toXml() + "\n");
			}
		}
		sb.append("</controller>\n");

		sb.append("</model>\n");

		// view properties

		sb.append("<view>\n");
		if (box.view.isGridOn()) {
			sb.append("<grid>true</grid>\n");
		}
		sb.append("<grid_size>" + box.view.getGridSize() + "</grid_size>\n");
		if (box.view.isRulerOn()) {
			sb.append("<ruler>true</ruler>\n");
		}
		if (box.view.isIsothermOn()) {
			sb.append("<isotherm>true</isotherm>\n");
		}
		if (box.view.isColorPaletteOn()) {
			sb.append("<color_palette>true</color_palette>\n");
		}
		sb.append("<color_palette_type>" + box.view.getColorPaletteType() + "</color_palette_type>\n");
		if (!box.view.isFrankOn()) {
			sb.append("<brand>false</brand>\n");
		}
		if (box.view.isControlPanelVisible()) {
			sb.append("<control_panel>true</control_panel>\n");
		}
		Rectangle2D.Float colorPalette = box.view.getColorPaletteRectangle();
		sb.append("<color_palette_x>" + colorPalette.x + "</color_palette_x>");
		sb.append("<color_palette_y>" + colorPalette.y + "</color_palette_y>");
		sb.append("<color_palette_w>" + colorPalette.width + "</color_palette_w>");
		sb.append("<color_palette_h>" + colorPalette.height + "</color_palette_h>");
		sb.append("<minimum_temperature>" + box.view.getMinimumTemperature() + "</minimum_temperature>\n");
		sb.append("<maximum_temperature>" + box.view.getMaximumTemperature() + "</maximum_temperature>\n");
		if (box.view.isVelocityOn()) {
			sb.append("<velocity>true</velocity>\n");
		}
		if (box.view.isHeatFluxArrowsOn()) {
			sb.append("<heat_flux_arrow>true</heat_flux_arrow>\n");
		}
		if (box.view.isHeatFluxLinesOn()) {
			sb.append("<heat_flux_line>true</heat_flux_line>\n");
		}
		if (box.view.isStreamlineOn()) {
			sb.append("<streamline>true</streamline>\n");
		}
		if (box.view.isGraphOn()) {
			sb.append("<graph>true</graph>\n");
		}
		if (!View2D.DEFAULT_XLABEL.equals(box.view.getGraphXLabel()))
			sb.append("<graph_xlabel>" + box.view.getGraphXLabel() + "</graph_xlabel>");
		if (!View2D.DEFAULT_YLABEL.equals(box.view.getGraphYLabel()))
			sb.append("<graph_ylabel>" + box.view.getGraphYLabel() + "</graph_ylabel>");
		if (!box.view.isClockOn()) {
			sb.append("<clock>false</clock>\n");
		}
		if (!box.view.isSmooth()) {
			sb.append("<smooth>false</smooth>\n");
		}
		if (box.view.getHeatMapType() != View2D.HEATMAP_TEMPERATURE) {
			sb.append("<heat_map>" + box.view.getHeatMapType() + "</heat_map>\n");
		}
		int n = box.view.getTextBoxCount();
		if (n > 0) {
			for (int i = 0; i < n; i++) {
				sb.append(box.view.getTextBox(i).toXml());
			}
		}
		sb.append("</view>\n");

		sb.append("</state>\n");

		return sb.toString();

	}
}
