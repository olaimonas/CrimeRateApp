package crimeRate;

import java.util.HashMap;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.utils.MapUtils;
import processing.core.PApplet;

/**
 * Written and modified by Laimonas Oberauskis
 * Visualizes crime rate in different countries. 
 * It loads the country shapes from a GeoJSON file via a data reader, and loads the crime rate 
 * (per 100_000 inhabitants) values from another CSV file (provided by the World Bank). The data 
 * value is encoded to transparency via a simplistic linear mapping.
 */
public class CrimeRate extends PApplet {

	private static final long serialVersionUID = 1L;
	
	UnfoldingMap map;
	HashMap<String, Float> crimeRateMap;
	List<Feature> countries;
	List<Marker> countryMarkers;

	public void setup() {
		size(800, 600, OPENGL);
		map = new UnfoldingMap(this, 50, 50, 700, 500, new Google.GoogleMapProvider());
		MapUtils.createDefaultEventDispatcher(this, map);

		// Load crime rate data
		crimeRateMap = loadCrimeRateFromCSV("CrimeRate.csv");
		println("Loaded " + crimeRateMap.size() + " data entries");
		

		// Load country polygons and adds them as markers
		countries = GeoJSONReader.loadData(this, "countries.geo.json");
		countryMarkers = MapUtils.createSimpleMarkers(countries);
		map.addMarkers(countryMarkers);
		
		// Country markers are shaded according to crime rate (only once)
		shadeCountries();
	}

	public void draw() {
		// Draw map tiles and country markers
		map.draw();
	}

	//Helper method to color each country based on crime rate
	//Blue-violet indicates low (0 and a little above) - the brighter blue, the lower
	//Red indicates high (the closer to 54 (max), the redder)
	private void shadeCountries() {
		for (Marker marker : countryMarkers) {
			// Find data for country of the current marker
			String countryId = marker.getId();
			if (crimeRateMap.containsKey(countryId)) {
				float lifeExp = crimeRateMap.get(countryId);
				// Encode value as brightness (values range: 0-54)
				int colorLevel = (int) map(lifeExp, 0, 54, 165, 10);
				marker.setColor(color(255-colorLevel, 100, colorLevel));
			}
			else {
				marker.setColor(color(150,150,150));
			}
		}
	}

	//Helper method to load crime rate data from file
	private HashMap<String, Float> loadCrimeRateFromCSV(String fileName) {
		HashMap<String, Float> crimeRateMap = new HashMap<String, Float>();

		String[] rows = loadStrings(fileName);
		for (String row : rows) {
			// Reads country name and crime rate value from CSV row
			String[] columns = row.split(",");
			if (columns.length == 2 && !columns[1].equals("..")) {
				crimeRateMap.put(columns[0], Float.parseFloat(columns[1]));
			}
		}

		return crimeRateMap;
	}

}