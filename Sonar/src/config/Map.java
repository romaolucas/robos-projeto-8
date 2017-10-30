package config;

import lejos.geom.Line;
import lejos.geom.Rectangle;
import lejos.robotics.mapping.LineMap;

public class Map {
	public static LineMap makeMap () {
//		Line[] lines1 = {
//			/* L-shape polygon */
//			new Line(164,356,58,600),
//			new Line(58,600,396,721),
//			new Line(396,721,455,600),
//			new Line(455,600,227,515),
//			new Line(227,515,280,399),
//			new Line(280,399,164,356),
//			/* Triangle */
//			new Line(778,526,1079,748),
//			new Line(1079,748,1063,436),
//			new Line(1063,436,778,526),
//			/* Pentagon */
//			new Line(503,76,333,267),
//			new Line(333,267,481,452),
//			new Line(481,452,730,409),
//			new Line(730,409,704,150),
//			new Line(704,150,503,76)
//		};
		// Line[] lines2 = {
		// 	/* L-shape polygon */
		// 	new Line(16.4f,35.6f,5.8f,60.0f),
		// 	new Line(5.8f,60.0f,39.6f,72.1f),
		// 	new Line(39.6f,72.1f,45.5f,60.0f),
		// 	new Line(45.5f,60.0f,22.7f,51.5f),
		// 	new Line(22.7f,51.5f,28.0f,39.9f),
		// 	new Line(28.0f,39.9f,16.4f,35.6f),
		// 	/* Triangle */
		// 	new Line(77.8f,52.6f,107.9f,74.8f),
		// 	new Line(107.9f,74.8f,106.3f,43.6f),
		// 	new Line(106.3f,43.6f,77.8f,52.6f),
		// 	/* Pentagon */
		// 	new Line(50.3f,7.6f,33.3f,26.7f),
		// 	new Line(33.3f,26.7f,48.1f,45.2f),
		// 	new Line(48.1f,45.2f,73.0f,40.9f),
		// 	new Line(73.0f,40.9f,70.4f,15.0f),
		// 	new Line(70.4f,15.0f,50.3f,7.6f)
		// };
		Line[] lines2 = {
			/* L-shape polygon */
			new Line(0f, 0f, 155f, 0f),
			new Line(155f, 0f, 110f, 135f),
			new Line(110f, 135f, 0f, 132f),
			new Line(0f, 132f, 0f, 0f)
		};
		Rectangle bounds = new Rectangle(0, 0, 1189, 841);
		LineMap  map = new LineMap(lines2, bounds);

		return map;
	}
}
