package xbeerelay;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleInsets;

/**
 * This class is used for plotting the simulated house's power consumption.
 * 
 * <p> It makes use of the open source JFreeChart library: <a href=http://www.jfree.org/jfreechart>jfree.org/jfreechart</a>
 *  and the code here is modeled off of examples found in that library.
 * 
 * @author <a href=mailto:cdw38@cornell.edu>Casey Worthington</a>
 *
 */
public class PowerPlotter extends JPanel {

	/**
	 * Generated serial version ID (by Eclipse).
	 */
	private static final long serialVersionUID = -4172513523886548443L;
	private TimeSeries powerSeries;

	/**
	 * Constructs a new PowerPlotter instance.
	 * 
	 * @param inMaxItemAgeInSeconds the maximum age for something to stay in plot
	 */
	public PowerPlotter(int inMaxItemAgeInSeconds) {
		super(new BorderLayout());

		powerSeries = new TimeSeries("Total Power Consumption");
		powerSeries.setMaximumItemAge(inMaxItemAgeInSeconds);

		TimeSeriesCollection powerDataset = new TimeSeriesCollection();
		powerDataset.addSeries(powerSeries);

		DateAxis powerDomain = new DateAxis("Time");
		NumberAxis powerRange = new NumberAxis("Total Power (mW)");
		powerDomain.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
		powerRange.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
		powerDomain.setLabelFont(new Font("SansSerif", Font.PLAIN, 14));
		powerRange.setLabelFont(new Font("SansSerif", Font.PLAIN, 14));

		XYItemRenderer powerRenderer = new XYLineAndShapeRenderer(true, false);
		powerRenderer.setSeriesPaint(0, Color.blue);
		powerRenderer.setSeriesStroke(0, new BasicStroke(3f, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_BEVEL));

		XYPlot powerPlot = new XYPlot(powerDataset, powerDomain, powerRange, powerRenderer);
		powerPlot.setBackgroundPaint(Color.lightGray);
		powerPlot.setDomainGridlinePaint(Color.white);
		powerPlot.setRangeGridlinePaint(Color.white);
		powerPlot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		powerDomain.setAutoRange(true);
		powerDomain.setLowerMargin(0.0);
		powerDomain.setUpperMargin(0.0);
		powerDomain.setTickLabelsVisible(true);
		powerRange.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		JFreeChart powerChart = new JFreeChart("Total Power Consumption in House",
				new Font("SansSerif", Font.BOLD, 24), powerPlot, true);
		powerChart.setBackgroundPaint(Color.white);
		ChartPanel chartPanel = new ChartPanel(powerChart);
		chartPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(4, 4, 4, 4),
				BorderFactory.createLineBorder(Color.black)));
		add(chartPanel);
	}

	/**
	 * Adds a new power reading to the power plot.
	 * 
	 * @param inPowerReading the power, in mW
	 */
	public void addPowerReading(double inPowerReading) {
		powerSeries.add(new Second(), inPowerReading);
	}

}
