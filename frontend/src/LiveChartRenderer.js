import { useEffect, useState } from 'react';
import { Line } from 'react-chartjs-2';
import { liveChartOptions } from './chartConfig';
import { MAX_POINTS_DISPLAYED_ON_CHART } from './consts';
import { customCanvasBackgroundColor } from './plugins';

const LiveChartRenderer = ({ currentX, time }) => {
  const [labels, setLabels] = useState([]);
  const [xDataSet, setXDataSet] = useState([[], []]);

  useEffect(() => {
    setLabels((labels) => {
      if (labels.length > MAX_POINTS_DISPLAYED_ON_CHART)
        // Don't want to display all values. Only most recent 3 seconds
        labels = labels.slice(labels.length - MAX_POINTS_DISPLAYED_ON_CHART);
      return [...labels, time];
    });
    setXDataSet((ds) => {
      const newDs = [...ds];
      currentX.forEach((x, i) => {
        if (newDs[i].length > MAX_POINTS_DISPLAYED_ON_CHART)
          // Don't want to display all values. Only most recent 3 seconds
          newDs[i] = newDs[i].slice(ds.length - MAX_POINTS_DISPLAYED_ON_CHART);
        newDs[i].push(x);
      });
      return newDs;
    });
  }, [currentX, time]);

  const data = {
    labels: labels,
    datasets: [
      {
        label: 'Traffic light status',
        data: xDataSet[0],
        borderColor: 'rgb(255, 99, 132)',
        backgroundColor: 'rgba(255, 99, 132, 0.5)',
      },
      {
        label: 'Traffic light status',
        data: xDataSet[1],
        borderColor: 'rgb(0, 99, 132)',
        backgroundColor: 'rgba(0, 99, 132, 0.5)',
      },
    ],
  };

  return (
    <div className='live-chart'>
      <Line
        data={data}
        options={liveChartOptions}
        plugins={[customCanvasBackgroundColor]}
      />
    </div>
  );
};

export default LiveChartRenderer;
