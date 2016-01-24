package src;
/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc. 
 * in the United States and other countries.]
 *
 * -------------------
 * LineChartDemo6.java
 * -------------------
 * (C) Copyright 2004, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: LineChartDemo6.java,v 1.5 2004/04/26 19:11:55 taqua Exp $
 *
 * Changes
 * -------
 * 27-Jan-2004 : Version 1 (DG);
 * 
 */

import java.awt.Color;
import java.util.ArrayList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.*;
//import org.jfree.ui.Spacer;
import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
/**
 * A simple demonstration application showing how to create a line chart using data from an
 * {@link XYDataset}.
 *
 */
public class DrawPlot extends ApplicationFrame {

    /**
     * Creates a new demo.
     *
     * @param title  the frame title.
     */
    public DrawPlot(final String title,ArrayList<Double> F,ArrayList<Double> F_low,ArrayList<Double> F2,ArrayList<Double> F2_up,ArrayList<Double> F1, ArrayList<Double> Penalty) {

        super(title);

        final XYDataset dataset = createDataset(F,F_low,F2,F2_up,F1,Penalty);
        final JFreeChart chart = createChart(dataset);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(chartPanel);

    }
    
    /**
     * Creates a sample dataset.
     * 
     * @return a sample dataset.
     */
    private XYDataset createDataset(ArrayList<Double> F,ArrayList<Double> F_low,ArrayList<Double> F2,ArrayList<Double> F2_up,ArrayList<Double> F1, ArrayList<Double> Penalty) {
        
    	final XYSeries series1 = new XYSeries("F");
        for(int i=0;i<F.size();i++)
        	series1.add(i, F.get(i));
        
        final XYSeries series2 = new XYSeries("F_low");
        for(int i=0;i<F_low.size();i++)
        	series2.add(i, F_low.get(i));
        
        final XYSeries series3 = new XYSeries("F2");
        for(int i=0;i<F2.size();i++)
        	series3.add(i, F2.get(i));
        
        final XYSeries series4 = new XYSeries("F2_up");
        for(int i=0;i<F2_up.size();i++)
        	series4.add(i, F2_up.get(i));
        
        final XYSeries series5 = new XYSeries("F1");
        for(int i=0;i<F1.size();i++)
        	series5.add(i, F1.get(i));
        
        final XYSeries series6 = new XYSeries("Penalty");
        for(int i=0;i<Penalty.size();i++)
        	series6.add(i, Penalty.get(i));
        
        final XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series1);
        dataset.addSeries(series2);
        dataset.addSeries(series3);
        dataset.addSeries(series4);
        dataset.addSeries(series5);
        dataset.addSeries(series6);
        return dataset;
        
    }
    
    /**
     * Creates a chart.
     * 
     * @param dataset  the data for the chart.
     * 
     * @return a chart.
     */
    private JFreeChart createChart(final XYDataset dataset) {
        
        // create the chart...
        final JFreeChart chart = ChartFactory.createXYLineChart(
            "Functions and upper/lower bound",      // chart title
            "iteration",                      // x axis label
            "Score",                      // y axis label
            dataset,                  // data
            PlotOrientation.VERTICAL,
            true,                     // include legend
            true,                     // tooltips
            false                     // urls
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setBackgroundPaint(Color.white);

//        final StandardLegend legend = (StandardLegend) chart.getLegend();
  //      legend.setDisplaySeriesShapes(true);
        
        // get a reference to the plot for further customisation...
        final NumberAxis domainAxis = new NumberAxis("X-Axis");
        //domainAxis.setRange(0.0,101.00);
        domainAxis.setTickUnit(new NumberTickUnit(1.0));
        final NumberAxis rangeAxis = new NumberAxis("Y-Axis");
        rangeAxis.setRange(0.00,1.20);
        rangeAxis.setTickUnit(new NumberTickUnit(0.01));

        /*final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(0.0, 1.0, 0.0, 30.0));
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        
        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShapesVisible(1, false);
        plot.setRenderer(renderer);*/

        // change the auto tick unit selection to integer units only...
        //final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        //rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        // OPTIONAL CUSTOMISATION COMPLETED.
                
        return chart;
        
    }

    // ****************************************************************************
    // * JFREECHART DEVELOPER GUIDE                                               *
    // * The JFreeChart Developer Guide, written by David Gilbert, is available   *
    // * to purchase from Object Refinery Limited:                                *
    // *                                                                          *
    // * http://www.object-refinery.com/jfreechart/guide.html                     *
    // *                                                                          *
    // * Sales are used to provide funding for the JFreeChart project - please    * 
    // * support us so that we can continue developing free software.             *
    // ****************************************************************************
    
    /**
     * Starting point for the demonstration application.
     *
     * @param args  ignored.
     */
    public static void draw(ArrayList<Double> F,ArrayList<Double> F_low,ArrayList<Double> F2,ArrayList<Double> F2_up, ArrayList<Double> F1, ArrayList<Double> Penalty){
        
        final DrawPlot demo = new DrawPlot("Line Chart Demo 6",F,F_low,F2,F2_up,F1, Penalty);
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);
    }
    public static void main(final String[] args) {
    	//double[] prob=new double[]{0.7756, 0.7507, 0.7173, 0.6886, 0.671, 0.6606, 0.6025, 0.5809, 0.5779, 0.548, 0.5451, 0.5393, 0.5222, 0.5222, 0.5193, 0.5152, 0.5152, 0.5082, 0.4972, 0.4957, 0.4939, 0.4888, 0.4878, 0.4734, 0.4567, 0.4543, 0.453, 0.4477, 0.4428, 0.4297,1.0};
        
       // final JfreeChartTest demo = new JfreeChartTest("Line Chart Demo 6");
        //demo.pack();
       // RefineryUtilities.centerFrameOnScreen(demo);
        //demo.setVisible(true);

    }

}
