package com.zstreaming.statistics;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.zstreaming.gui.components.StatisticsChart;
import com.zstreaming.launcher.ZStreaming;

public class DisabledChartCounter {
	
	StatisticsChart[] charts; 
	
	public DisabledChartCounter() {
		super();
	}
	
	public void checkEnabledChart(StatisticsChart[] charts) {		
		this.charts = charts;
		
		int max = 4;
		
		try {
			max = Integer.parseInt(ZStreaming.getSettingManager().getSettings().get("max.download"));
			
			if(max < 1) throw new IllegalArgumentException("Value too low");			
		}catch(NumberFormatException e) { }

		for(int i=0; i<this.charts.length;i++) {
			this.charts[i].disable(i > max - 1);
		}
		
	}
	
	public List<StatisticsChart> getEnabledCharts() {
		List<StatisticsChart> enabledCharts = Arrays.asList(this.charts).stream().filter(chart->!chart.isDisabled()).collect(Collectors.toList());
		
		for(StatisticsChart chart : this.charts) {
			chart.disabledProperty().addListener((observable, oldValue, newValue)->{		
				if(newValue)
					enabledCharts.remove(chart);
				else
					enabledCharts.add(chart);
			});			
		}		
		return enabledCharts;
	}

}
